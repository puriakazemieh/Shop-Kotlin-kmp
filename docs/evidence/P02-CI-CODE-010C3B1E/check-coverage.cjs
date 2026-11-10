const fs = require('node:fs');
const zlib = require('node:zlib');
const crypto = require('node:crypto');
const path = require('node:path');
const root = path.resolve(__dirname, '../../..');
const report = zlib.gunzipSync(fs.readFileSync(path.join(root, 'docs/evidence/P02-CI-CODE-010C3A1/plugin-check-results.txt.gz')));
const hash = crypto.createHash('sha256').update(report).digest('hex');
const expectedHash = '50f0c694447c4b9aab40b1d66c4091cecdad0614ba18c8f70208c3f2b36fd0bd';
if (hash !== expectedHash) throw new Error('Pinned report changed');
const ids = ['P02-CI-CODE-010C3B1A1', 'P02-CI-CODE-010C3B1A2', 'P02-CI-CODE-010C3B1B', 'P02-CI-CODE-010C3B1C', 'P02-CI-CODE-010C3B1D'];
const ledgers = ids.map(id => JSON.parse(fs.readFileSync(path.join(root, `docs/evidence/${id}/ledger.json`))));
const key = x => `${x.shippedZipPath || x.path.replace(/^wordpress\/carmilla-bridge\//, '').replace(/^wordpress\//, '')}|${x.line}|${x.column}|${x.code}`;
const fromLedgers = ledgers.flatMap(x => x.findings);
const expected = [];
let file = '';
for (const line of report.toString('utf8').split(/\r?\n/)) {
  if (line.startsWith('FILE: ')) { file = line.slice(6); continue; }
  if (!line.startsWith('[')) continue;
  for (const x of JSON.parse(line)) {
    if (x.code.startsWith('WordPress.Security.') || x.code.startsWith('PluginCheck.Security.')) expected.push({shippedZipPath: file, ...x});
  }
}
const count = a => { const m = new Map(); for (const x of a) m.set(key(x), (m.get(key(x)) || 0) + 1); return m; };
const actualKeys = count(fromLedgers), expectedKeys = count(expected);
const decode = s => s.replace(/&#039;/g, "'").replace(/&quot;/g, '"').replace(/&gt;/g, '>').replace(/&amp;/g, '&');
const expectedByKey = new Map(expected.map(x => [key(x), decode(x.message)]));
const messageMismatch = fromLedgers.filter(x => expectedByKey.get(key(x)) !== x.message).map(key);
const missing = [...expectedKeys].filter(([k, n]) => (actualKeys.get(k) || 0) < n).map(([k, n]) => ({key: k, expected: n, actual: actualKeys.get(k) || 0}));
const extra = [...actualKeys].filter(([k, n]) => (expectedKeys.get(k) || 0) < n).map(([k, n]) => ({key: k, expected: expectedKeys.get(k) || 0, actual: n}));
const invalid = fromLedgers.filter(x => !x.disposition || !x.reason && !x.dataOriginAndRisk && !x.variableOriginAndGuard || !x.verificationTaskId || x.disposition.startsWith('deferred') && !x.fixTaskId).map(key);
const result = {schema: 1, taskId: 'P02-CI-CODE-010C3B1E', reportSha256: hash,
  expectedCount: expected.length, ledgerCounts: Object.fromEntries(ledgers.map((x, i) => [ids[i], x.count])),
  actualCount: fromLedgers.length, uniqueExpected: expectedKeys.size, uniqueActual: actualKeys.size,
  wpcsComparison: {directMatches: fromLedgers.filter(x => x.wpcs && x.wpcs.column).length,
    nearbyDifferentRule: fromLedgers.filter(x => Array.isArray(x.wpcsNearby) && x.wpcsNearby.length).length,
    noPriorEntryBundledCore: fromLedgers.filter(x => x.wpcs && !x.wpcs.column).length},
  missing, extra, messageMismatch, invalid, deferred: fromLedgers.filter(x => x.disposition.startsWith('deferred')).map(x => ({key: key(x), fixTaskId: x.fixTaskId})),
  dispositions: fromLedgers.reduce((m, x) => (m[x.disposition] = (m[x.disposition] || 0) + 1, m), {})};
fs.writeFileSync(path.join(__dirname, 'coverage.json'), JSON.stringify(result, null, 2) + '\n');
console.log(`expected=${result.expectedCount} actual=${result.actualCount} unique=${result.uniqueActual} missing=${missing.length} extra=${extra.length} messages=${messageMismatch.length} invalid=${invalid.length} deferred=${result.deferred.length}`);
if (expected.length !== 58 || fromLedgers.length !== 58 || missing.length || extra.length || messageMismatch.length || invalid.length) process.exitCode = 1;
