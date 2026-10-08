# Quoter revised design direction

8 October 2026. The owner rejected the initial Compose POC's visual identity. This document supersedes its ivory/teal palette, editorial marketing headings and card-feed layout. It is a concrete design recommendation for the next POC iteration; the existing prototype has not yet been restyled.

## Preserve the identity

Existing resources define black/white primary and secondary colors, inverted in night mode. Existing screenshots show a near-black reading surface, bold bundled font, sparse monochrome icons, large areas of empty space, left-aligned quote text and a right-aligned author. Preserve the actual theme surface colors from the resolved existing theme; do not assume a pure-black surface simply because the primary token is black. Keep `main_bold.ttf` and `main_regular.ttf`.

Modernization means reliable interaction, precise spacing, readable text and smooth native motion. It does not require an accent color or a new brand voice. Use the current logo and concise labels such as Quotes, Saved, Browse, Settings, Copy and Listen.

## References inspected

1. [Minimal Quotes App Concept — Kishore, Project365](https://dribbble.com/shots/4563805-Minimal-Quotes-App-Concept-Day-127-365-Project365). The 2018 design presents a single monochrome quote with extensive negative space, secondary author text and small save/share actions. Borrow its hierarchy and restraint; keep Quoter's heavier font and accessible contrast/targets rather than copying the reference's small, pale labels. Its published attachment is named `07052018-design.png`.
2. [Quotes App Interaction — tubik](https://dribbble.com/shots/10836526-Quotes-App-Interaction). A designer-attributed quote-app interaction concept published before the current generative UI trend (also discussed in a 2020 article). Its My Quotes/topic collection structure is useful for assessing focused navigation. Quoter's palette and typography remain authoritative; elaborate visual styling is not a requirement.

These are published designer references, not generated mockups. A Dribbble concept is visual evidence, not proof of usability or production behavior. The recommendation below is our design judgment based on those references and Quoter's existing UI.

## Recommended final composition

| Surface | Composition | Minimalism rule |
|---|---|---|
| Main / Quotes | Start with a readable quote. Small Quotes / Saved text switch near the top; Browse and overflow actions; existing large bold quote, author aligned right; compact share/listen/save actions and position indicator | One quote dominates; no promotional heading, hero card, stacked feed or repeated quote |
| Browse | Open a native sheet from Browse. Search field, a short horizontal topic-chip row and an All topics view. Optionally retain the existing bold topic-wheel presentation as a browsing mode, with an accessible list alternative | Chips/search appear only when browsing; show current topic succinctly on the reader |
| Saved | Same two-way switch and reading language; plain typographic rows separated by space or thin rules, then open the same reader | No filled cards; visible empty-state instruction only when empty |
| Settings | Overflow opens Settings; simple rows for theme, reminder, voice and speed; detailed choices in native dialogs/sheets | No permanent Settings tab and no large language-chip wall |
| Reader actions | Preserve the current placement/language of Share and Listen; add Save. Copy and export style choices can live in overflow or share sheet | Visible glyphs stay visually small while interactive bounds are at least 48 dp and labeled for accessibility |

The initial bottom bar proved that collections are useful, but a permanent three-item navigation bar consumes reading space for an infrequent Settings visit. Prefer a small two-way text switch for Quotes/Saved and keep Settings in overflow. This is the chosen recommendation, not three competing themes.

Opening Browse and returning preserves topic, query, quote ID and scroll position. Choosing a topic scopes subsequent paging. The reader and collection views share typography and spacing. Long quotes scroll vertically when needed; horizontal swipes change quotes. Do not reduce system text scaling to force a composition to fit.

## Motion direction

- Keep content attached to the finger during paging, with native spring settling. Avoid large zooms, dramatic rotation, bouncing decoration and staggered entrances.
- Use short, consistent transitions when opening Browse/Settings and changing collections. Let the native sheet animate its own drag/dismiss behavior.
- Save feedback is a restrained icon/state change and optional subtle haptic. Playback shows an honest loading/playing/stopped state.
- Respect animator duration scaling. Validate interrupted gestures, rapid taps and predictive-back cancellation.
- Measure startup and frame timing on physical devices in profileable release builds before claiming improved smoothness. Visual polish and low frame latency are separate acceptance criteria.

## TTS default policy

1. Preserve a user's explicit voice/locale/rate choice when migrating existing settings.
2. Initialize the device-selected TTS engine instead of hardcoding Google. Enumerate `getVoices()` after initialization and select a locale suitable for the quote language and user's accent preference.
3. Prefer a ready, installed voice that does not require a network connection. Prefer higher engine-reported quality among suitable candidates, using latency and the engine default as tie breakers. Validate missing voice data and synthesis failure; keep a reliable offline fallback.
4. Listen to representative short/long quotes, names and punctuation on actual devices. `Voice.getQuality()` is an engine-provided signal, not a standardized naturalness score across engines. Android does not expose a universal flag guaranteeing that a voice uses a neural/AI model. Do not infer it from names or silently download a large model.
5. If an engine offers a demonstrably smoother natural/neural offline voice, prefer it as the automatic default. Offer a brief user-initiated preview and an override. Network voices may be an explicit option with clear availability; they are not the default for this offline-friendly app.

References: [Voice quality, latency and network requirement](https://developer.android.com/reference/android/speech/tts/Voice), [TextToSpeech engine/voice APIs](https://developer.android.com/reference/android/speech/tts/TextToSpeech).

## Android CLI and hardware workflow

Use relevant installed official Android skills for every implementation phase, with Android CLI docs lookup and device operations. Use Gradle/ADB where the skill calls for them or the CLI has a demonstrated limitation (the existing multi-display screencapture issue, for example).

Updated 8 October: Android CLI sign-in succeeded and the device catalog is available for the repo's Firebase project `quoter-ad7a1`. Its Device Streaming API is disabled, so no reservation has been created. Browser sign-in/setup and free quota verification remain pending. Use only verified free quota as requested by the owner. TTS audio and hardware frame timing are priority physical-device checks; a remote session is only suitable for listening if it exposes audio capture/streaming. Otherwise use a connected physical device for audible verification.

## Next implementation artifact

Revise the standalone POC into this monochrome, quote-first composition, preserving the working search/save/state logic. Verify native Compose rendering against the current app's screenshots, then test font scale, gesture behavior, search/saved recovery and theme restoration. Keep a minimal visual diff to the current reader while introducing the Browse sheet and collection switch. Update the full migration plan with the resulting implementation evidence before production replacement.

## Settings and chrome refinement

Appearance follows Apple's Light/Dark previews and Automatic pattern, with original Quoter screen artwork rather than platform-restricted Apple symbols: https://support.apple.com/en-by/guide/iphone/iph60ba71065/ios . The grouped rows retain Quoter's monochrome palette and bundled typeface. Values stack below labels in narrow panes or large-text mode.

Apple's materials guidance informs content scrolling beneath a readable navigation edge: https://developer.apple.com/design/human-interface-guidelines/materials . Settings' large title scrolls away and a compact title fades in. Browse contracts its title and keeps search/filters over a live content backdrop. Quotes/Saved use plain text controls with an animated selection marker. Toolbar shadow is explicitly disabled; floating control shadows have space to extend. No Apple renderer or assets are copied.

Platform view haptics provide short tap feedback and soft selection ticks. Continuous transparency changes are quantized to 5% boundaries; speech speed ticks at discrete steps. Restoring state and idle animations do not generate haptics. These obey the device haptic settings and require no vibration permission. A physical device is required to judge feel.
