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
  53: 'Fixed START TRANSACTION follows admin capability, nonce, managed-account and integrity-state checks.',
  56: 'Live FOR UPDATE user lock uses the internal users table and prepared integer account ID; caching would invalidate the lock.',
  57: 'Fixed ROLLBACK follows SQL error or invalid locked row; fail closed.',
  59: 'Fixed ROLLBACK follows unavailable dependency read; fail closed.',
  60: 'Fixed ROLLBACK protects an account with posts or comments.',
  61: 'wpdb->delete uses internal usermeta table and explicit integer user_id format.',
  62: 'wpdb->delete uses internal users table and explicit integer ID format.',
  64: 'Live post-delete verification uses internal users table and prepared integer ID; caching would hide partial deletion.',
  65: 'Fixed ROLLBACK follows failed delete or nonzero verification count.',
  66: 'Fixed COMMIT runs only after deletion and zero-count verification.',
  70: 'Fixed ROLLBACK runs in exception path.',
  80: 'Live dependent-post count uses internal posts table and prepared integer author ID; stale cache could allow unsafe deletion.',
  83: 'Live dependent-comment count uses internal comments table and prepared integer user ID; stale cache could allow unsafe deletion.',
};
let file = '';
const findings = [];
for (const line of report.toString('utf8').split(/\r?\n/)) {
  if (line.startsWith('FILE: ')) { file = line.slice(6); continue; }
  if (file !== 'includes/class-cb-managed-user-deletion.php' || !line.startsWith('[')) continue;
  for (const item of JSON.parse(line)) {
    if (!item.code.startsWith('WordPress.DB.')) continue;
    if (!details[item.line]) throw new Error(`Unreviewed line ${item.line}`);
    const sourcePath = `wordpress/carmilla-bridge/${file}`;
    const matches = wpcs.filter(x => x.path === sourcePath && x.line === item.line && x.source === item.code);
    if (matches.length !== 1 || matches[0].disposition !== 'context_justified') throw new Error(`WPCS mismatch ${item.line}:${item.code}`);
    findings.push({path: sourcePath, line: item.line, column: item.column, code: item.code,
      message: item.message, severity: item.type, sqlOriginAndGuard: details[item.line],
      risk: 'low_fixed_or_prepared_transaction_fail_closed', disposition: 'context_justified', fixTaskId: null,
      verificationTaskId: 'P02-CI-CODE-010C3B2B', sourceTrace: `wordpress/carmilla-bridge/includes/class-cb-managed-user-deletion.php:38-85`,
      testPath: 'wordpress/carmilla-bridge/tests/smoke-managed-user-deletion-real-wp.php',
      wpcs: {column: matches[0].column, disposition: matches[0].disposition, matchBasis: 'path+line+code'}});
  }
}
if (findings.length !== 17 || new Set(findings.map(x => `${x.path}:${x.line}:${x.column}:${x.code}`)).size !== 17) throw new Error('Count/coverage mismatch');
fs.writeFileSync(path.join(__dirname, 'ledger.json'), JSON.stringify({schema: 1, taskId: 'P02-CI-CODE-010C3B2B', reportSha256: hash, count: findings.length, findings}, null, 2) + '\n');
console.log('findings=17 unique=17 wpcsMatched=17');
