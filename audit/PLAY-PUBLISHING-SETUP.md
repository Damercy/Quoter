# Quoter Play publishing setup

Configured on 9 October 2026 using the owner's signed-in Chrome profile.

- Cloud project: `quoter-323321` (existing Quoter project).
- Google Play Android Developer API: enabled.
- Publisher identity: `quoter-play-publisher@quoter-323321.iam.gserviceaccount.com`.
- No Google Cloud project roles granted to this identity.
- Play access: Quoter only (`com.dayaonweb.quoter`), with production releases,
  testing releases and store presence. Play automatically bundles read access,
  policy declarations and deep links. No account administrator, financial-data,
  orders, review-reply or Stack-app access granted.
- Credentials: `%USERPROFILE%\.codex\quoter-play\credentials\publisher.json`.
  The downloaded key was moved out of Documents. Its directory and file have
  Windows ACLs restricted to the owner. Never commit or print the key.
- Python runtime: `%USERPROFILE%\.codex\quoter-play\venv\Scripts\python.exe`.
- Live connection check passed: en-GB listing, screenshot references and
  production/beta/alpha/internal tracks read successfully. The temporary edit was
  discarded. The snapshot is under `%USERPROFILE%\.codex\quoter-play\backups`.
- No bundle uploaded or release/listing change published during setup.

## Use from a chat or terminal

The Android CLI remains the build/device workflow. Its installed version has no
Play publishing command. Use `scripts/play-store.py` for publishing through the
official Google API. This avoids Fastlane's partially supported Windows runtime.

```powershell
$quoterPlayPython = "$env:USERPROFILE\.codex\quoter-play\venv\Scripts\python.exe"
& $quoterPlayPython scripts/play-store.py export
& $quoterPlayPython scripts/play-store.py plan --manifest play-store/release.json
& $quoterPlayPython scripts/play-store.py apply --manifest play-store/release.json
# Only after reviewing/authorizing the actual release:
& $quoterPlayPython scripts/play-store.py apply --manifest play-store/release.json --commit
```

`plan` validates files and text locally and prints file hashes. `apply` uploads to
a temporary edit, validates it with Google and discards it unless `--commit` is
explicit. Bundle uploads contact Google even without `--commit`; use `plan` for
a purely local preview. Each API run exports existing metadata and image
references to a local snapshot before changing the temporary edit. Changes already
in review cause commit to fail rather than cancelling that review.

`play-store/listing.json` contains the current en-GB listing as a starting point.
Five mock tests passed, including discard on validation failure and the explicit
commit requirement. Actual bundle and screenshot uploads remain untested until
the signed release and final images are available.

The JSON manifest has this structure (paths are relative to the manifest):

```json
{
  "packageName": "com.dayaonweb.quoter",
  "listings": {
    "en-GB": {"shortDescription": "REPLACE WITH REVIEWED DESCRIPTION"}
  },
  "images": {
    "en-GB": {"phoneScreenshots": ["screenshots/01.png", "screenshots/02.png"]}
  },
  "bundle": "../app/build/outputs/bundle/release/app-release.aab",
  "mapping": "../app/build/outputs/mapping/release/mapping.txt",
  "release": {
    "track": "internal",
    "status": "completed",
    "expectedVersionCode": 40000,
    "name": "4.00.00",
    "releaseNotes": [{"language": "en-GB", "text": "REPLACE WITH REVIEWED RELEASE NOTES"}]
  }
}
```

Omit fields that should remain unchanged. Image lists replace the specified image
type in that language, in order. The release replaces the target track's release
list; review its snapshot first, especially if a staged release exists. For a
staged rollout use `status: inProgress` and `userFraction` between 0 and 1. Google
still controls review, eligibility, image requirements and publishing timing.

Signing is now configured through private environment variables. The existing
upload key/alias was recovered and verified against Play's upload certificate.
`scripts/build-signed-release.ps1` supplies the Windows-user encrypted credential
to Gradle; debug/development builds can still run without it. Gradle signingReport
passed and showed the expected release certificate. The release orchestrator
verifies the signed bundle cryptographically and checks that certificate before
uploading. See `audit/RELEASE-WORKFLOW.md` for the final confirmation workflow.
Do not put keystore passwords in tracked Gradle files or manifests.

## Production submission — 4.00.00

Version code 40000 and the signed GitHub bundle were submitted to production,
with the reviewed en-GB description, eight phone screenshots, four screenshots
in each tablet slot, release notes and R8 crash mapping. Google validated and
committed the edit. Submission is distinct from review approval/public availability.

Bundle SHA-256: `aca9c616d8c2328a4ee75118a95305f4733394a4b40ab2c0dce9b76c88044a32`.
Play Console subsequently confirmed publication on 9 October 2026. A separate
metadata-only edit with English refinements, Spanish/German listings and localized
screenshot captions was validated and committed. It leaves production code 40000
and its uploaded bundle unchanged; localization approval/propagation is separate.
Private before/after snapshots and submission logs are retained under
`%USERPROFILE%/.codex/quoter-play/releases/play-v4.00.00`.

The publishing client uses 1 MiB resumable bundle/mapping uploads with bounded
chunk retries and the Google client's upload-aware HTTP transport. An initial
write timeout and a resumable-response transport error stopped before commit;
the successful attempt validated and committed the entire edit.

## Home-address support request

The street address remains public. An account change has not been made. The
support draft was filled in, but has not been submitted:

https://play.google.com/console/u/0/developers/5440443008270660403/create-support-ticket

Selected topic: Play developer account and verification → General developer
account → I have questions about editing Developer Profiles.

The request is in **Describe your issue**; the owner can review and select Submit.
The payments/identity profile must not be deleted as a troubleshooting experiment.

## References

- https://developers.google.com/android-publisher/getting_started
- https://developers.google.com/android-publisher/api-ref/rest
- https://developers.google.com/android-publisher/api-ref/rest/v3/edits/commit
- https://support.google.com/googleplay/android-developer/answer/13628312
- https://docs.fastlane.tools/
