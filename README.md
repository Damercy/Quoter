<p align="center"><strong>Quoter</strong> — a minimalist Android app for reading, saving and listening to quotes.</p>
<p align="center">
  <a href="https://play.google.com/store/apps/details?id=com.dayaonweb.quoter">Google Play</a> ·
  <a href="https://github.com/Damercy/Quoter/releases/latest">Download APK</a> ·
  <a href="CHANGELOG.md">Changelog</a> ·
  <a href="https://github.com/Damercy/Quoter/actions/workflows/android.yml">CI</a>
</p>

Read offline, browse topics and authors, save favourites, listen with installed
on-device voices, set daily reminders and share attributed quote images.
Monochrome Light/Dark themes, adjustable glass and supporting panes adapt the
reader to phone, tablet and unfolded windows.

## Screenshots

<!-- QUOTER-SCREENSHOTS:START -->
<table>
<tr>
<td align="center"><a href="play-store/screenshots/phone/01-reader.png"><img src="screenshots/frames/phone-01-reader.svg" width="140" alt="Minimal monochrome quote reader with listening, saving and sharing controls" /></a><br/><sub>Reader</sub></td>
<td align="center"><a href="play-store/screenshots/phone/02-browse.png"><img src="screenshots/frames/phone-02-browse.svg" width="140" alt="Browse quotes, search by words or author, and filter by topic" /></a><br/><sub>Browse</sub></td>
<td align="center"><a href="play-store/screenshots/phone/03-saved.png"><img src="screenshots/frames/phone-03-saved.svg" width="140" alt="Saved quotes with a separate reading position and selected bookmark" /></a><br/><sub>Saved</sub></td>
<td align="center"><a href="play-store/screenshots/phone/04-appearance.png"><img src="screenshots/frames/phone-04-appearance.svg" width="140" alt="Light, Dark, Automatic and adjustable glass appearance settings" /></a><br/><sub>Appearance</sub></td>
</tr>
</table>

<details>
<summary>Voice, reminders, sharing and dark mode</summary>

<table>
<tr>
<td align="center"><a href="play-store/screenshots/phone/05-voice-and-reminders.png"><img src="screenshots/frames/phone-05-voice-and-reminders.svg" width="140" alt="On-device voice and speech speed settings with daily notification options" /></a><br/><sub>Voice and reminders</sub></td>
<td align="center"><a href="play-store/screenshots/phone/06-reminder-clock.png"><img src="screenshots/frames/phone-06-reminder-clock.svg" width="140" alt="Native reminder time sheet with an analogue clock" /></a><br/><sub>Reminder clock</sub></td>
<td align="center"><a href="play-store/screenshots/phone/07-image-sharing.png"><img src="screenshots/frames/phone-07-image-sharing.svg" width="140" alt="Android sharing menu with quote image, author and Quoter attribution" /></a><br/><sub>Image sharing</sub></td>
<td align="center"><a href="play-store/screenshots/phone/08-dark-reader.png"><img src="screenshots/frames/phone-08-dark-reader.svg" width="140" alt="Dark monochrome quote reader with glass controls" /></a><br/><sub>Dark reader</sub></td>
</tr>
</table>

</details>


<details>
<summary>Unfolded and tablet layouts</summary>

<table>
<tr>
<td align="center"><a href="play-store/screenshots/large-screen/01-unfolded-browse.png"><img src="screenshots/frames/unfolded-01-unfolded-browse.svg" width="280" alt="Unfolded large-screen reader with Browse beside the quote" /></a><br/><sub>Browse</sub></td>
<td align="center"><a href="play-store/screenshots/large-screen/02-unfolded-settings.png"><img src="screenshots/frames/unfolded-02-unfolded-settings.svg" width="280" alt="Unfolded large-screen reader with Settings in the supporting pane" /></a><br/><sub>Settings</sub></td>
</tr>
<tr>
<td align="center"><a href="play-store/screenshots/large-screen/03-unfolded-saved.png"><img src="screenshots/frames/unfolded-03-unfolded-saved.svg" width="280" alt="Saved quote with Browse available alongside on a large screen" /></a><br/><sub>Saved</sub></td>
<td align="center"><a href="play-store/screenshots/large-screen/04-unfolded-dark.png"><img src="screenshots/frames/unfolded-04-unfolded-dark.svg" width="280" alt="Dark unfolded large-screen reader and Browse pane" /></a><br/><sub>Dark</sub></td>
</tr>
</table>

</details>

<!-- QUOTER-SCREENSHOTS:END -->

Click a preview for the full capture. Frames are illustrative; all views use the
same screenshots prepared for the [Play listing](play-store/ASO-RESEARCH.md).

## Quickstart

Install JDK 17+, Android SDK platform 37 and the official Android CLI. Set
`JAVA_HOME` and `ANDROID_HOME`, then:

```sh
git clone https://github.com/Damercy/Quoter.git
cd Quoter
android info
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

On Windows, use `./gradlew.bat`. On Unix, run `chmod +x gradlew` first.
Release credentials are unnecessary for development.

## Releases

[GitHub releases](https://github.com/Damercy/Quoter/releases/latest) include a signed
APK, Play bundle, crash mapping and SHA-256 checksums. The GitHub APK uses a
different certificate from Play-installed copies and cannot update them.
The 4.00.00 Play update is pending.

## Documentation

- [Contributing and agent guidance](AGENTS.md) · [CI guide](audit/CI.md)
- [Design direction](audit/DESIGN-DIRECTION.md) · [Validation](audit/MIGRATION-VALIDATION.md)
- [Release workflow](audit/RELEASE-WORKFLOW.md) · [Play setup](audit/PLAY-PUBLISHING-SETUP.md)
- [Quote sources and attribution](audit/QUOTE-SOURCES.md)

Report bugs or suggest improvements through [GitHub issues](https://github.com/Damercy/Quoter/issues).
This repository is licensed under [CC0 1.0](LICENSE); bundled dependencies and
quote sources retain their [respective notices](app/src/main/assets/licenses).
