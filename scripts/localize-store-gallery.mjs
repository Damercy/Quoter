// Lay out translated captions around unaltered, real English app captures.
// npm install --no-save sharp, then node scripts/localize-store-gallery.mjs
// Alternatively pass the absolute path to an existing sharp module as argv[2].
import {readFileSync, writeFileSync, mkdirSync} from 'node:fs';
import {resolve, dirname} from 'node:path';
import {fileURLToPath} from 'node:url';
import {createRequire} from 'node:module';
const require = createRequire(import.meta.url);
const sharp = require(process.argv[2] || 'sharp');
const root = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const store = resolve(root, 'play-store');
const media = JSON.parse(readFileSync(resolve(store, 'media.json'), 'utf8'));
const captions = JSON.parse(readFileSync(resolve(store, 'screenshot-captions.json'), 'utf8'));
const manifest = JSON.parse(readFileSync(resolve(store, 'localization.json'), 'utf8'));
manifest.images = {};
const escape = value => value.replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('"', '&quot;');
for (const [language, copy] of Object.entries(captions)) {
  manifest.images[language] = {};
  for (const group of ['phone', 'largeScreen']) {
    if (copy[group].length !== media[group].length) throw new Error(`Caption count: ${language}/${group}`);
    const paths = [];
    for (const [index, item] of media[group].entries()) {
      const phone = group === 'phone';
      const w = phone ? 1080 : 1920, h = phone ? 1920 : 1080;
      const png = readFileSync(resolve(store, item.path));
      const title = copy[group][index];
      const x = phone ? 80 : 165, y = phone ? 224 : 168;
      const imageW = phone ? 920 : 1590, imageH = phone ? 1635 : 894;
      const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="${w}" height="${h}">
        <rect width="100%" height="100%" fill="#fff"/>
        <text x="${w / 2}" y="${phone ? 102 : 75}" text-anchor="middle" font-family="Arial, sans-serif" font-size="${phone ? 50 : 52}" font-weight="bold" fill="#111">${escape(title)}</text>
        <text x="${w / 2}" y="${phone ? 158 : 128}" text-anchor="middle" font-family="Arial, sans-serif" font-size="${phone ? 30 : 30}" fill="#444">${escape(copy.disclosure)}</text>
        <image x="${x}" y="${y}" width="${imageW}" height="${imageH}" preserveAspectRatio="xMidYMid meet" href="data:image/png;base64,${png.toString('base64')}"/>
      </svg>`;
      const relative = `screenshots/localized/${language}/${group}/${item.path.split('/').at(-1)}`;
      const output = resolve(store, relative);
      mkdirSync(dirname(output), {recursive: true});
      await sharp(Buffer.from(svg)).removeAlpha().png({palette: false}).toFile(output);
      const metadata = await sharp(output).metadata();
      if (metadata.width !== w || metadata.height !== h || metadata.channels !== 3) throw new Error(`Invalid asset: ${output}`);
      paths.push(relative);
    }
    for (const type of group === 'phone' ? ['phoneScreenshots'] : ['sevenInchScreenshots', 'tenInchScreenshots']) {
      manifest.images[language][type] = paths;
    }
  }
}
writeFileSync(resolve(store, 'localization-update.json'), JSON.stringify(manifest, null, 2) + '\n');
console.log('Prepared 24 captioned assets and a metadata-only manifest; original captures are unchanged.');
