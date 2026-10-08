# Quoter 4.00.00

Quoter's reader now uses Jetpack Compose while keeping its monochrome palette,
bundled fonts and uncluttered quote page.

- Read the bundled collection offline, with optional static quote refresh.
- Search words and authors, browse topics, and save favourites with independent reading positions.
- Listen with compatible installed on-device voices and adjustable speech speed.
- Share a PNG quote image and attributed text directly through Android's chooser.
- Set daily reminders and choose text or image-style notifications.
- Choose Light, Dark or Automatic appearance and adjustable glass controls.
- Keep Browse or Settings beside the reader on unfolded and tablet windows.
- Enjoy restrained motion, haptics, accessible controls and reduced-motion support.

The repository includes the shared phone and large-screen screenshot gallery,
updated setup instructions, release tooling, CI verification and prepared Play
listing content.

## Downloads

- `quoter-4.00.00.apk`: signed APK for independent installation.
- `quoter-4.00.00.aab`: signed Android App Bundle for Google Play publishing.
- `quoter-4.00.00-mapping.txt`: R8 mapping for release crash diagnostics.
- `SHA256SUMS.txt`: SHA-256 checksums for these three assets.

Google Play signs distributed APKs with a different certificate. This GitHub APK
cannot update an existing Play-installed copy; use Google Play for that update.
The same bundle, updated description and shared screenshot set are published on
Google Play. Play Console confirmed publication on 9 October 2026; store
propagation can vary.

## Verification

Signed release builds, unit tests, debug/release lint, twelve screenshot
comparisons, release-client tests, signature verification and native-library
16 KB alignment checks passed locally. Reproducible mixed-use emulator journeys
are documented in `audit/RANDOM-JOURNEYS.md`.

Physical-device speech quality, haptic feel, performance and gesture navigation
remain unverified. Local interaction checks used the retained API 36 emulator
with three-button navigation; these do not establish physical-device behavior.
