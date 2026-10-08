// Run with Node 20+: node scripts/import-quotes.mjs [output-directory]
// Imports pinned snapshots, preserves attribution, deduplicates deterministically.
import {mkdir, readFile, writeFile} from 'node:fs/promises';
import {createHash} from 'node:crypto';
import path from 'node:path';
const output = path.resolve(process.argv[2] || 'app/src/main/assets');
const sources = [
  {repo:'micheleriva/the-quotes-database', branch:'master', data:'src/data/quotes.json', license:'LICENSE.md'},
  {repo:'Musheer360/QuoteSlate', branch:'main', data:'data/quotes.json', license:'LICENSE'}
];
async function get(url) {
  const response = await fetch(url, {signal:AbortSignal.timeout(30000)});
  if (!response.ok) throw new Error(`${response.status}: ${url}`);
  return response.text();
}
const all = JSON.parse(await readFile('poc/app/src/main/assets/quotes.json','utf8'));
const provenance = [];
await mkdir(path.join(output,'licenses'),{recursive:true});
for (const source of sources) {
  const metadata = JSON.parse(await get(`https://api.github.com/repos/${source.repo}`));
  source.branch = metadata.default_branch;
  const commit = JSON.parse(await get(`https://api.github.com/repos/${source.repo}/commits/${source.branch}`)).sha;
  const base = `https://raw.githubusercontent.com/${source.repo}/${commit}/`;
  const raw = await get(base+source.data);
  const rows = JSON.parse(raw);
  if (!Array.isArray(rows)) throw new Error(`Unexpected schema: ${source.repo}`);
  const license = await get(base+source.license);
  if (!license.includes('Permission is hereby granted')) throw new Error(`Expected MIT license: ${source.repo}`);
  await writeFile(path.join(output,'licenses',source.repo.replace('/','_')+'.txt'),license);
  all.push(...rows);
  provenance.push({repo:source.repo,commit,endpoint:`https://raw.githubusercontent.com/${source.repo}/${source.branch}/${source.data}`,
    sha256:createHash('sha256').update(raw).digest('hex'),inputCount:rows.length,license:'MIT'});
}
const deduplicated = new Map();
for (const row of all) {
  const quote = String(row.quote || row.quoteText || '').replace(/\s+/g,' ').trim();
  const author = String(row.author || row.quoteAuthor || '').replace(/\s+/g,' ').trim();
  if (quote.length < 5 || quote.length > 3000 || !author || author.length > 200) continue;
  const identity = `${quote.toLowerCase()}|${author.toLowerCase()}`;
  const existing = deduplicated.get(identity);
  const tags = [...new Set((row.tags || []).filter(t=>typeof t==='string').map(t=>t.trim().toLowerCase()).filter(Boolean))];
  if (existing) {existing.tags = [...new Set([...existing.tags,...tags])]; continue;}
  deduplicated.set(identity,{id:createHash('sha256').update(identity).digest('hex').slice(0,24),quote,author,tags});
}
const quotes = [...deduplicated.values()].map(q=>({...q,tags:q.tags.length?q.tags:['general']}));
await writeFile(path.join(output,'quotes.json'),JSON.stringify(quotes));
await writeFile(path.join(output,'quote-sources.json'),JSON.stringify({imported:'2026-10-08',quoteCount:quotes.length,sources:provenance},null,2));
console.log(JSON.stringify({quoteCount:quotes.length,tagged:quotes.filter(q=>!q.tags.includes('general')).length,sources:provenance},null,2));
