"""Classify changed paths; docs/media-only commits avoid Android build/device jobs."""
import fnmatch
import json
import os
from pathlib import Path
import re
import subprocess


def android_changed(paths, config):
    return any(any(fnmatch.fnmatchcase(path, pattern) for pattern in config['android'])
               and not any(fnmatch.fnmatchcase(path, pattern) for pattern in config['androidIgnore'])
               for path in paths)


def main():
    config = json.loads(Path('.github/ci-paths.json').read_text())
    event = json.loads(Path(os.environ['GITHUB_EVENT_PATH']).read_text())
    kind = os.environ['GITHUB_EVENT_NAME']
    if kind == 'workflow_dispatch':
        needed = True
    else:
        base = event['pull_request']['base']['sha'] if kind == 'pull_request' else event.get('before', '')
        if not re.fullmatch('[0-9a-f]{40}', base) or set(base) == {'0'}:
            needed = True
        else:
            comparison = base + ('...HEAD' if kind == 'pull_request' else '..HEAD')
            paths = subprocess.check_output(['git', 'diff', '--name-only', '-z', comparison]).decode().split('\0')
            needed = android_changed(paths, config)
    value = str(needed).lower()
    with open(os.environ['GITHUB_OUTPUT'], 'a', encoding='utf-8') as output:
        output.write(f'android={value}\n')
    if os.environ.get('GITHUB_STEP_SUMMARY'):
        with open(os.environ['GITHUB_STEP_SUMMARY'], 'a', encoding='utf-8') as summary:
            summary.write(f'Android build/device matrix required: **{value}**.\n\n{config["description"]}\n')
    print(f'android={value}')


if __name__ == '__main__':
    main()
