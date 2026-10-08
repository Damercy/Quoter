# Quoter release workflow

Configured 9 October 2026. GitHub CLI is connected as Damercy, with write access
to the existing public repository `Damercy/Quoter`. Its default branch is `master`.
Git uses the GitHub CLI credential helper. Version 4.00.00 is prepared for the
owner's explicitly requested GitHub-only publication; Play submission is pending.

## Final confirmation in chat

When the app is ready, prepare the final README, versioned CHANGELOG/release notes,
store descriptions, ordered screenshots and production manifest. Run the agreed
app checks and build a signed bundle. Show the owner the version, destination
track, changes, images, validation results and remaining limitations. Prepare the
release plan and record its approval digest. The owner's explicit “100%” release
confirmation authorizes execution of that exact reviewed plan. If sources,
notes, bundle or screenshots change, prepare and review a fresh plan.

The shared screenshot set is now prepared in `play-store/screenshots`, with
ordering/alt text in `play-store/media.json`. Run `scripts/sync-store-gallery.py`
to regenerate both README and `play-store/store-update.json`. Copy its listings
and images into the final bundle release manifest so the store and source release
use identical assets. `play-store/ASO-RESEARCH.md` records the prepared copy,
measurement plan and YouTube preview-video requirements. Recheck screenshots
against the final build; the current set captures the 4.00.00 development app.

For an explicitly requested GitHub-only publication, add `--github-only` to
`prepare`. Add `--asset app/build/distribution/v4.00.00/quoter-4.00.00.apk`
and `--asset app/build/distribution/v4.00.00/SHA256SUMS.txt` to include the signed
APK and checksums alongside the manifest's bundle and mapping. This mode skips
Play entirely. A request to update GitHub authorizes that destination only;
publishing both destinations still requires the owner's final release request.

```powershell
# Set JAVA_HOME to JDK 17 or newer and ANDROID_HOME to the installed SDK.
./scripts/build-signed-release.ps1
$quoterPython = "$env:USERPROFILE/.codex/quoter-play/venv/Scripts/python.exe"
& $quoterPython scripts/release.py prepare --manifest play-store/release.json `
    --notes play-store/release-notes.md --keytool "$env:JAVA_HOME/bin/keytool.exe" `
    --jarsigner "$env:JAVA_HOME/bin/jarsigner.exe"
# Only after the owner confirms the reviewed plan:
& $quoterPython scripts/release.py execute --plan <private-plan-path> --approval <reviewed-digest>
```

The prepare step reads GitHub permission/history, verifies the signed bundle
against the public Play upload fingerprint, and freezes release source/asset
hashes. It does not commit, push, create tags/releases or upload to Play.
The source set includes the app, Gradle, scripts, store assets, README, CHANGELOG,
AGENTS instructions and these release docs. Local device evidence and the POC
are excluded. Review the plan's complete file list before confirmation. Release
notes are kept in `play-store/release-notes.md` and the CHANGELOG is updated with
the same versioned entry so both are preserved in the cloned repository.

Execution stages that source set, removes tracked keystores from the new commit
without deleting local copies, commits, creates an annotated version tag, and
pushes `master` plus the tag atomically without force. It then creates a draft
GitHub release, uploads the bundle/mapping and additional reviewed assets,
commits the Play edit including reviewed listing/images when Play is selected,
and publishes the GitHub release. Google review and
managed publishing can delay public availability. A successful API submission
does not prove the app is already live in the store.

GitHub and Play do not share a transaction. The private plan folder contains a
step journal. GitHub failures can be resumed using the same plan and digest.
If the Play request fails after it begins, the tool stops repeat uploads: inspect
Play Console and the snapshot first, determine whether the edit committed,
and resolve the journal with that evidence. Do not blindly replay the release.
No automatic run on a timer or tag push was enabled; final release is started
from this chat after approval.

## Signing and credentials

The existing `quoter` private-key alias and supplied password were verified. Its
SHA-256 certificate matches Play's upload certificate stored in
`play-store/upload-certificate.sha256`. Google has a separate app signing key.
The active local keystore is `%USERPROFILE%/.codex/quoter-play/credentials/upload.jks`.
The password is Windows-user encrypted in `signing.clixml` alongside it. The
PowerShell build wrapper decrypts only for the Gradle process and restores its
environment afterward. These files are excluded from Git and have owner-only
Windows ACLs. Keep an independently secured backup: Windows credential
encryption does not make the password portable to another machine/account.

The old keystore exists in the public repository's history. Removing it from
the next commit does not erase that history. The owner explicitly chose to keep
the current upload key for now. A replacement was generated locally as an unused
standby (`upload-pending.jks`, `signing-pending.clixml` and its public PEM); no
reset request was submitted and it is not the active signing identity.

Cloning contains the app/build/release tooling, not signing passwords, the Play
publisher credential, or GitHub login. Another machine needs SDK/JDK installation
and secure credential provisioning to publish. Normal development builds do not
need release credentials. CI verification remains separate from final release;
publisher/signing secrets were not uploaded to GitHub Actions.

## Validation of the tooling

GitHub identity and repository write permissions were read live. The previous
Play API check read Quoter's listing and release tracks. Keystore store/key
passwords and the Play upload certificate were verified. Unit tests cover the
approval gate, changed files and stop-on-uncertain-Play behavior. No final app
release was uploaded or published to Play as part of this setup. Local signed
4.00.00 APK/AAB builds passed unit tests and debug/release lint.

For a new Windows checkout, install Python 3.11 or newer and GitHub CLI, run
`scripts/setup-play-client.ps1`, authenticate GitHub, and provision the private
credentials separately. Copy `play-store/release.example.json` to `release.json`
and replace its placeholders with the final reviewed assets/text. The local
signingReport passed with the active Play upload certificate; all eleven release
client tests passed. App tests and final signed artifact verification are run
again against the finished app before requesting final approval.
