# Quote delivery decision — 8 October 2026

The owner approved static JSON refresh with an offline database. Reading, author search, topic filtering, saved quotes and reminders use Room locally. No per-quote REST request is required.

## Selected sources

- [QuoteSlate](https://github.com/Musheer360/QuoteSlate): its public REST service documents 100 requests per 15 minutes and recommends caching or hosting your own instance for public apps. The observed REST response was HTTP 429. Refresh uses its raw `main/data/quotes.json` instead.
- [The Quotes Database](https://github.com/micheleriva/the-quotes-database): refresh uses raw `master/src/data/quotes.json`.
- Quote Garden's public service returned HTTP 503 during assessment and was excluded.

Both selected repositories include MIT notices. Exact notices, source commit IDs, SHA-256 hashes and input record counts are bundled under `assets/licenses` and `assets/quote-sources.json`. The importer pins source commits when preparing an app release; runtime refresh follows the source branches.

## Offline size and reliability

The larger source has 5,421 records, but contains duplicates and missing authors. Combined with QuoteSlate and the original 100 quotes, validation yields **3,079 distinct attributed quotes**, including 2,678 with specific topics. This is the actual offline count, rather than the raw record count. Text and author determine stable IDs, so source IDs cannot overwrite saved identities.

At most two conditional requests run once per day while the app is used. ETags avoid downloading unchanged files. Failed attempts back off for one hour. Responses have seven-second call timeouts, a four-MiB size limit and a 20,000-record limit. Invalid, empty, rate-limited or unavailable responses never replace the local collection. Refresh adds or updates valid quotes and preserves existing tags and saved IDs.

GitHub raw hosting is free to access without an API key but has no unlimited-usage or availability guarantee. Offline operation removes that dependency from the reading path. Annual maintenance should rerun `scripts/import-quotes.mjs`, review source notices and check the validation results before release.

## Content limitations

Source repository notices document collection provenance; they do not verify every quotation's attribution. A publication review should sample attribution and inappropriate content. Expansion should use collections with explicit redistribution notices and count distinct valid records after importing.
