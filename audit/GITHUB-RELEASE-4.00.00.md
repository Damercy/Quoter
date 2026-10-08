# GitHub release 4.00.00 — 9 October 2026

Destination: `Damercy/Quoter`, `master`, annotated tag `v4.00.00`.
The owner requested GitHub publication of the latest source, screenshots and
release assets. This plan excludes Google Play submission.

## Local verification

| Check | Result |
| --- | --- |
| Signed minified APK and AAB | Passed; version 4.00.00 / code 40000 |
| JVM tests | 10 passed, 0 failures |
| Debug and release lint | Passed |
| Preview screenshot comparisons | 12 passed, 0 failures/errors/skips |
| Release and Play-client tests | 11 passed; GitHub-only path does not invoke Play |
| Store/README gallery | 12 RGB PNGs validated; identical ordered files |
| APK cryptographic verification | Passed APK Signature Scheme v2 |
| AAB cryptographic verification | `jarsigner -verify` passed; signer matches Play upload certificate |
| APK ZIP alignment | `zipalign -c -P 16 4` passed |
| APK and AAB native ELF alignment | Each contains 8 libraries; load segments aligned for 16 KB |
| GitHub Actions references | All five pinned action tags verified against their upstream repositories |

Checksums are attached to the GitHub release in `SHA256SUMS.txt`. Assets:

- `quoter-4.00.00.apk`: 3,022,365 bytes.
- `quoter-4.00.00.aab`: 6,581,090 bytes.
- `quoter-4.00.00-mapping.txt`: 51,435,909 bytes.

The local Gradle build uses JDK 22.0.1 with Java 17 source/bytecode targets.
Gradle is the supporting build/signing tool; Android CLI handles supported
SDK/device operations. Native alignment/signature checks use SDK tools because
Android CLI does not provide those archive checks. The preview task reused its
unchanged successful inputs; its XML records all twelve comparisons passing.

## Limits and credentials

Mixed-use functional emulator journeys are documented in RANDOM-JOURNEYS.md.
Physical-device audio quality, haptics, performance, gesture navigation,
Play-distributed signed upgrades and Play pre-launch checks remain outstanding.
Native ELF/ZIP alignment does not prove 16 KB physical-device runtime behavior.
GitHub Actions results are available on the repository's Actions page after push.

Google holds a separate app signing key. The upload-key-signed GitHub APK cannot
update a Play-installed copy. Existing Play users should obtain updates from
Play when that release is separately authorized and approved.

The keystore is removed from the current Git tree and remains secured locally;
its earlier public history is unchanged. No private credentials or signing
passwords are included in the release source or GitHub Actions secrets. The
owner chose to retain the existing upload certificate. Cloning is sufficient
for source/build setup once SDK/JDK dependencies are installed; publishing
requires credentials provisioned independently.
