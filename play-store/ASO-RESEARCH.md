# Quoter listing and acquisition plan

Play Console confirmed the 4.00.00 update was published on 9 October 2026.
Store changes can take time to propagate.
On Play this is app store optimisation (ASO). The copy is an informed starting
point, not a measured keyword-ranking or download-growth result. No private
search-term report or external keyword-volume dataset was used.

## English listing and localization

- Title: **Quoter: Quotes and Motivation**.
- Short description: **Read inspiring quotes offline, save favourites, listen and share**.
- Full description: `listing.next.json`.
- Combined description/screenshot update: `store-update.json`.

The follow-up source is `localization.json`; `localization-update.json` contains
the text and translated screenshot captions. The English title stays unchanged.
The revised short description is **Daily quotes in English: read offline, save
favourites, listen and share**. Spanish (Latin America, `es-419`) uses **Quoter:
Frases y motivación**; German (`de-DE`) uses **Quoter: Zitate und Motivation**.
Every language states explicitly that the app interface and quotes are in English.
These translations were prepared here, without independent native-speaker review.
Do not advertise a positive-affirmations feature the app does not have.

Google supports these [listing languages](https://support.google.com/googleplay/android-developer/answer/9844778?hl=en).
An absent manual translation does not mean users can never discover the app:
Play can offer automated listing translations. Manual text and captions give us
control over clarity; the app itself still needs English comprehension.

The follow-up was validated and committed to Google Play on 9 October 2026.
Submission is separate from approval and public availability.

## Acquisition baseline and follow-up

The owner's acquisition report for 12 April–5 October 2026 indicates very low
search traffic. Exact report values and country/locale observations are retained
privately outside Git. Three search visitors cannot establish search conversion,
keyword ranking or the cause of low visibility. Active users by country and store
visitors by client language measure different populations; neither alone proves
a missing translation caused a missed install.

Record the localization's actual public date. Compare matching 14-day and 28-day
windows for search visitors/acquisitions, separately from Explore and referrals.
Inspect Spanish/German client-language rows alongside Colombia/Germany country
rows: language and country are separate dimensions. Use the preceding 28 days
as a comparable baseline when available, retaining the long window as context.
Report visitor counts alongside conversion, and examine retention. Use search
queries only when privacy thresholds permit; suppressed queries are not zero demand.
Low traffic may leave even a month inconclusive. The app release and metadata
changes occur together, so before/after installs cannot isolate an ASO effect.

This update changes no categories, tags, countries, paid campaigns or availability.
Keyword relevance is a hypothesis to measure, not a promise of improved ranking.

## Original English release rationale

The previous title led with a generic “Quotes app” and the old description omitted
offline reading, search, Saved and large-screen layouts. The prepared copy brings
the brand forward, explains the main benefits early and uses relevant language
naturally: quotes, motivation, offline reading, favourites, daily reminders and
on-device listening. These terms describe verified functionality; their search
volume and ranking impact have not been established. Voice choice does not imply
translation or guaranteed natural/neural speech. Reading works offline; content
refresh needs a connection. Keep UI and content claims aligned with the final build.

Google's [listing guidance](https://support.google.com/googleplay/android-developer/answer/13393723)
limits title/short/full text to 30/80/4,000 characters and recommends accurate,
concise benefits. Its [metadata policy](https://support.google.com/googleplay/android-developer/answer/9898842)
prohibits misleading claims and repetitive or irrelevant keywords. Avoid ranking,
price and install-now claims, keyword blocks, competitor names or implied endorsements.

## Measure before making further changes

Use [Acquisition reporting](https://google.play/business/acquisitionreporting/)
to establish search terms, visitors, acquisitions and conversion by country and
language. Record the baseline before this release. The search-intent suggestions
above should be refined using Quoter's actual search terms rather than assumed
keyword popularity. Choose the category/tags that accurately match the app's
primary quote-reading purpose; this draft does not change category or tags.

Once enough traffic is available, run a [store listing experiment](https://google.play/business/store-listing-experiments/)
on one screenshot order or one short-description variant at a time. Google
recommends at least a week to include weekday/weekend differences; a week alone
is not sufficient evidence if traffic is low. Evaluate acquisition/conversion
and one-day retention, use the experiment's uncertainty, and keep an inconclusive
test inconclusive. Do not infer a listing effect solely from raw installs during
an app release. First test: reader → Browse → Saved versus reader → Saved → Browse.

Prioritise translations where acquisition reports show demand. The app
currently presents English quotes/UI; a localised store page must not imply that
the app or its quotes are translated. Consider country/search-targeted listings
only when actual audience data supports a distinct message.

## Screenshots shared with GitHub

`media.json` is the source of ordering and alt text. `scripts/sync-store-gallery.py`
validates assets, regenerates the README gallery and builds `store-update.json`
with the same exact files. It also writes dimensions and SHA-256 values to
`screenshots/inventory.json`.

There are eight 1080×1920 phone captures and four 1920×1080 unfolded/large-window
captures from the retained emulator. PNGs are opaque 24-bit RGB; no device frames,
promotional text, stretched UI or artificial fold seam were added. The large set
is prepared for both tablet screenshot slots; Play has no dedicated foldable
image type in this client. All images were visually inspected. This demonstrates
the emulator UI, not physical-device fold/audio/performance validation.

Captures cover reading, Browse/search/topics, Saved, theme/glass controls, speech
speed/language, daily reminders, the clock picker, image sharing and dark mode.
Large images show the supporting Browse or Settings pane beside the reader.
Motion, haptics and offline speech quality cannot be proved by a still image.
Recapture any changed screens from the final approved build before publishing.

See Google's [preview asset guidance](https://support.google.com/googleplay/android-developer/answer/9866151)
for current format requirements. The prepared files meet the screenshot format,
dimension and recommended 9:16/16:9 checks locally. Google validated and accepted
the original release images. Alt text is included in the README and inventory; upload it
in Console where supported because this API client does not submit image alt text.

`screenshot-captions.json` supplies translated captions.
`scripts/localize-store-gallery.mjs` lays captions around the same English captures;
it never replaces app UI text, stretches the capture or adds an artificial fold
seam. Each image discloses the English interface/content. The 24 generated opaque
RGB PNGs in `screenshots/localized` include eight phone and four landscape images
per language. Landscape images populate both tablet slots. All were visually
inspected and passed dimension/RGB checks. Original captures remain unchanged.

Regenerate using Node.js and Sharp:

```sh
npm install --no-save --package-lock=false sharp@0.35.4
node scripts/localize-store-gallery.mjs
python scripts/play-store.py plan --manifest play-store/localization-update.json
```

Alternatively pass an existing Sharp module's absolute path as the second argument.
Inspect captions before publishing because fonts can differ between hosts.
The follow-up manifest contains no bundle or track replacement. New languages
are created with the listing update endpoint; existing languages are patched.
Commit rejects active review instead of canceling it. Credentials, report baseline
and before/after snapshots stay under `%USERPROFILE%/.codex/quoter-play/localization`
outside Git. Apply with `scripts/play-store.py apply --manifest
play-store/localization-update.json --commit`, then check review/public availability.

## Preview video

Yes: add a single YouTube video URL to the listing's `video` field. Play does not
accept an MP4 file directly as the standard listing preview. The video must be
public or unlisted, embeddable, not age restricted, and have ads disabled. Use a
video URL without playlist/timecode parameters. These are Google's
[preview video requirements](https://support.google.com/googleplay/android-developer/answer/9866151).

Suggested 30-second walkthrough, based on the verified UI:

| Time | App footage |
| --- | --- |
| 0–5s | Read and page through a quote |
| 5–10s | Browse, search and choose a topic |
| 10–14s | Save a quote and open Saved |
| 14–18s | Listening controls and speech settings |
| 18–22s | Theme and daily reminder settings |
| 22–26s | Share image via the system chooser; cancel without recipients |
| 26–30s | Unfolded reader with Browse beside it |

Use real final-build footage and concise captions. No copyrighted music is
needed. Keep the demo understandable with muted audio. The video is not recorded
or uploaded yet; YouTube account/channel access and the reviewed video are needed.
Once its URL exists, add it to `listing.next.json`, regenerate the combined
manifest and review it with the same final release confirmation.

## Branch cleanup

The owner authorised removing every remote branch except master/main. Seven
branches were deleted and the GitHub API verified that only `master` remains.
Their names and commits, plus a Git recovery bundle, are saved outside Git under
`%USERPROFILE%/.codex/quoter-play/branch-backups`. The active local development
branch was preserved. GitHub v4.00.00 and the Play production update have since
been published. The follow-up localization is a separate store metadata change.
