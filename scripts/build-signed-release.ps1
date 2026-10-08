param([switch]$VerifyOnly)
$ErrorActionPreference = 'Stop'
$quoterRoot = Split-Path $PSScriptRoot -Parent
$quoterSecrets = Join-Path $env:USERPROFILE '.codex/quoter-play/credentials'
$quoterCredential = Import-Clixml (Join-Path $quoterSecrets 'signing.clixml')
$quoterVariables = @('QUOTER_UPLOAD_STORE','QUOTER_UPLOAD_ALIAS','QUOTER_UPLOAD_STORE_PASSWORD','QUOTER_UPLOAD_KEY_PASSWORD')
$quoterPrevious = @{}
foreach ($quoterName in $quoterVariables) { $quoterPrevious[$quoterName] = [Environment]::GetEnvironmentVariable($quoterName) }
try {
    $env:QUOTER_UPLOAD_STORE = Join-Path $quoterSecrets 'upload.jks'
    $env:QUOTER_UPLOAD_ALIAS = $quoterCredential.UserName
    $env:QUOTER_UPLOAD_STORE_PASSWORD = $quoterCredential.GetNetworkCredential().Password
    $env:QUOTER_UPLOAD_KEY_PASSWORD = $env:QUOTER_UPLOAD_STORE_PASSWORD
    Push-Location $quoterRoot
    try {
        if ($VerifyOnly) {
            & ./gradlew.bat --no-daemon :app:signingReport
        } else {
            & ./gradlew.bat --no-daemon :app:testDebugUnitTest :app:lintDebug :app:lintRelease :app:bundleRelease :app:assembleRelease
        }
        if ($LASTEXITCODE -ne 0) { throw 'Gradle release verification failed.' }
    } finally { Pop-Location }
} finally {
    foreach ($quoterName in $quoterVariables) { [Environment]::SetEnvironmentVariable($quoterName, $quoterPrevious[$quoterName]) }
    $quoterCredential = $null
}
