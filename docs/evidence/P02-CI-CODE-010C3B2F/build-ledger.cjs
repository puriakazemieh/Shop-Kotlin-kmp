const fs = require('node:fs');
const zlib = require('node:zlib');
const crypto = require('node:crypto');
const path = require('node:path');
const root = path.resolve(__dirname, '../../..');
const report = zlib.gunzipSync(fs.readFileSync(path.join(root, 'docs/evidence/P02-CI-CODE-010C3A1/plugin-check-results.txt.gz')));
const hash = crypto.createHash('sha256').update(report).digest('hex');
if (hash !== '50f0c694447c4b9aab40b1d66c4091cecdad0614ba18c8f70208c3f2b36fd0bd') throw Error('Pinned report changed');
const wpcs = JSON.parse(fs.readFileSync(path.join(root, 'tools/code-quality/wpcs-security-dispositions.json'))).securityDbFindings;
const details = {
  'class-cb-web-session-wp-adapter.php:54': 'SELECT DATABASE() checks the active WordPress connection against trusted DB_NAME before separate mysqli connections are allowed; a cached value would defeat this fail-closed check.',
  'class-cb-web-session-wp-adapter.php:55': 'SELECT CONNECTION_ID() identifies the live WordPress connection for same-primary validation; a cached value would be unsafe.',
  'class-cb-web-session-wp-adapter.php:135': 'mysqli_init creates separate local connections required for atomic browser-session and password-state checks; config, topology, identity, and errors are guarded and fail closed.',
  'class-cb-web-auth-login.php:96': 'Prepared login/email/mobile lookup binds all request-derived identifiers; table names come from WordPress. Live read detects ambiguous matches and last_error is checked before password authentication.',
  'class-cb-web-auth-controller.php:23': 'Fixed-prefix schema-version marker read gates browser-auth controller construction; last_error and exact version are checked, so stale cache would be inappropriate.',
  'class-cb-otp-atomic-consumer.php:25': 'Prepared transient-hash read uses authoritative options table after external cache is rejected; current hash must match before conditional one-row deletion.',
  'class-cb-otp-atomic-consumer.php:33': 'Prepared conditional DELETE binds expiry name, OTP name, and checked hash; BINARY value comparison and database expiry enforce one-time consumption.',
  'class-cb-reset-atomic-consumer.php:16': 'Prepared transient mapping read uses authoritative options table after external cache is rejected; expected login must match before conditional delete.',
  'class-cb-reset-atomic-consumer.php:21': 'Prepared conditional DELETE binds expiry name, mapping name, and checked login; BINARY value comparison and database expiry enforce one-time consumption.'
};
const selected = new Set(['class-cb-web-session-wp-adapter.php', 'class-cb-web-auth-login.php', 'class-cb-web-auth-controller.php', 'class-cb-otp-atomic-consumer.php', 'class-cb-reset-atomic-consumer.php']);
let file = '';
const findings = [];
for (const line of report.toString('utf8').split(/\r?\n/)) {
  if (line.startsWith('FILE: ')) { file = line.slice(6); continue; }
  if (!line.startsWith('[') || !selected.has(path.basename(file))) continue;
  for (const item of JSON.parse(line)) {
    if (!item.code.startsWith('WordPress.DB.')) continue;
    const detail = details[`${path.basename(file)}:${item.line}`];
    if (!detail) throw Error(`Unreviewed ${file}:${item.line}`);
    const sourcePath = `wordpress/carmilla-bridge/${file}`;
    const matches = wpcs.filter(x => x.path === sourcePath && x.line === item.line && x.source === item.code);
    if (matches.length !== 1 || matches[0].disposition !== 'context_justified') throw Error(`WPCS mismatch ${file}:${item.line}:${item.code}`);
    findings.push({path: sourcePath, shippedZipPath: file, line: item.line, column: item.column, code: item.code, message: item.message, severity: item.type,
      sqlOriginAndRisk: detail, disposition: 'context_justified', fixTaskId: null, verificationTaskId: 'P02-CI-CODE-010C3B2F',
      sourceTrace: `${sourcePath}:${item.line}`, testPath: file.includes('web-session') ? 'wordpress/carmilla-bridge/tests/smoke-web-session-wp-adapter.php' : null,
      wpcs: {column: matches[0].column, disposition: matches[0].disposition, matchBasis: 'path+line+code; columns differ between scanners where shown'}});
  }
}
if (findings.length !== 16 || new Set(findings.map(x => `${x.shippedZipPath}:${x.line}:${x.column}:${x.code}`)).size !== 16) throw Error('Coverage mismatch');
fs.writeFileSync(path.join(__dirname, 'ledger.json'), JSON.stringify({schema: 1, taskId: 'P02-CI-CODE-010C3B2F', reportSha256: hash, count: findings.length, findings}, null, 2) + '\n');
console.log('findings=16 context=16 wpcsMatched=16');
