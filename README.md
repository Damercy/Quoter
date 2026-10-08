![Banner](https://github.com/Damercy/Quoter/assets/24220261/338a7a61-eb22-4683-a71e-667a077d5918 "Banner")  

A minimalist Android quotes app for offline reading, saved favourites, daily
reminders and on-device listening. Browse by topic, search words or authors, and
share an attributed quote image. On supported unfolded and tablet layouts, keep
Browse or Settings beside the reader.

#### Screenshots

<!-- QUOTER-SCREENSHOTS:START -->
<p>
<img src="play-store/screenshots/phone/01-reader.png" width="200" alt="Minimal monochrome quote reader with listening, saving and sharing controls" />
<img src="play-store/screenshots/phone/02-browse.png" width="200" alt="Browse quotes, search by words or author, and filter by topic" />
<img src="play-store/screenshots/phone/03-saved.png" width="200" alt="Saved quotes with a separate reading position and selected bookmark" />
<img src="play-store/screenshots/phone/04-appearance.png" width="200" alt="Light, Dark, Automatic and adjustable glass appearance settings" />
<img src="play-store/screenshots/phone/05-voice-and-reminders.png" width="200" alt="On-device voice and speech speed settings with daily notification options" />
<img src="play-store/screenshots/phone/06-reminder-clock.png" width="200" alt="Native reminder time sheet with an analogue clock" />
<img src="play-store/screenshots/phone/07-image-sharing.png" width="200" alt="Android sharing menu with quote image, author and Quoter attribution" />
<img src="play-store/screenshots/phone/08-dark-reader.png" width="200" alt="Dark monochrome quote reader with glass controls" />
</p>
<p>
<img src="play-store/screenshots/large-screen/01-unfolded-browse.png" width="600" alt="Unfolded large-screen reader with Browse beside the quote" />
<img src="play-store/screenshots/large-screen/02-unfolded-settings.png" width="600" alt="Unfolded large-screen reader with Settings in the supporting pane" />
<img src="play-store/screenshots/large-screen/03-unfolded-saved.png" width="600" alt="Saved quote with Browse available alongside on a large screen" />
<img src="play-store/screenshots/large-screen/04-unfolded-dark.png" width="600" alt="Dark unfolded large-screen reader and Browse pane" />
</p>
<!-- QUOTER-SCREENSHOTS:END -->

#### Features

- Read the bundled quote collection offline; search words, authors and topics.
- Save favourites with a separate reading position.
- Listen using compatible installed offline voices and adjust speech speed.
- Share a quote image with its author and attributed text through Android's chooser.
- Set daily reminders with text or image-style notifications.
- Choose Light, Dark or Automatic appearance, with optional adjustable glass controls.
- Open Browse or Settings beside the quote on supported large and unfolded screens.

The gallery shows version 4.00.00 on the retained phone/unfolded emulator. The
same image files are prepared for the next Play listing update. See
[the ASO and media plan](play-store/ASO-RESEARCH.md).

Download the signed APK, Play bundle, crash mapping and checksums from
[Quoter 4.00.00](https://github.com/Damercy/Quoter/releases/tag/v4.00.00).
The GitHub APK cannot update an existing Play-installed copy because Google uses
a different app signing certificate. The Play Store update is pending.

<p float="left">
<a href='https://play.google.com/store/apps/details?id=com.dayaonweb.quoter&pcampaignid=pcampaignidMKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png' width=500 height=200/></a>
 </p>

Share feedback at: adhikaridayamoy@gmail.com.

## Development

See [CHANGELOG](CHANGELOG.md) for versioned changes. The reader uses Compose, bundled offline quotes, optional
static data refresh, saved quotes and the device's text-to-speech engine.

Clone this existing repository and install JDK 17 or newer, the Android SDK
(platform 37 and the required build tools), and the official Android CLI.
Set `JAVA_HOME` and `ANDROID_HOME` for your machine, then use the included Gradle
wrapper; no global Gradle installation is needed.

```powershell
git clone https://github.com/Damercy/Quoter.git
cd Quoter
android info
./gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

On macOS/Linux use `./gradlew` (make it executable if necessary). Read
[AGENTS.md](AGENTS.md) before changing the app. The Android CLI handles supported
SDK/device workflows; Gradle builds and verifies the project. Release credentials
are optional for development and are supplied privately for publishing.

## Releases from chat

When the owner confirms the final reviewed release, the local release client
updates this repository, creates an annotated version tag and GitHub release,
and submits the same signed bundle plus reviewed store images/descriptions to
Google Play. The README and versioned release notes are reviewed before that
confirmation. Google controls review and when the Play update becomes public.
An explicitly requested GitHub-only release uses `--github-only` and publishes
code, tag and assets without submitting a Play edit.

See [the release workflow](audit/RELEASE-WORKFLOW.md) and
[Play setup](audit/PLAY-PUBLISHING-SETUP.md) for commands and recovery details.
Cloning includes source and tooling; publishing on another machine additionally
requires secure signing/Play credentials and GitHub authentication.
