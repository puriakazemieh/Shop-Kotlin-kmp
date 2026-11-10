// Rebuild the exact Plugin Check finding ledger from the pinned, shipped ZIP report.
const fs = require('node:fs');
const zlib = require('node:zlib');
const crypto = require('node:crypto');
const path = require('node:path');

const root = path.resolve(__dirname, '../../..');
const reportPath = path.join(root, 'docs/evidence/P02-CI-CODE-010C3A1/plugin-check-results.txt.gz');
const report = zlib.gunzipSync(fs.readFileSync(reportPath));
const hash = crypto.createHash('sha256').update(report).digest('hex');
const expected = '50f0c694447c4b9aab40b1d66c4091cecdad0614ba18c8f70208c3f2b36fd0bd';
if (hash !== expected) throw new Error('Pinned report hash changed');
const wpcs = JSON.parse(fs.readFileSync(path.join(root, 'tools/code-quality/wpcs-security-dispositions.json'))).securityDbFindings;
const details = {
  'class-cb-web-auth-controller.php:93:68': ['REMOTE_ADDR is string and IP validated, then constrained by direct-peer or proxy policy.', 'class-cb-web-request-policy.php:114-140', 'smoke-web-request-policy.php'],
  'class-cb-web-auth-controller.php:93:116': ['SERVER_ADDR is string and IP validated; local QA additionally checks loopback address.', 'class-cb-web-request-policy.php:114-140', 'smoke-web-request-policy.php'],
  'class-cb-web-auth-controller.php:93:158': ['HTTPS is string checked and compared against exact TLS or local QA protocol values.', 'class-cb-web-request-policy.php:114-140', 'smoke-web-request-policy.php'],
  'class-cb-web-auth-controller.php:105:129': ['HTTP_HOST is string checked, control characters and duplicate headers are rejected, and canonical origin must equal configured site origin.', 'class-cb-web-request-policy.php:118-120,143-167', 'smoke-web-request-policy.php'],
  'class-cb-web-auth-controller.php:110:69': ['Cookie is an opaque exact-byte credential. Store digest accepts only 64 lowercase hex characters before rotate or revoke; unslashing would alter credentials.', 'class-cb-web-session-store.php:100-101,149-150,226-227', 'smoke-web-refresh-logout-real-wp.php'],
  'class-cb-plugin.php:101:83': ['REQUEST_METHOD is uppercased and compared only with literal OPTIONS for preflight; it is not emitted or used as a dynamic route.', 'class-cb-plugin.php:98-105', 'smoke-web-request-policy.php'],
  'class-cb-plugin.php:115:68': ['REQUEST_URI is parsed to path and restricted to four literal root prefixes before WordPress REST routing and route authentication; the log uses only the matched literal prefix.', 'class-cb-plugin.php:114-135', 'probe-root-alias-malformed-http-real-wp.php'],
  'class-cb-plugin.php:204:14': ['HTTP_AUTHORIZATION supplies an opaque Bearer credential; JWT or browser verifier validates signature and claims before user identity is accepted.', 'class-cb-plugin.php:175-196,201-219', 'probe-root-alias-foreign-token-http-real-wp.php'],
  'class-cb-plugin.php:206:14': ['REDIRECT_HTTP_AUTHORIZATION follows the same Bearer extraction and signed-token verification path.', 'class-cb-plugin.php:175-196,201-219', 'probe-root-alias-foreign-token-http-real-wp.php'],
};
const decode = s => s.replace(/&#039;/g, "'").replace(/&quot;/g, '"').replace(/&amp;/g, '&');
let file = '';
const findings = [];
for (const line of report.toString('utf8').split(/\r?\n/)) {
  if (line.startsWith('FILE: ')) { file = line.slice(6); continue; }
  if (!/^includes\/class-cb-(web-auth-controller|plugin)\.php$/.test(file) || !line.startsWith('[')) continue;
  for (const item of JSON.parse(line)) {
    if (!item.code.startsWith('WordPress.Security.ValidatedSanitizedInput.')) continue;
    const key = `${path.basename(file)}:${item.line}:${item.column}`;
    const detail = details[key];
    if (!detail) throw new Error(`Unreviewed finding ${key}`);
    const full = `wordpress/carmilla-bridge/${file}`;
    const matches = wpcs.filter(x => x.path === full && x.line === item.line && x.source === item.code);
    const sameScanner = findings.filter(x => x.path === full && x.line === item.line && x.code === item.code).length;
    const match = matches.sort((a, b) => a.column - b.column)[sameScanner];
    if (!match || match.disposition !== 'context_justified') throw new Error(`WPCS mismatch ${key}`);
    findings.push({path: full, line: item.line, column: item.column, code: item.code,
      message: decode(item.message), severity: item.type, disposition: 'context_justified',
      risk: 'low_after_validation', reason: detail[0],
      sourceTrace: `wordpress/carmilla-bridge/includes/${detail[1]}`,
      verificationTaskId: 'P02-CI-CODE-010C3B1A1',
      testPath: `wordpress/carmilla-bridge/tests/${detail[2]}`,
      wpcs: {column: match.column, disposition: match.disposition, matchBasis: 'path+line+code+occurrence; scanner columns differ'}});
  }
}
if (findings.length !== 18 || Object.keys(details).length !== new Set(findings.map(x => `${path.basename(x.path)}:${x.line}:${x.column}`)).size) throw new Error('Count/coverage mismatch');
fs.writeFileSync(path.join(__dirname, 'ledger.json'), JSON.stringify({schema: 1, taskId: 'P02-CI-CODE-010C3B1A1', pluginCheckReportSha256: hash, count: findings.length, findings}, null, 2) + '\n');
console.log(`findings=${findings.length} locations=${Object.keys(details).length} wpcsMatched=${findings.length}`);
