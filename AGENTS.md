# Working on Quoter

## Project map

- `app/src/main/java/.../presentation/compose`: reader, Browse, Settings, speech and sharing UI.
- `app/src/main/java/.../data`: offline Room quotes, parsing, refresh and DataStore preferences.
- `app/src/main/java/.../domain/broadcast`: reminder scheduling and delivery.
- `app/src/main/assets`: bundled quotes, source provenance, licenses and vector source icons.
- `app/src/test`, `app/src/androidTest`, `app/src/screenshotTest`: JVM, device and preview checks.
- `play-store`: listing copy, ordered original screenshots and release manifests.
- `scripts`: imports, image presentation, verification and publishing tools.

Read [design direction](audit/DESIGN-DIRECTION.md) before UI changes and
[validation](audit/MIGRATION-VALIDATION.md) before making release-readiness claims.
Treat older exploration documents as history; these rules describe current requirements.

## Build and tools

Use the official `android` CLI and relevant installed Android skills for docs,
SDK/device management, installation, layout inspection and supported interactions.
Read applicable skills and their referenced workflows first. Gradle and ADB
support operations required by skills or unavailable in the CLI; record material fallbacks.

Use JDK 17+, SDK platform 37, `JAVA_HOME`, `ANDROID_HOME` and the Gradle wrapper.
Use `./gradlew.bat` on Windows or `./gradlew` on Unix.

```sh
./gradlew :app:testDebugUnitTest :app:lintDebug :app:lintRelease :app:bundleRelease
./gradlew :app:validateDebugScreenshotTest
node scripts/check-native-alignment.mjs app/build/outputs/bundle/release/app-release.aab
```

Run relevant checks for each change. UI/state changes also need targeted device
regressions; use reproducible seeded mixed-use journeys for broader coverage.
Do not update screenshot baselines merely to silence a regression.

## Design and interaction

- Preserve the monochrome palette, bundled fonts, bold typography, sparse reader
  and progressive disclosure. Avoid new accent palettes, promotional headings and card feeds.
- Use plain Quotes/Saved tabs with one spring underline, independent saveable
  reading positions, a compact rolling counter and no Next icon.
- Anchor Browse and Settings at the top right. Supporting panes are mutually
  exclusive on wide windows; retain one reader on compact windows. Prevent
  duplicate destinations and keep content, controls and sheets clear of hinges.
- Browse uses plain categories, stable hit targets/header height, a subdued search
  field and one continuous surface. Hide its background control while the phone
  sheet is open without shifting Settings. Phone sheets use native handle, Back
  and scrim dismissal; wide panes retain Close.
- Settings uses grouped sections with 16 dp gutters, Light/Dark screen previews
  and Automatic, adaptive large-text rows and a clock with compact-input fallback.
  Use sheets rather than app dialogs or overflow/About/Reviews destinations.
- Glass is optional, persistent and adjustable. Use real backdrop frost/lens
  effects where supported and accessible fallbacks elsewhere. Keep navigation
  surfaces opaque; avoid a rectangular toolbar shadow. Dim disabled Share
  without clipping its overflowing glass shadow.
- Disable ripples in glass and plain modes, including Material and Foundation
  targets. Retain accessibility targets, restrained haptics, animated vectors,
  short spring motion and microanimations. Respect reduced motion and device
  haptic settings; stop idle waves when hidden or not started.
- Keep the platform splash with immediate exit. Finish sheet hide animations on
  selection/close. Empty states use simple monochrome vectors and concise actions.

## Data and platform behavior

- Keep bundled offline quotes and bounded static JSON refresh with provenance,
  licenses, stable IDs, saved data and invalid-response recovery. Avoid restoring
  a dependency on an unavailable/rate-limited quote REST service.
- Preserve preference/data upgrades. Speech uses the device-selected engine and
  suitable installed offline voices, preserving explicit choices. Evaluate
  locale/quality metadata with real listening; metadata does not prove natural
  or neural speech. Cloud speech is not the default.
- Share opens Android's chooser directly with PNG and attributed text including
  “Shared with Quoter” and the verified Play link. Do not add an app choice sheet
  or replay sharing after recreation. Tests cancel sharing without recipients.
- Reminders handle permission/channel denial, disabled state, reboot and time
  changes. Successful scheduling does not prove delivery.
- Attempt native Play review on launches 2, 4, 8, 16 and subsequent powers of two.
  Google controls quotas; do not prefill stars, detect completion or claim an
  attempted prompt proves a submitted review.

## Device validation

Keep the native emulator available for manual testing. Announce automation before
controlling it and leave it untouched between runs. Reuse the workspace's existing
configured AVD for phone/fold/tablet checks; do not create extra local AVDs.
Hosted CI may create disposable emulators. Consult
[local device notes](audit/LOCAL-DEVICE-NOTES.md) for workstation configuration.

Document renderer/navigation workarounds and their validation limits; they do
not establish gesture or physical performance. Pause any configured screenshot
mirror during boot, builds and automation; resume it for manual viewing. Do not
start a mirror that is absent.

Hardware/OEM behavior, performance, haptic feel and audible TTS require physical
validation through `android device remote`. Check authentication, models, session
constraints and verified free quota first; release reservations afterward.
Record deferred checks in validation notes. Do not spend paid quota without
authorization or describe emulator results as physical-device evidence.

## Documentation, CI and releases

Keep README concise and detailed procedures in `audit`. Keep this file current
and declarative; remove obsolete rules and conversational history. Ask about
unresolved product decisions and report meaningful implementation progress.

Keep planned work in [ROADMAP.md](ROADMAP.md) and the public issue ledger.
Follow [CONTRIBUTING.md](CONTRIBUTING.md) for requests and triage. Link changes to
their issue, update completion checklists and release notes, and preserve issue
history. Milestone assignment does not imply implementation or a published release.

Original Play screenshots remain RGB PNGs. Reuse them in vector device wrappers;
do not synthesize app UI or let image bots alter reviewed assets/baselines.
Regenerate presentation with:

```sh
node scripts/frame-store-gallery.mjs
python scripts/sync-store-gallery.py
```

CI runs lightweight source/media/workflow checks for all changes, gates Android
builds/device jobs on relevant paths, cancels superseded runs and bounds job
time/artifact retention. Preserve minimum-API and wide-window coverage; use
manually dispatched full checks when needed. Never execute publishing from CI.

Publish only to explicitly authorized destinations. Use the reviewed, hash-frozen
plan and journal in [release workflow](audit/RELEASE-WORKFLOW.md). GitHub-only
publication excludes Play. Keep credentials, keystores and passwords outside Git
and provision them privately on new machines. Preserve release history and
published tags. Play submission is distinct from approval/public availability;
the upload-signed GitHub APK cannot update a Google-signed Play installation.
