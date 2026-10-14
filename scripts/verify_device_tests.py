"""Require a completed native instrumentation run, including every declared test."""
import argparse
from pathlib import Path
import re
import xml.etree.ElementTree as ET


def parse_results(text):
    current = {}
    completed = {}
    for line in text.splitlines():
        match = re.match(r'INSTRUMENTATION_STATUS: (class|test)=(.*)', line)
        if match:
            current[match[1]] = match[2].strip()
        status = re.match(r'INSTRUMENTATION_STATUS_CODE: (-?\d+)', line)
        if status:
            code = int(status[1])
            if code <= 0 and current.get('class') and current.get('test'):
                completed[(current['class'], current['test'])] = code
            current = {}
    return completed


def validate(text, completed, expected):
    if expected <= 0:
        raise ValueError('No declared device tests found.')
    if len(completed) < expected:
        raise ValueError(f'Incomplete device run: {len(completed)} of at least {expected} tests reported.')
    if any(code not in {0, -3, -4} for code in completed.values()):
        raise ValueError('Device tests reported failures or errors.')
    if not re.search(r'^OK \([1-9]\d* tests?\)\s*$', text, re.MULTILINE) or 'FAILURES!!!' in text:
        raise ValueError('Native runner did not report a successful completed run.')
    if not re.search(r'^INSTRUMENTATION_CODE: -1\s*$', text, re.MULTILINE):
        raise ValueError('Native runner did not terminate normally.')


def write_report(completed, output):
    failed = sum(code not in {0, -3, -4} for code in completed.values())
    skipped = sum(code in {-3, -4} for code in completed.values())
    suite = ET.Element('testsuite', name='device', tests=str(len(completed)), failures=str(failed), skipped=str(skipped))
    for (class_name, name), code in sorted(completed.items()):
        case = ET.SubElement(suite, 'testcase', classname=class_name, name=name)
        if code in {-3, -4}:
            ET.SubElement(case, 'skipped', message='Runner reported a platform or window assumption.')
        elif code != 0:
            ET.SubElement(case, 'failure', message=f'Instrumentation status {code}; see instrumentation.txt.')
    output.parent.mkdir(parents=True, exist_ok=True)
    ET.ElementTree(suite).write(output, encoding='utf-8', xml_declaration=True)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--result', type=Path, required=True)
    parser.add_argument('--output', type=Path, required=True)
    parser.add_argument('--sources', type=Path, default=Path('app/src/androidTest'))
    args = parser.parse_args()
    text = args.result.read_text(encoding='utf-8', errors='replace')
    completed = parse_results(text)
    write_report(completed, args.output)
    expected = sum(len(re.findall(r'^\s*@Test\b', source.read_text(encoding='utf-8'), re.MULTILINE))
                   for source in args.sources.rglob('*') if source.suffix in {'.kt', '.java'})
    validate(text, completed, expected)
    print(f'Device run completed: {len(completed)} tests, {sum(code in {-3, -4} for code in completed.values())} platform/window skips.')


if __name__ == '__main__':
    main()
