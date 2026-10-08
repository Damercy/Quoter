# Continuous integration

`Android verification` runs on pushes to `master`/`main`, pull requests targeting
those branches, and manual dispatch. Feature-branch pushes and tag pushes do not
start duplicate runs. New runs cancel older runs for the same branch or PR.

| Changed paths | Checks |
| --- | --- |
| Any change | Workflow/shell lint, publishing guards, CI routing tests, generated gallery/inventory consistency and tracked-credential rejection |
| App sources/resources/data/tests, Gradle configuration or Android workflow/routing | Above plus unit tests, debug/release lint, unsigned bundle, preview comparisons and native ELF alignment |
| Android changes after a successful build | API 24 phone and API 37 wide-window instrumented tests |
| README, AGENTS, audit docs, store copy/screenshots or publishing-only tooling | Lightweight checks; Android build/device jobs skip |
| Icon SVG sources alone | Lightweight checks; generated Android resource changes trigger full checks |

The path rules live in `.github/ci-paths.json` and have regression tests in
`scripts/test_ci_changes.py`. All changed paths are examined, including deleted
files. PR comparisons use the base/merge-base; pushes use the event's previous
commit. An unknown/initial base conservatively enables Android checks.

Draft PRs keep build verification but defer emulator jobs until ready for review.
Manual **Run workflow** in GitHub Actions forces the full build/device matrix.
There is no publishing step and no signing or Play credential in CI.

Jobs are bounded to 8 minutes for repository checks, 25 for build verification
and 35 per emulator. Device jobs wait for a successful build and use KVM
acceleration. Gradle caches are writable on default-branch builds and read-only
on PR/device jobs. Only reports are uploaded, retained for seven days; cancelled
jobs do not upload artifacts. Release APK/AAB/mapping assets live in GitHub Releases.

The wide emulator uses a standard Pixel profile with a resized window and
synthetic separating hinges in tests. It does not establish physical foldable,
speech, haptic or performance behavior. See MIGRATION-VALIDATION.md for limits.
