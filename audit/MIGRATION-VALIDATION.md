# Production migration validation — 8 October 2026

The sections below record local migration checks. The final signed GitHub release
and its current checks are recorded in [GITHUB-RELEASE-4.00.00.md](GITHUB-RELEASE-4.00.00.md).
Play submission remains pending. Evidence log links refer to local development
records; raw device logs and captures are intentionally excluded from Git.

## Implemented

- Full Compose single Activity, Navigation 3, original fonts and monochrome colors. Legacy Fragments, View layouts, obsolete networking and voice implementations removed.
- Offline-first Room database with 3,079 distinct attributed quotes, daily bounded conditional JSON refresh, stable IDs, saved quotes and local author/text/topic search. See QUOTE-SOURCES.md for licenses, pins and limitations.
- Existing DataStore preference file retained; system/light/dark theme, reminder/time, locale, offline voice and rate. Central reminder scheduler with permission/channel awareness and boot/update/time recovery.
- Direct system sharing of a full-text quote PNG plus attributed text through a narrow private cache FileProvider; no app share-choice sheet or replay after restoration. Lifecycle-managed installed offline TTS.
- Restrained monochrome glass controls with blur/lens on supported devices, fallback on older/low-RAM devices, a wavy native Material 3 speech slider and spring transitions. Motion scale zero is respected.
- Optional Browse pane via the stable adaptive Navigation 3 scene strategy. Phone Browse sheet remains. Single-pane reader/settings/sheets avoid separating hinges; controls wrap and quote content scrolls at large text sizes.
- Settings entry replaces any secondary route; Browse opens idempotently; root is retained. Main Activity uses `singleTop`.
- Stable production dependency set, target/compile API 37, AGP 9.4.1, Gradle 9.6.0, Java 17 target. CI configuration builds/tests the minimum API and an API 37 foldable; it has not yet run on GitHub.

## Evidence already completed

| Check | Result |
|---|---|
| Production debug compilation | Passed with glass and adaptive code |
| JVM tests | 10 passed, including 4 hinge geometry, 3 reminder time and 2 review launch schedule tests |
| Earlier production UI regression | 3 passed: real-touch Save/Unsave, author search selection, Settings/state restoration and share non-replay |
| Repository instrumented tests | 5 passed: local/offline data, invalid response fallback, conditional refresh and persistence semantics |
| Debug/release lint | 0 errors; 23 warnings, primarily retained unused resources, monochrome launcher metadata and KTX suggestions |
| Minified release APK and AAB | Latest refinement built successfully; APK 3,005,853 bytes, AAB 6,545,706 bytes |
| Release APK ZIP alignment | `zipalign -c -P 16 -v 4` passed |
| Bundle native ELF alignment | All 8 libraries across 4 ABIs have PT_LOAD alignment of at least 16 KB; `scripts/check-native-alignment.mjs` |

The owner's existing `Resizable_Experimental_API_VanillaIceCream` is the only local AVD. The extra fold AVD was removed through Android CLI. Wiping and cold booting initially cleared the System UI dialog, but it returned at 21:36 with an edge-swipe monitor timeout and guest graphics-service CPU saturation. The owner then approved three-button navigation. The same AVD now uses software graphics and was cold booted through Android CLI without another data wipe. This is a workaround, not a proven permanent emulator fix; gesture and hardware performance evidence must be revisited separately.

The System UI dialog appeared again during boot with the continuous browser screenshot mirror running. That mirror originally captured every 900 ms even without a viewer. It now starts paused, captures only on requests at most once every four seconds and is paused during boot/build/device automation. After closing the boot-time dialog and pausing the mirror, all seven final UI/share checks and both final fold checks passed without the dialog interrupting them. The native emulator remains interactive; the browser mirror was resumed at the lower rate afterward. Logs retain the failed attempts rather than hiding them.

The latest UI revision uses compact 24 dp Settings gutters and 24 dp section spacing, icon-only System/Light/Dark selection, matching wavy sliders that animate while idle, and no Reviews section. Browse uses a rounded monochrome search field and capsule filters. Material typography uses the app's original font throughout. Loading uses native dots instead of a circular indicator or Lottie. The splash icon zooms with a bounce before fading into the app. Opaque clipped navigation surfaces prevent reader text showing through Settings; headers, colors, controls and icons have restrained motion. Zero animation scale is respected, and idle waves stop when the screen is not started.

Native Play review attempts retain the agreed launch sequence of 2, 4, 8, 16 and subsequent powers of two; the former opt-out setting is no longer shown. Stars cannot be prefilled and submission completion is unavailable through Play. Actual review display requires a Play-enabled distribution test.

Latest debug, minified release APK/AAB and lint builds passed (`refinement-final-build.log`). Native ELF and ZIP 16 KB alignment checks passed. Nine preview screenshots passed comparison and were visually reviewed, including Settings light/dark/large text and reader phone/landscape/unfolded/tablet/desktop/large text. Static previews deliberately disable motion and advanced glass rendering.

Two unfolded/tabletop instrumented checks passed on the retained resizable API 36 emulator: Browse selection and closing, plus reader/share-sheet hinge avoidance. All six latest phone UI tests passed, including the fix retaining an unfinished transparency adjustment across recreation (`resizable-final-ui-tests.log`). Five repository tests passed in the preceding run. Device wipe cleared local app/emulator data; no repository or user production data was changed.

Latest refinement evidence supersedes those earlier UI checks: `refinement-final-phone-tests.log` passed six UI tests plus the image/text/URI-permission export check; `refinement-final-fold-tests.log` passed Browse selection and reader/reminder-sheet hinge checks; `refinement-final-preview-tests.log` passed all nine current screenshot comparisons. The preceding full run also passed the five repository tests. Actual system-share preview and live Settings wave were visually inspected in `refinement-native-share.png` and `refinement-live-settings.png`. Light and Dark icon choices were exercised, then the owner's previous Dark selection was restored. Only the phone-sized original emulator is left open with the actual 3,079-quote collection.

## Release gates still requiring evidence

- Predictive back gesture cancellation and sustained hardware performance; current phone/fold emulator journeys and large-text previews are covered below.
- API 24 actual runtime; GitHub CI execution.
- Real-device TTS listening and release performance/frame timing versus v3. Remote testing is deferred by the owner; no physical device was reserved.
- Signed installed-v3 data upgrade using the owner's production signing identity; minimum/16 KB runtime, real notification delivery/reboot/Doze and OEM behavior.
- Final Play privacy/Data safety declarations, pre-launch/vitals and staged rollout. No emulator-only result proves these gates.

The QA APK uses the local debug key only. The release bundle remains unsigned. Do not upload either as a production release without the proper signing/release process.

## Apple-inspired Settings and chrome refinement (latest)

This revision supersedes the icon-only theme picker and Quotes/Saved pills described above. Settings now uses grouped monochrome rows, 16 dp gutters, Light/Dark screen previews with Automatic, and the existing living wavy sliders. Value rows stack at large font scales or in narrow panes. Quotes and Saved are plain text tabs with an animated marker. Settings' large title scrolls away while a compact title fades in; Browse contracts its title and retains search over the live result backdrop. Long reader content can scroll under the chrome. Toolbar shadow/highlight are explicitly disabled, while the unscrolled header allows floating control shadows to extend without clipping.

Native haptic feedback is wired to icon/text taps, Settings rows/toggles, appearance choices, Browse selections, slider step crossings, and completed swipe paging. Transparency ticks are quantized to 5% intervals. Button-driven paging suppresses a duplicate settle tick. Feedback uses platform settings and requires no vibration permission. Actual haptic feel is unverified on physical hardware.

| Current evidence | Result |
| --- | --- |
| Debug APK, release APK/AAB, unit tests and lint | Passed: apple-final-validation.log; 10 JVM tests; lint 0 errors |
| Phone UI plus image/text export | 8 passed: apple-final-phone-ui-tests.log |
| Unfolded Browse and tabletop reader/reminder hinge checks | 2 passed: apple-final-fold-tests.log, same retained API 36 resizable emulator |
| Preview comparisons | 9 passed; refreshed intentional design references and visually reviewed light/dark/large-text Settings and six reader sizes |
| Release native ELF and ZIP alignment | 8 libraries passed ELF checks; APK ZIP alignment passed for 16 KB |
| Live light-mode reader | Visually checked: apple-final-reader-light.png; previous cropped rectangular shadow strip removed |
| Live scrolled Settings and Browse | Visually checked: apple-settings-scrolled.png, apple-settings-dark.png, apple-browse-scrolled.png |

The initial full phone run passed the five repository checks and app-context check, skipped two fold assumptions on the phone preset, and failed one old combined-label assertion. Its log is retained in apple-phone-old-label-failure.log. Correcting the test's label to the new separate percentage produced the eight-test successful UI/export rerun. This is a test assertion correction, not a data-store workaround.

Only Resizable_Experimental_API_VanillaIceCream remains installed. It was returned to phone preset 0, Dark was restored, the actual 3,079-quote reader was reopened, and the browser mirror resumed at its bounded capture rate. No System UI dialog appeared during these current journeys; the earlier renderer/navigation workaround remains provisional. Gesture validation, physical performance/TTS/haptics, signed v3 upgrade and the other release gates above remain separate.


## Motion, collection and Browse refinement — 9 October 2026 (current)

This revision supersedes the earlier search-to-Close icon treatment. The phone sheet has no top-right Close control; the background Browse button is hidden without shifting Settings. Native handle, Back and scrim dismissal remain available; wide supporting panes retain their Close control. Browse category markers reserve constant width, the collapsing header preserves line height, filtering resets the list to the top and departing rows no longer animate beneath changing hit areas. Quotes and Saved keep independent saveable quote IDs. Navigation is 280 ms forward / 240 ms back, collection changes 200 ms, and control/counter springs are firmer. Saved, empty search and offline voice lists use original monochrome vector illustrations with brief guidance and appropriate actions.

The other motion/detail changes include direct image/text sharing with the Play link, the rolling counter and removal of Next, symmetric speaker/stop icons, quiet tap sounds and explicit ripple suppression in glass mode, drawing-only living wave tracks with a wider thumb, immediately exiting native splash, a fully expanded simplified-clock reminder sheet with compact input fallback, and Android-native image notification preview/style.

| Current validation | Result and evidence |
| --- | --- |
| Debug APK, Android test APK, JVM tests and lint | Passed: evidence/refine3-build.log; 10 JVM tests; lint 0 errors / 26 warnings |
| Phone UI and image/text/temporary-URI export | 9 passed: evidence/refine3-phone-tests.log; includes independent collection positions after repeated switches and saved-state restoration |
| Real-app random mixed-use journeys | 3 seeds passed, 45 steps total: evidence/refine3-random-tests.log; full sequences in evidence/refine3-random-steps.log and RANDOM-JOURNEYS.md |
| Unfolded/tabletop panes and hinge checks | 3 passed: evidence/refine3-fold-final.log; Settings replaces Browse, toolbar position stays fixed, repeated Settings does not duplicate the route |
| Visual comparisons | 12 passed: evidence/refine3-preview-tests.log and app/build/test-results/validateDebugScreenshotTest/TEST-preview-screenshot-test-engine.xml; six reader sizes, three Settings variants and three empty-state variants |
| Release APK and AAB | Passed: evidence/refine3-release-previews-build.log; unsigned APK 3,005,981 bytes, AAB 6,569,609 bytes |
| 16 KB packaging | Eight native libraries passed ELF checks in both APK and AAB; release APK ZIP verification successful: evidence/refine3-native-apk-alignment.log, evidence/refine3-native-aab-alignment.log, evidence/refine3-zip-alignment.log |
| Live phone visual checks | evidence/refine3-light-reader.png, evidence/refine3-browse.png, evidence/refine3-search-empty.png and evidence/refine3-reminder.png; continuous Browse surface, absent redundant cross controls, empty artwork and fully visible clock/actions |

The first three-fold-test run passed Browse and tabletop checks but the new Settings test matched both its fixture quote and the notification preview, which used the same text. The retained failed log is evidence/refine3-fold-tests.log. Changing the fixture to a distinct quote produced the full three-test successful rerun; no production workaround or weakened assertion was introduced.

The native emulator is back in phone preset 0 with the real 3,079-quote reader. The owner's original Automatic appearance setting was restored after inspecting Light; transparency 75%, speech speed 1.00× and reminder time 8:40 were retained, and the time sheet was cancelled. The browser mirror resumed. Only the existing resizable emulator remains; no new AVD or remote reservation was created. No System UI interruption occurred during these journeys, but the software-renderer/three-button workaround remains provisional.

Settings A/B performance evidence in MOTION-REFINEMENT.md measured the earlier rendering optimization, not the final entire app. The software-rendered emulator remains slow at drawing; shorter transition durations do not prove physical frame smoothness or lower power usage. Physical performance, voice/haptic quality, gesture navigation, API 24/37 runtime, signed installed-v3 upgrade and Play release gates remain as described above. No production signing or publishing was performed.

## Ripple removal and Share shadow fix — current

All Quoter ripple drawing is now disabled in both glass and plain modes. The theme supplies null Material ripple configuration and a quiet Foundation indication; icon buttons explicitly use no indication. Haptics, press springs, vector motion and slider feedback remain.

Share's temporary disabled opacity previously caused an automatically allocated, bounded alpha layer to clip the glass shadow. ModulateAlpha with clip=false preserves the shadow outside the button while dimming it. Both enabled and visibly disabled captures were reviewed: evidence/share-shadow-enabled.png and evidence/share-shadow-disabled.png.

Debug/test builds and lint passed (0 errors / 26 warnings) in evidence/ripple-shadow-build.log. The nine UI/export checks passed within evidence/ripple-shadow-tests.log; its initial shadow capture failed because Compose's forced-redraw screenshot timed out. The replacement direct-compositor capture was strengthened to reject blank/stale frames and wait for the actual enabled/disabled icon appearance. Both rendering checks passed in evidence/ripple-shadow-render-verified.log. The plain-mode test holds icon and text controls and compares their rendered pixels to reject ripple painting. The shadow check samples outside the touch bounds while the actual glyph is visibly dimmed.

Twelve unchanged visual comparisons passed in evidence/ripple-shadow-capture-build.log. Current release APK/AAB builds passed in evidence/ripple-shadow-release.log; APK 3,005,981 bytes, AAB 6,569,220 bytes. Both archives' eight native libraries passed ELF alignment checks; APK ZIP alignment verification passed (ripple-shadow-native-apk.log, ripple-shadow-native-aab.log, ripple-shadow-zipalign.log). Release artifacts remain unsigned.

Android CLI installed the updated debug APK on the sole retained emulator. A final real-app Share tap opened Android's image/text chooser, then Back returned to the same 1 / 3079 reader without choosing a recipient. Layout evidence: ripple-shadow-reader-layout.json, ripple-shadow-chooser-layout.json and ripple-shadow-return-layout.json. The phone emulator is left available for manual use and the browser mirror is resumed. Existing preferences were retained. Android system UI outside Quoter is owned by the operating system.
