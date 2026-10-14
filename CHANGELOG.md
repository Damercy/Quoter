# Changelog

## 4.00.01 — 2026-10-15

- Restore the selected quote before reporting a page change.
- Dismiss search focus on topic selection and keep Back in the Browse sheet.
- Serialize saved-state changes so repeated taps count and persist once.
- Ignore stale speech callbacks and measure listening only when speech starts or completes.
- Measure settled quote views, share chooser openings, reminders and review requests without quote content.
- Make local Firebase configuration optional and keep publishing inputs private.
- Bound device verification and retain logs for failed or stalled test runs.

## 4.00.00 — 2026-10-09

- Migrate the reader to Compose while preserving Quoter's monochrome theme and fonts.
- Bundle an offline quote database with optional static refresh; remove the unavailable REST API dependency.
- Add searchable topic/author browsing and saved quotes with independent reading positions.
- Support Browse or Settings beside the reader on unfolded and tablet windows.
- Add grouped appearance, voice and reminder settings, adjustable glass, reduced motion and haptics.
- Share attributed PNG quote images directly through the Android system chooser.
- Retain on-device speech, daily reminders and launch-based native Play review attempts.
- Add the shared phone/large-screen gallery, CI verification, signing/release scripts and prepared Play listing copy.
- Remove the tracked keystore from the current tree; provision publishing credentials separately.
