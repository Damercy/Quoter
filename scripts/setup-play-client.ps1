$ErrorActionPreference = 'Stop'
$quoterRuntime = Join-Path $env:USERPROFILE '.codex/quoter-play/venv'
$quoterPython = Join-Path $quoterRuntime 'Scripts/python.exe'
if (!(Test-Path -LiteralPath $quoterPython)) {
    & python -m venv $quoterRuntime
    if ($LASTEXITCODE -ne 0) { throw 'Install Python 3.11 or newer and rerun setup.' }
}
& $quoterPython -m pip install -r (Join-Path $PSScriptRoot 'play-store-requirements.txt')
if ($LASTEXITCODE -ne 0) { throw 'Play client dependency installation failed.' }
Write-Output 'Play client installed. Supply publisher/signing credentials privately; they are never cloned from Git.'
