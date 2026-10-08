// Vector presentation wrappers; the original PNG bytes are embedded unchanged.
import {readFileSync, writeFileSync, mkdirSync} from 'node:fs';
import {resolve, dirname} from 'node:path';
import {fileURLToPath} from 'node:url';

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const media = JSON.parse(readFileSync(resolve(root, 'play-store/media.json'), 'utf8'));
const output = resolve(root, 'screenshots/frames');
mkdirSync(output, {recursive: true});
const escape = value => value.replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('"', '&quot;');
for (const [group, items] of Object.entries(media)) {
  if (!['phone', 'largeScreen'].includes(group)) continue;
  for (const item of items) {
    const png = readFileSync(resolve(root, 'play-store', item.path));
    const phone = group === 'phone';
    const width = phone ? 1080 : 1920, height = phone ? 1920 : 1080;
    const marginX = phone ? 20 : 36, marginY = phone ? 34 : 36;
    const outerWidth = width + 2 * marginX, outerHeight = height + 2 * marginY;
    const frame = `<svg xmlns="http://www.w3.org/2000/svg" width="${outerWidth}" height="${outerHeight}" viewBox="0 0 ${outerWidth} ${outerHeight}" role="img" aria-label="${escape(item.alt)}">
  <title>${escape(item.alt)}</title>
  <rect x="2" y="2" width="${outerWidth - 4}" height="${outerHeight - 4}" rx="${phone ? 66 : 52}" fill="#171717" stroke="#777" stroke-width="4"/>
  <defs><clipPath id="screen"><rect x="${marginX}" y="${marginY}" width="${width}" height="${height}" rx="24"/></clipPath></defs>
  <image x="${marginX}" y="${marginY}" width="${width}" height="${height}" clip-path="url(#screen)" href="data:image/png;base64,${png.toString('base64')}"/>
  <circle cx="${outerWidth / 2}" cy="${marginY / 2}" r="6" fill="#555"/>
</svg>
`;
    writeFileSync(resolve(output, `${phone ? 'phone' : 'unfolded'}-${item.path.split('/').at(-1).replace('.png', '.svg')}`), frame);
  }
}
console.log('Framed 12 existing captures without changing their PNG contents.');
