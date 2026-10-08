import {readFileSync} from 'node:fs';
import {inflateRawSync} from 'node:zlib';

// Inspect ELF PT_LOAD alignment in every ABI of an APK or AAB, without an NDK dependency.
const archive = readFileSync(process.argv[2]);
let end = archive.length - 22;
while (end >= Math.max(0, archive.length - 65557) && archive.readUInt32LE(end) !== 0x06054b50) end--;
if (end < 0 || archive.readUInt32LE(end) !== 0x06054b50) throw Error('ZIP directory not found');
let offset = archive.readUInt32LE(end + 16);
let libraries = 0;
for (let i = 0; i < archive.readUInt16LE(end + 10); i++) {
  if (archive.readUInt32LE(offset) !== 0x02014b50) throw Error('Invalid ZIP directory');
  const nameLength = archive.readUInt16LE(offset + 28);
  const name = archive.subarray(offset + 46, offset + 46 + nameLength).toString();
  if (name.endsWith('.so')) {
    const local = archive.readUInt32LE(offset + 42);
    const start = local + 30 + archive.readUInt16LE(local + 26) + archive.readUInt16LE(local + 28);
    const compressed = archive.subarray(start, start + archive.readUInt32LE(offset + 20));
    const method = archive.readUInt16LE(offset + 10);
    if (method !== 0 && method !== 8) throw Error(`Unsupported ZIP compression: ${name}`);
    const elf = method === 8 ? inflateRawSync(compressed) : compressed;
    if (elf.readUInt32LE(0) !== 0x464c457f || elf[5] !== 1) throw Error(`Unsupported ELF: ${name}`);
    const is64 = elf[4] === 2;
    const table = is64 ? Number(elf.readBigUInt64LE(32)) : elf.readUInt32LE(28);
    const stride = elf.readUInt16LE(is64 ? 54 : 42);
    const count = elf.readUInt16LE(is64 ? 56 : 44);
    for (let j = 0; j < count; j++) {
      const header = table + j * stride;
      if (elf.readUInt32LE(header) !== 1) continue;
      const alignment = is64 ? Number(elf.readBigUInt64LE(header + 48)) : elf.readUInt32LE(header + 28);
      if (alignment < 16384) throw Error(`${name}: PT_LOAD alignment ${alignment} is below 16 KB`);
    }
    libraries++;
    console.log(`${name}: ELF load segments aligned for 16 KB`);
  }
  offset += 46 + nameLength + archive.readUInt16LE(offset + 30) + archive.readUInt16LE(offset + 32);
}
console.log(`Verified ${libraries} native libraries. ZIP alignment and on-device runtime are separate checks.`);
