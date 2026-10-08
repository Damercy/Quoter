# Random mixed-use journeys — 9 October 2026

Run on the existing API 36 resizable emulator in phone preset 0 through UI Automator 2.4.0. Android CLI deployed the app/test APK; ADB instrumentation ran the tests. All three seeds passed; 45 steps total. Every shuffled plan contains the eleven scenarios and four additional random repeats.

| Seed | Steps | Result |
| --- | --- | --- |
| 42 | 15 | Passed |
| 108 | 15 | Passed |
| 9271 | 15 | Passed |

Scenarios: author selection; empty search and clearing; save, switch and restore; mixed category selection; Light/Dark selection; glass toggle persisted through Activity replacement; reminder-time cancellation; Android image/text sharing cancellation; rotation; background/resume; horizontal quote swiping. Each step asserts return to Quoter and absence of a duplicate Browse destination. Category steps also verify the phone sheet has no redundant Close button. Preferences and original saved state are restored; no share recipient is chosen and review requests are suppressed only for testing.

Separate Compose regression checks preserve the Quotes position after displaying a different Saved quote, repeated tab switches and saved-state restoration. Earlier trial failures are retained in evidence logs; these included an empty-result accessibility occlusion fixed in Browse and keyboard/transition synchronization problems corrected in the test harness.

Evidence: [instrumentation result](evidence/refine3-random-tests.log), [step log](evidence/refine3-random-steps.log), [phone UI/export regression](evidence/refine3-phone-tests.log).

This is emulator functional evidence. Hardware animation timing, actual haptic feel, TTS voice quality and gesture-navigation behavior remain unverified.
