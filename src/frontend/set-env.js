const fs = require('fs');
const path = require('path');

require('dotenv').config({ path: path.resolve(__dirname, '../../.env') });
require('dotenv').config();

const apiKey = process.env.GOOGLE_MAPS_API_KEY || '';

if (!apiKey) {
  console.warn(
    '[set-env] GOOGLE_MAPS_API_KEY is not set — Google Maps will fail to load.\n' +
    '          Copy .env.example to .env at the repo root and fill it in.'
  );
}

const targets = [
  { template: 'environment.template.ts', out: 'environment.ts' },
  { template: 'environment.prod.template.ts', out: 'environment.prod.ts' }
];

for (const { template, out } of targets) {
  const content = fs.readFileSync(path.resolve(__dirname, 'src/environments', template), 'utf8');
  const generated = content.replace(/googleMapsApiKey:\s*'[^']*'/, `googleMapsApiKey: '${apiKey}'`);
  fs.writeFileSync(path.resolve(__dirname, 'src/environments', out), generated);
}

console.log('[set-env] Generated environment.ts and environment.prod.ts from templates.');
