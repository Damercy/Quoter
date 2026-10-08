"""Use the same reviewed screenshot files for the README and Play manifest."""
import hashlib
import html
import json
from pathlib import Path
import struct

ROOT = Path(__file__).resolve().parents[1]
STORE = ROOT / 'play-store'
START = '<!-- QUOTER-SCREENSHOTS:START -->'
END = '<!-- QUOTER-SCREENSHOTS:END -->'


def main():
    media = json.loads((STORE / 'media.json').read_text(encoding='utf-8'))
    plan = json.loads((STORE / 'listing.next.json').read_text(encoding='utf-8'))
    rows = []
    inventory = []
    for group, expected, width in [('phone', (1080, 1920), 200), ('largeScreen', (1920, 1080), 600)]:
        rows.append('<p>')
        for item in media[group]:
            file = STORE / item['path']
            content = file.read_bytes()
            if content[:8] != b'\x89PNG\r\n\x1a\n':
                raise ValueError(f'Not a PNG: {file}')
            dimensions = struct.unpack('>II', content[16:24])
            if dimensions != expected or content[24:26] != bytes([8, 2]):
                raise ValueError(f'Use 24-bit RGB PNG at {expected}: {file}')
            inventory.append({'path': item['path'], 'dimensions': dimensions,
                              'sha256': hashlib.sha256(content).hexdigest(), 'alt': item['alt']})
            rows.append(f'<img src="play-store/{html.escape(item["path"])}" width="{width}" alt="{html.escape(item["alt"])}" />')
        rows.append('</p>')
    plan['images'] = {'en-GB': {
        'phoneScreenshots': [x['path'] for x in media['phone']],
        'sevenInchScreenshots': [x['path'] for x in media['largeScreen']],
        'tenInchScreenshots': [x['path'] for x in media['largeScreen']]}}
    (STORE / 'store-update.json').write_text(json.dumps(plan, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    (STORE / 'screenshots/inventory.json').write_text(json.dumps(inventory, indent=2) + '\n', encoding='utf-8')
    readme = ROOT / 'README.md'
    text = readme.read_text(encoding='utf-8')
    gallery = START + '\n' + '\n'.join(rows) + '\n' + END
    if START in text:
        left, rest = text.split(START, 1)
        _, right = rest.split(END, 1)
        text = left + gallery + right
    else:
        left, rest = text.split('#### Screenshots', 1)
        _, right = rest.split('#### Features', 1)
        text = left + '#### Screenshots\n\n' + gallery + '\n\n#### Features' + right
    readme.write_text(text, encoding='utf-8')
    print(f'Validated {len(inventory)} screenshots. README and Play manifest use the same files.')


if __name__ == '__main__':
    main()
