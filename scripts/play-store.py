"""Quoter's Google Play client. Export/plan are the default preparation steps.

Apply creates a temporary Play edit; only --commit publishes the edit.
Credentials default to ~/.codex/quoter-play/credentials/publisher.json.
"""
import argparse
import hashlib
import json
import os
import sys
import zipfile
from datetime import datetime, timezone
from pathlib import Path

PACKAGE = "com.dayaonweb.quoter"
ROOT = Path.home() / ".codex" / "quoter-play"
IMAGE_TYPES = {"icon", "featureGraphic", "phoneScreenshots", "sevenInchScreenshots",
               "tenInchScreenshots", "tvBanner", "tvScreenshots", "wearScreenshots"}


def load_plan(path):
    data = json.loads(path.read_text(encoding="utf-8"))
    if data.get("packageName") != PACKAGE:
        raise ValueError("Manifest must target Quoter's production package.")
    unknown = set(data) - {"packageName", "listings", "images", "bundle", "release", "mapping"}
    if unknown:
        raise ValueError(f"Unknown manifest fields: {sorted(unknown)}")
    for language, listing in data.get("listings", {}).items():
        if not language or not listing:
            raise ValueError("Each listing needs a language and at least one field.")
        for field, value in listing.items():
            limits = {"title": 30, "shortDescription": 80, "fullDescription": 4000, "video": 500}
            if field not in limits or not isinstance(value, str) or not value.strip() or len(value) > limits[field]:
                raise ValueError(f"Invalid {language} listing field: {field}")
    files = []
    for language, types in data.get("images", {}).items():
        for kind, images in types.items():
            if kind not in IMAGE_TYPES or not isinstance(images, list) or not images:
                raise ValueError("Image replacement must contain a supported type and nonempty file list.")
            for image in images:
                file = (path.parent / image).resolve()
                if file.suffix.lower() not in {".png", ".jpg", ".jpeg"}:
                    raise ValueError("Store images must be PNG or JPEG.")
                files.append(file)
    release = data.get("release")
    if bool(data.get("bundle")) != bool(release):
        raise ValueError("A bundle and release must be provided together.")
    if release:
        if release.get("track") not in {"internal", "alpha", "beta", "production"}:
            raise ValueError("Unsupported release track.")
        if release.get("status") not in {"draft", "completed", "inProgress"}:
            raise ValueError("Specify draft, completed, or inProgress release status.")
        if not isinstance(release.get("expectedVersionCode"), int) or release["expectedVersionCode"] <= 0:
            raise ValueError("Release needs a positive expectedVersionCode.")
        if release["status"] == "inProgress" and not 0 < release.get("userFraction", 0) < 1:
            raise ValueError("Staged rollout needs userFraction between 0 and 1.")
        for note in release.get("releaseNotes", []):
            if set(note) != {"language", "text"} or not isinstance(note["text"], str) or len(note["text"]) > 500:
                raise ValueError("Release notes need language/text and at most 500 characters.")
        bundle = (path.parent / data["bundle"]).resolve()
        files.append(bundle)
        if bundle.suffix.lower() != ".aab":
            raise ValueError("Bundle must be an AAB.")
        with zipfile.ZipFile(bundle) as archive:
            names = [name.upper() for name in archive.namelist()]
            if not any(name.startswith("META-INF/") and name.endswith(".SF") for name in names) or not any(
                name.startswith("META-INF/") and name.endswith((".RSA", ".DSA", ".EC")) for name in names):
                raise ValueError("Bundle is unsigned. Use the existing Play upload signing identity.")
    if data.get("mapping"):
        if not release:
            raise ValueError("Mapping requires a bundle release.")
        files.append((path.parent / data["mapping"]).resolve())
    for file in files:
        if not file.is_file():
            raise ValueError(f"Missing release file: {file}")
    if not any(data.get(key) for key in ("listings", "images", "bundle")):
        raise ValueError("Manifest contains no changes.")
    hashes = {str(file): hashlib.sha256(file.read_bytes()).hexdigest() for file in files}
    return data, hashes


def client(key):
    from google.oauth2 import service_account
    from google_auth_httplib2 import AuthorizedHttp
    from googleapiclient.discovery import build
    from googleapiclient.http import build_http
    credentials = service_account.Credentials.from_service_account_file(
        str(key), scopes=["https://www.googleapis.com/auth/androidpublisher"])
    transport = build_http()  # Excludes HTTP 308 from redirects for resumable uploads.
    transport.timeout = 120
    return build("androidpublisher", "v3", http=AuthorizedHttp(credentials, http=transport), cache_discovery=False)


def upload_chunks(request, label):
    result = None
    while result is None:
        status, result = request.next_chunk(num_retries=3)
        if status:
            print(f'{label}: {status.progress():.0%}', flush=True)
    print(f'{label}: uploaded', flush=True)
    return result


def export(service, base, output):
    edits = service.edits()
    listing_data = edits.listings().list(**base).execute()
    snapshot = {"packageName": PACKAGE, "listings": listing_data,
                "tracks": edits.tracks().list(**base).execute(), "images": {}}
    for listing in listing_data.get("listings", []):
        language = listing["language"]
        snapshot["images"][language] = {kind: edits.images().list(
            **base, language=language, imageType=kind).execute() for kind in sorted(IMAGE_TYPES)}
    output.mkdir(parents=True, exist_ok=True)
    (output / "snapshot.json").write_text(json.dumps(snapshot, indent=2, ensure_ascii=False), encoding="utf-8")
    print(json.dumps({"connection": "verified", "packageName": PACKAGE,
                      "languages": [x["language"] for x in listing_data.get("listings", [])],
                      "tracks": [x["track"] for x in snapshot["tracks"].get("tracks", [])],
                      "snapshot": str(output / "snapshot.json")}, indent=2))


def apply(service, base, manifest, data):
    from googleapiclient.http import MediaFileUpload
    edits = service.edits()
    for language, listing in data.get("listings", {}).items():
        print(f'Updating {language} listing', flush=True)
        edits.listings().patch(**base, language=language, body=listing).execute()
    for language, types in data.get("images", {}).items():
        for kind, images in types.items():
            params = {**base, "language": language, "imageType": kind}
            edits.images().deleteall(**params).execute()
            for image in images:
                print(f'Uploading {kind}: {Path(image).name}', flush=True)
                file = (manifest.parent / image).resolve()
                mime = "image/png" if file.suffix.lower() == ".png" else "image/jpeg"
                edits.images().upload(**params, media_body=MediaFileUpload(str(file), mimetype=mime)).execute()
    if data.get("bundle"):
        release = data["release"]
        result = upload_chunks(edits.bundles().upload(**base, media_body=MediaFileUpload(
            str((manifest.parent / data["bundle"]).resolve()),
            mimetype="application/octet-stream", resumable=True, chunksize=1024 * 1024)), 'Bundle')
        version = result["versionCode"]
        if version != release["expectedVersionCode"]:
            raise ValueError("Uploaded bundle version differs from reviewed expectedVersionCode.")
        if data.get("mapping"):
            upload_chunks(edits.deobfuscationfiles().upload(**base, apkVersionCode=version,
                deobfuscationFileType="proguard", media_body=MediaFileUpload(
                    str((manifest.parent / data["mapping"]).resolve()), mimetype="application/octet-stream",
                    resumable=True, chunksize=1024 * 1024)), 'Crash mapping')
        body = {key: release[key] for key in ("name", "status", "releaseNotes", "userFraction") if key in release}
        body["versionCodes"] = [str(version)]
        # The manifest intentionally specifies the entire target-track release replacement.
        edits.tracks().update(**base, track=release["track"], body={"releases": [body]}).execute()
    print('Validating release edit', flush=True)
    edits.validate(**base).execute()


def main():
    for stream in (sys.stdout, sys.stderr):
        if hasattr(stream, "reconfigure"):
            stream.reconfigure(encoding="utf-8")
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("command", choices=["export", "plan", "apply"])
    parser.add_argument("--key", type=Path, default=Path(os.environ.get("QUOTER_PLAY_KEY", ROOT / "credentials" / "publisher.json")))
    parser.add_argument("--manifest", type=Path)
    parser.add_argument("--output", type=Path)
    parser.add_argument("--commit", action="store_true", help="Commit reviewed changes to Google Play")
    args = parser.parse_args()
    data = None
    if args.command in {"plan", "apply"}:
        if not args.manifest:
            parser.error("--manifest is required")
        data, hashes = load_plan(args.manifest.resolve())
        print(json.dumps({"manifest": data, "sha256": hashes, "commit": args.commit}, indent=2, ensure_ascii=False))
    if args.command == "plan":
        return
    if args.commit and args.command != "apply":
        parser.error("--commit is only allowed with apply")
    service = client(args.key)
    edits = service.edits()
    edit = edits.insert(packageName=PACKAGE, body={}).execute()
    base = {"packageName": PACKAGE, "editId": edit["id"]}
    committed = False
    try:
        stamp = datetime.now(timezone.utc).strftime("%Y%m%dT%H%M%S%fZ")
        output = args.output or ROOT / "backups" / stamp
        export(service, base, output)
        if args.command == "apply":
            apply(service, base, args.manifest.resolve(), data)
            if args.commit:
                print('Committing production/listing edit', flush=True)
                edits.commit(**base, changesInReviewBehavior="ERROR_IF_IN_REVIEW").execute()
                committed = True
                print("Edit committed. Google review/publishing rules still apply.")
            else:
                print("Google validated the temporary edit; it will be discarded without committing.")
    finally:
        if not committed:
            edits.delete(**base).execute()


if __name__ == "__main__":
    try:
        main()
    except Exception as error:
        # Never emit OAuth tokens or credential file content in tracebacks.
        print(f"Play operation failed: {type(error).__name__}: {error}", file=sys.stderr)
        sys.exit(1)
