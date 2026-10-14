# Quoter 4.00.01

Saving handles repeated taps more reliably. Listening ignores delayed callbacks
from a previous speech session. Quote positions survive restoration more reliably,
and Back closes Browse consistently.

Aggregate engagement events distinguish settled quote views, saved-state changes,
speech playback and opening the share chooser. Events contain no quote text,
author names or account identifiers. A review request does not count as a rating.

Local Firebase configuration is optional for development and stays outside Git.
Device verification retains diagnostic logs and stops stalled runs.

## Downloads

- `quoter-4.00.01.apk`: signed APK for independent installation.
- `quoter-4.00.01.aab`: signed Android App Bundle for Google Play.
- `quoter-4.00.01-mapping.txt`: release crash mapping.
- `SHA256SUMS.txt`: checksums for these assets.

Use Google Play to update a Play-installed copy. The independent APK uses the
upload certificate, which differs from Google's app signing certificate.
