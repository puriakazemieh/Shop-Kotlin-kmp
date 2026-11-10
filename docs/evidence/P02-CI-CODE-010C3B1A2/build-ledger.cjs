// Rebuild remaining input findings from the pinned shipped Bridge scan.
const fs = require('node:fs');
const zlib = require('node:zlib');
const crypto = require('node:crypto');
const path = require('node:path');
const root = path.resolve(__dirname, '../../..');
const report = zlib.gunzipSync(fs.readFileSync(path.join(root, 'docs/evidence/P02-CI-CODE-010C3A1/plugin-check-results.txt.gz')));
const hash = crypto.createHash('sha256').update(report).digest('hex');
if (hash !== '50f0c694447c4b9aab40b1d66c4091cecdad0614ba18c8f70208c3f2b36fd0bd') throw new Error('Pinned report hash changed');
const wpcs = JSON.parse(fs.readFileSync(path.join(root, 'tools/code-quality/wpcs-security-dispositions.json'))).securityDbFindings;
const details = {
  'includes/class-cb-manifest-controller.php:30:6': ['Admin capability and nonce precede this read. Only known feature IDs are projected to booleans; submitted values and unknown keys are not persisted.', 'wordpress/carmilla-bridge/includes/class-cb-manifest-controller.php:24-44', 'wordpress/carmilla-bridge/tests/smoke-manifest-security.php'],
  'tools/user-integrity.php:33:16': ['SCRIPT_FILENAME is compared only by realpath to the fixed current file to select CLI entry; no HTML, SQL, or dynamic file open uses it.', 'wordpress/carmilla-bridge/tools/user-integrity.php:33-37', 'wordpress/carmilla-bridge/tests/smoke-user-integrity-operator-real-wp.php'],
  'includes/meta-boxes.php:134:23': ['Nonce, edit_post capability, known field key, and scalar shape checks precede wp_unslash; field-type sanitization precedes update_post_meta.', 'wordpress/carmilla-bridge/includes/meta-boxes.php:116-145', 'wordpress/carmilla-bridge/tests/smoke-meta-box-scalar-shape-real-wp.php'],
  'includes/class-cb-local-qa-otp-sink.php:30:90': ['Localhost-only feature and admin capability gate the page; value is unslashed, hashed into an exact transient lookup key, and escaped if echoed. Nonce gates lookup.', 'wordpress/carmilla-bridge/includes/class-cb-local-qa-otp-sink.php:9-44', 'wordpress/carmilla-bridge/tests/smoke-local-otp-sink-real-wp.php'],
  'includes/class-cb-managed-user-deletion.php:42:98': ['Admin capability and nonce precede an unslashed exact-word hash_equals comparison; only the boolean result controls deletion.', 'wordpress/carmilla-bridge/includes/class-cb-managed-user-deletion.php:26-44', 'wordpress/carmilla-bridge/tests/smoke-managed-user-deletion-real-wp.php'],
  'packages/carmilla-core/inc/Health/HealthChecker.php:243:41': ['SERVER_SOFTWARE is server-provided environment metadata placed in a diagnostic array; repository search found no production consumer that emits it as HTML or SQL.', 'wordpress/packages/carmilla-core/inc/Health/HealthChecker.php:228-247', 'wordpress/packages/carmilla-core/tests/smoke-carmilla-core.php'],
};
const decode = s => s.replace(/&#039;/g, "'").replace(/&quot;/g, '"').replace(/&amp;/g, '&');
let file = '';
const findings = [];
for (const line of report.toString('utf8').split(/\r?\n/)) {
  if (line.startsWith('FILE: ')) { file = line.slice(6); continue; }
  if (!line.startsWith('[')) continue;
  for (const item of JSON.parse(line)) {
    if (!item.code.startsWith('WordPress.Security.ValidatedSanitizedInput.') || /^includes\/class-cb-(plugin|web-auth-controller)\.php$/.test(file)) continue;
    const key = `${file}:${item.line}:${item.column}`;
    const detail = details[key];
    if (!detail) throw new Error(`Unreviewed finding ${key}`);
    const sourcePath = file.startsWith('packages/') ? `wordpress/${file}` : `wordpress/carmilla-bridge/${file}`;
    const matches = wpcs.filter(x => x.path === sourcePath && x.line === item.line && x.source === item.code);
    const match = matches[0] || null;
    if (matches.length > 1 || (match && match.disposition !== 'context_justified')) throw new Error(`WPCS mismatch ${key}`);
    findings.push({path: sourcePath, shippedZipPath: file, line: item.line, column: item.column,
      code: item.code, message: decode(item.message), severity: item.type,
      disposition: 'context_justified', risk: 'low_after_validation', reason: detail[0],
      sourceTrace: detail[1], verificationTaskId: 'P02-CI-CODE-010C3B1A2', testPath: detail[2],
      wpcs: match ? {column: match.column, disposition: match.disposition, matchBasis: 'path+line+code; scanner columns differ'} : {matchBasis: 'no entry: bundled core absent from prior WPCS scan root'}});
  }
}
if (findings.length !== 9 || new Set(findings.map(x => `${x.shippedZipPath}:${x.line}:${x.column}`)).size !== 6 || findings.filter(x => x.wpcs.column).length !== 7) throw new Error('Coverage mismatch');
fs.writeFileSync(path.join(__dirname, 'ledger.json'), JSON.stringify({schema: 1, taskId: 'P02-CI-CODE-010C3B1A2', pluginCheckReportSha256: hash, count: findings.length, findings}, null, 2) + '\n');
console.log('findings=9 locations=6 wpcsMatched=7 bundledCoreUnmatched=2');
