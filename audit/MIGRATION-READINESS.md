# Quoter migration readiness — 8 October 2026

Production migration is underway following the owner's authorization. The production app now uses Compose and Navigation 3. See MIGRATION-VALIDATION.md for current evidence and remaining release gates.

## Decisions established by the owner

- Full Compose is the intended destination after review of the revised POC.
- Preserve original monochrome palette, bundled fonts, sparse layout and concise labels. Quote-first reader; search/topics in Browse; compact Quotes/Saved switch; Settings in overflow.
- Smooth native gestures and restrained motion; real frame-time measurements before claiming a performance improvement.
- Prefer a natural offline voice when available. Preserve existing explicit voice and rate choices; do not silently download models or substitute cloud speech.
- Always use Android CLI and applicable installed skills. Use physical hardware for OEM, audio and performance checks. Reservations must use free quota only.
- Prepare work autonomously and communicate progress. Ask uncertain product choices or missing requirements through popup questions.

## Prepared environment

| Requirement | Evidence / status |
|---|---|
| Prototype and production | Separate `poc/` reference app; production migration active in `app/` on branch `codex/compose-poc` |
| Android CLI | Installed globally in user PATH, executable `C:/Users/adhik/AppData/AndroidCLI/android.exe`; inherited shell PATH may need refresh, so commands use the absolute executable |
| Installed official skills | Android CLI, XML-to-Compose migration, adaptive, edge-to-edge, navigation, profiling, testing and security skills available; read relevant skills before each phase |
| SDK and prototype baseline | API 37 platform installed; AGP 9.4.1 / Gradle 9.6.0 / Compose BOM 2026.09.00 / Compose compiler plugin 2.4.21 / Activity Compose 1.13.0; Java target 17, min SDK 24 |
| Build host | Available JDK 22 runs Gradle; final production CI should pin a supported toolchain reproducibly |
| Existing runtime | Android 16/API 36 resizable emulator; live read-only side view at localhost:8765 |
| Device authentication | Android CLI Google sign-in succeeded |
| Cloud project | Repository `app/google-services.json` references `quoter-ad7a1` (project number 956961414759); `quoter-323321` is not referenced by this repo |
| Remote catalog | Pixel, Samsung and other device models listed through CLI for `quoter-ad7a1` |
| Remote reservation prerequisite | Device Streaming API is disabled on `quoter-ad7a1`; reservation not created. Browser sign-in/setup and free quota verification are pending |
| Cost guardrail | No billing upgrade, paid session or assumed free capacity. Spark quota is capped; Blaze may charge beyond included minutes. Verify plan/remaining quota before connecting |

Android Device Streaming uses Firebase-backed project quota even when driven by Android CLI. Google documents [the streaming service](https://developer.android.com/studio/run/android-device-streaming) and [30 free minutes per project/month and Blaze overage](https://firebase.google.com/docs/test-lab/usage-quotas-pricing). CLI remote streaming is the planned interaction route; automated Test Lab runs are a separate optional release check.

## Requirements to lock before production edits

| Area | Required behavior / acceptance |
|---|---|
| Identity and upgrade | Same production app ID and signing identity; install upgrade over signed v3 without clearing data. Retain theme, reminder/time, locale, voice and speech rate |
| Data | Quote-first offline startup; validated remote responses; cache survives restart; saved quotes local without accounts; cancellation propagates; stable quote IDs |
| Reminders | Honest permission/channel/scheduling status; repair reboot/update/time-zone recovery; do not claim success after denial; disabled means no reschedule |
| UI parity | Complete inventory of current topic browsing, reader, settings, about, voice/rate, text/image share and reminders before deleting Views/XML |
| Accessibility | Full quote and author reachable at 200% font scale; 48 dp actions; TalkBack order, keyboard focus, contrast, RTL, gesture and three-button navigation |
| Adaptive UI | Phone, landscape, split-screen, tablet/foldable resizing; maximum reading width; inset handling; no dependency on orientation lock |
| State/navigation | Search/topic/position restoration; saved collection recovery; single-shot share events; predictive-back cancellation and progress |
| Speech | Ready/error/play/stop states; installed offline voice fallback; missing engine/data graceful; stop on departure; never auto-speak after recreation; actual listening samples |
| Motion | Finger-following pager, native sheet motion, no decorative entrance delays; animation scale zero; rapid taps/interrupted gestures; measured release frame timing |
| Security/privacy | Narrow exported cache FileProvider; manifest/intent review; Analytics behavior matches policy/privacy/Data safety declarations |
| Platform | API 37 targeted/all-app behavior review; Android 16/17 edge-to-edge and resizing; network/TLS changes; 16 KB runtime and final APK/native alignment |
| Toolchain | Compatible stable production dependencies as one tested set, including Hilt/KSP/Room/DataStore/Firebase/networking/Nav3; version catalog and reproducible CI. POC compatibility does not prove annotation-processor compatibility |
| Release | Signed AAB/generated APKs, upgrade test, Play pre-launch/vitals, staged rollout and rollback thresholds; no Play publication without the owner's release authorization |

## Validation order and cost-efficient physical session

1. Finish emulator journeys, debug/lint/shrunken release, font scale and rotation. Prepare APKs and scripts before consuming physical-device minutes.
2. Enable Device Streaming API, verify free quota, reserve via Android CLI. Prefer an available API 37 Pixel for platform smoke testing, then a midrange/OEM device only if quota remains.
3. Install the revised POC, inspect layout/screens, exercise swipe/Browse/Saved/theme/rotation/Listen, collect logs. Check whether the session can expose audio; do not claim naturalness from voice metadata or silent playback logs.
4. Disconnect and remove the reservation immediately after checks. Record device model/API, duration, results and limitations.
5. The owner reviewed the revised POC and authorized production migration. Validate production regression tests and release artifacts.

## Evidence required later, not satisfied by preparation

Physical release performance baseline versus v3; Android 17 runtime; minimum API 24; notification permission/reboot/Doze delivery; signed data upgrade; 16 KB generated-APK runtime; TalkBack; audible TTS quality; final dependency compatibility and Play declarations. These are release gates, not reasons to delay the design POC.

The annual release should target the current stable SDK after behavior testing, beyond the Play minimum where feasible. Recheck official policies and dependency metadata immediately before signing. Keep dependency/security build checks between annual feature updates, with urgent fixes possible; no recurring automation is created by this plan.
