"""Prepare a frozen Quoter release; execute only the reviewed approval digest.

No push, tag, GitHub release, or Play upload occurs during prepare.
"""
import argparse
import hashlib
import importlib.util
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]
PRIVATE = Path.home() / '.codex/quoter-play/releases'
REPO = 'Damercy/Quoter'
REMOTE = 'https://github.com/Damercy/Quoter.git'
GH = shutil.which('gh') or 'C:/Program Files/GitHub CLI/gh.exe'
spec = importlib.util.spec_from_file_location('play', ROOT / 'scripts/play-store.py')
play = importlib.util.module_from_spec(spec)
spec.loader.exec_module(play)


def run(*args, input=None):
    result = subprocess.run([str(x) for x in args], cwd=ROOT, input=input,
                            capture_output=True, text=True, encoding='utf-8')
    if result.returncode:
        raise RuntimeError(f'{args[0]} failed: {result.stderr.strip()}')
    return result.stdout.strip()


def digest(data):
    return hashlib.sha256(json.dumps(data, sort_keys=True).encode()).hexdigest()


def file_hash(path):
    return hashlib.sha256(path.read_bytes()).hexdigest() if path.is_file() else None


def source_files():
    names = run('git', 'ls-files', '--cached', '--others', '--exclude-standard', '-z').split('\0')
    # Keep release sources, tests, build setup and reviewed docs. Device captures
    # and the experimental POC are not release source inputs.
    roots = {'app', 'gradle', 'scripts', 'play-store', '.github', 'screenshots'}
    top = {'README.md', 'CHANGELOG.md', 'AGENTS.md', '.gitignore', '.imgbotconfig', 'build.gradle',
           'settings.gradle', 'gradle.properties', 'gradlew', 'gradlew.bat', 'LICENSE'}
    docs = {'audit/RELEASE-WORKFLOW.md', 'audit/PLAY-PUBLISHING-SETUP.md',
            'audit/DESIGN-DIRECTION.md', 'audit/QUOTE-SOURCES.md',
            'audit/MIGRATION-VALIDATION.md', 'audit/MIGRATION-READINESS.md',
            'audit/RANDOM-JOURNEYS.md', 'audit/GITHUB-RELEASE-4.00.00.md',
            'audit/LOCAL-DEVICE-NOTES.md', 'audit/CI.md'}
    selected = []
    removed = []
    tracked = set(run('git', 'ls-files', '-z').split('\0'))
    for name in sorted(set(names) - {''}):
        path = Path(name)
        if path.suffix.lower() in {'.jks', '.keystore', '.p12', '.pfx', '.clixml'}:
            if name in tracked:
                removed.append(name)
            continue
        if path.parts[0] not in roots and name not in top and name not in docs:
            continue
        if name.endswith(('publisher.json', 'keystore.properties')):
            raise ValueError('A private credential is among release sources.')
        file = ROOT / name
        if file.is_file() and file.stat().st_size < 2_000_000:
            text = file.read_bytes()
            if re.search(rb'^-----BEGIN (?:RSA |EC |ENCRYPTED )?PRIVATE KEY-----\s*$', text, re.MULTILINE):
                raise ValueError(f'Private key material found in {name}')
        selected.append(name)
    return {name: file_hash(ROOT / name) for name in selected}, removed


def remote_head():
    return run('git', 'ls-remote', 'origin', 'refs/heads/master').split()[0]


def check_connection():
    if run('git', 'remote', 'get-url', 'origin') != REMOTE:
        raise ValueError('origin must be the existing Damercy/Quoter repository.')
    if run('git', 'diff', '--cached', '--name-only'):
        raise ValueError('Finish or unstage existing staged work before preparing a release.')
    permissions = json.loads(run(GH, 'api', f'repos/{REPO}'))
    if not permissions['permissions'].get('push') or permissions['default_branch'] != 'master':
        raise ValueError('Repository write permission or expected master branch is missing.')


def prepare(args):
    check_connection()
    manifest = args.manifest.resolve()
    data, assets = play.load_plan(manifest)
    for asset in args.asset:
        asset = asset.resolve()
        if not asset.is_file() or asset.suffix.lower() not in {'.apk', '.txt'}:
            raise ValueError('Additional release assets must be APKs or checksum text files.')
        assets[str(asset)] = file_hash(asset)
    if not data.get('release') or data['release']['track'] != 'production' or data['release']['status'] != 'completed':
        raise ValueError('Final release requires a completed production bundle manifest.')
    version = data['release']['name']
    tag = 'v' + version
    if not re.fullmatch(r'v\d+\.\d+\.\d+', tag):
        raise ValueError('Use a numeric three-part release version.')
    gradle = (ROOT / 'app/build.gradle').read_text()
    if re.search(r"versionName\s+'([^']+)'", gradle)[1] != version or int(re.search(r'versionCode\s+(\d+)', gradle)[1]) != data['release']['expectedVersionCode']:
        raise ValueError('Manifest and Gradle versions differ.')
    notes = args.notes.resolve()
    if not notes.is_file() or not notes.read_text().strip():
        raise ValueError('Write and review release notes first.')
    if 'REPLACE WITH' in json.dumps(data) or 'REPLACE WITH' in notes.read_text():
        raise ValueError('Replace all release template placeholders before review.')
    certificate = (ROOT / 'play-store/upload-certificate.sha256').read_text().strip().replace(':', '').lower()
    bundle = (manifest.parent / data['bundle']).resolve()
    tool = args.keytool or 'keytool'
    output = run(tool, '-printcert', '-jarfile', bundle)
    match = re.search(r'SHA256:\s*([A-Fa-f0-9:]+)', output)
    if not match or match[1].replace(':', '').lower() != certificate:
        raise ValueError('Bundle signer does not match the reviewed Play upload certificate.')
    verification = run(args.jarsigner or 'jarsigner', '-verify', bundle)
    if 'jar verified' not in verification.lower():
        raise ValueError('Cryptographic bundle verification failed.')
    files, removed = source_files()
    if 'README.md' not in files or 'CHANGELOG.md' not in files:
        raise ValueError('README and CHANGELOG must be included in the final release.')
    try:
        notes_relative = notes.relative_to(ROOT).as_posix()
        manifest_relative = manifest.relative_to(ROOT).as_posix()
    except ValueError:
        raise ValueError('Keep reviewed notes and manifest in the repository source set.')
    if notes_relative not in files or manifest_relative not in files:
        raise ValueError('Reviewed notes and manifest must be included in the release source set.')
    head = run('git', 'rev-parse', 'HEAD')
    remote = remote_head()
    run('git', 'merge-base', '--is-ancestor', remote, head)
    plan = {'repo': REPO, 'head': head, 'remoteHead': remote, 'tag': tag,
            'destinations': ['github'] if args.github_only else ['github', 'play'],
            'manifest': str(manifest), 'manifestHash': file_hash(manifest),
            'notes': str(notes), 'notesHash': file_hash(notes), 'assets': assets,
            'sources': files, 'removeTrackedKeys': removed}
    approval = digest(plan)
    folder = PRIVATE / tag
    if folder.exists():
        raise ValueError(f'Release plan already exists: {folder}. Review it before replacing it.')
    folder.mkdir(parents=True)
    (folder / 'plan.json').write_text(json.dumps(plan, indent=2), encoding='utf-8')
    print(json.dumps({'approval': approval, 'plan': str(folder / 'plan.json'),
                      'tag': tag, 'files': len(files), 'removeTrackedKeys': removed}, indent=2))


def verify_inputs(plan):
    for name, value in plan['sources'].items():
        if file_hash(ROOT / name) != value:
            raise ValueError(f'Reviewed source changed: {name}')
    for field in ('manifest', 'notes'):
        if file_hash(Path(plan[field])) != plan[field + 'Hash']:
            raise ValueError(f'Reviewed {field} changed.')
    for name, value in plan['assets'].items():
        if file_hash(Path(name)) != value:
            raise ValueError(f'Reviewed asset changed: {name}')


def execute(args):
    path = args.plan.resolve()
    plan = json.loads(path.read_text())
    if digest(plan) != args.approval:
        raise ValueError('Approval digest does not match the reviewed release.')
    verify_inputs(plan)
    journal_path = path.with_name('state.json')
    state = json.loads(journal_path.read_text()) if journal_path.exists() else {}
    def save():
        temporary = journal_path.with_suffix('.tmp')
        temporary.write_text(json.dumps(state, indent=2), encoding='utf-8')
        temporary.replace(journal_path)
    if not state.get('commit'):
        check_connection()
        if run('git', 'rev-parse', 'HEAD') != plan['head'] or remote_head() != plan['remoteHead']:
            raise ValueError('Git history changed since review. Prepare a fresh release plan.')
        # Stage only the frozen source set; remove credential files from future
        # commits while preserving their working-tree copies.
        for name in plan['removeTrackedKeys']:
            run('git', 'rm', '--cached', '--', name)
        for name in plan['sources']:
            run('git', 'add', '--', name)
        run('git', 'commit', '-m', f'Release {plan["tag"]}')
        state['commit'] = run('git', 'rev-parse', 'HEAD')
        save()
    if run('git', 'rev-parse', 'HEAD') != state['commit']:
        raise ValueError('HEAD moved after the release commit. Inspect the release journal.')
    tag = plan['tag']
    refs = run('git', 'tag', '--list', tag)
    if refs:
        if run('git', 'rev-parse', tag + '^{}') != state['commit']:
            raise ValueError('Existing tag points to a different commit.')
    else:
        run('git', 'tag', '-a', tag, state['commit'], '-m', f'Quoter {tag}')
    if not state.get('pushed'):
        run('git', 'push', '--atomic', 'origin', 'HEAD:refs/heads/master', f'refs/tags/{tag}')
        state['pushed'] = True
        save()
    if not state.get('githubDraft'):
        existing = subprocess.run([GH, 'release', 'view', tag, '--repo', REPO, '--json', 'isDraft'], cwd=ROOT, capture_output=True, text=True)
        if existing.returncode == 0:
            if not json.loads(existing.stdout)['isDraft']:
                raise ValueError('An already published GitHub release needs manual review.')
        else:
            run(GH, 'release', 'create', tag, '--repo', REPO, '--verify-tag', '--draft',
                '--title', f'Quoter {tag}', '--notes-file', plan['notes'])
        state['githubDraft'] = True
        save()
    if not state.get('assetsUploaded'):
        bundle_assets = [name for name in plan['assets'] if Path(name).suffix in {'.aab', '.apk', '.txt'}]
        run(GH, 'release', 'upload', tag, '--repo', REPO, '--clobber', *bundle_assets)
        state['assetsUploaded'] = True
        save()
    submit_play = 'play' in plan.get('destinations', ['github', 'play'])
    if submit_play and not state.get('playCommitted'):
        if state.get('playStarted'):
            raise ValueError('Previous Play submission has an uncertain outcome. Check Play Console and the journal before retrying; do not re-upload automatically.')
        state['playStarted'] = True
        save()
        run(sys.executable, ROOT / 'scripts/play-store.py', 'apply', '--manifest', plan['manifest'], '--commit')
        state['playCommitted'] = True
        save()
    if not state.get('githubPublished'):
        run(GH, 'release', 'edit', tag, '--repo', REPO, '--draft=false', '--latest')
        state['githubPublished'] = True
        save()
    if submit_play:
        print('GitHub release published and Play edit submitted. Google review and publishing timing still apply.')
    else:
        print('GitHub release published. Play was excluded from this reviewed plan.')


def main():
    for stream in (sys.stdout, sys.stderr):
        if hasattr(stream, 'reconfigure'):
            stream.reconfigure(encoding='utf-8')
    parser = argparse.ArgumentParser(description=__doc__)
    commands = parser.add_subparsers(dest='command', required=True)
    prep = commands.add_parser('prepare')
    prep.add_argument('--manifest', type=Path, required=True)
    prep.add_argument('--notes', type=Path, required=True)
    prep.add_argument('--keytool')
    prep.add_argument('--jarsigner')
    prep.add_argument('--github-only', action='store_true', help='Publish GitHub assets without contacting Play')
    prep.add_argument('--asset', action='append', type=Path, default=[], help='Additional APK or checksum text asset')
    apply = commands.add_parser('execute')
    apply.add_argument('--plan', type=Path, required=True)
    apply.add_argument('--approval', required=True)
    args = parser.parse_args()
    if args.command == 'prepare':
        prepare(args)
    else:
        execute(args)


if __name__ == '__main__':
    try:
        main()
    except Exception as error:
        print(f'Release stopped: {error}', file=sys.stderr)
        sys.exit(1)
