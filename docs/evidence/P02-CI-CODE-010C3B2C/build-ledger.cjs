const fs = require('node:fs'), zlib = require('node:zlib'), crypto = require('node:crypto'), path = require('node:path');
const root = path.resolve(__dirname, '../../..');
const report = zlib.gunzipSync(fs.readFileSync(path.join(root, 'docs/evidence/P02-CI-CODE-010C3A1/plugin-check-results.txt.gz')));
const hash = crypto.createHash('sha256').update(report).digest('hex');
if (hash !== '50f0c694447c4b9aab40b1d66c4091cecdad0614ba18c8f70208c3f2b36fd0bd') throw new Error('Pinned report changed');
const wpcs = JSON.parse(fs.readFileSync(path.join(root, 'tools/code-quality/wpcs-security-dispositions.json'))).securityDbFindings;
const details = {
  'class-cb-user-integrity-migration.php:22': ['DDL identifiers come from inspected ASCII WordPress table names and fixed/hash-generated names; SQL errors fail the explicit install.', 'class-cb-user-integrity.php:17-27; class-cb-user-integrity-migration.php:7-24', 'smoke-user-integrity-migration-real-wp.php'],
  'class-cb-user-integrity-migration.php:29': ['Trigger DDL uses the same validated table identifiers and fixed/hash-generated names; errors fail the explicit install.', 'class-cb-user-integrity.php:6-27; class-cb-user-integrity-migration.php:27-36', 'smoke-user-integrity-migration-real-wp.php'],
  'class-cb-user-integrity.php:75': ['Internal read_var receives fixed version SQL, validated-identifier metadata SQL, or wpdb-prepared metadata values; DB error becomes unreadable.', 'class-cb-user-integrity.php:17-75', 'smoke-user-integrity-verifier-real-wp.php'],
  'class-cb-user-integrity.php:82': ['Internal read_row receives wpdb-prepared information_schema values after table-name validation; DB error becomes unreadable.', 'class-cb-user-integrity.php:17-82', 'smoke-user-integrity-verifier-real-wp.php'],
  'class-cb-deletion-incident-runtime.php:71': ['Fixed SELECT DATABASE() verifies the primary DB name against configured DB_NAME; live read is required before a second connection.', 'class-cb-deletion-incident-runtime.php:59-75', 'smoke-deletion-incident-runtime-real-wp.php'],
  'class-cb-deletion-incident-runtime.php:74': ['Fixed SELECT CONNECTION_ID() verifies the primary connection and a prepared processlist query later proves the second connection points to the same DB.', 'class-cb-deletion-incident-runtime.php:70-83', 'smoke-deletion-incident-runtime-real-wp.php'],
  'class-cb-deletion-incident-runtime.php:78': ['Dedicated mysqli connection is required for durable incident observation outside WordPress deletion transaction; strict stock-wpdb, host, prefix and DB identity guards reject unsupported deployments.', 'class-cb-deletion-incident-runtime.php:59-86', 'smoke-deletion-incident-runtime-real-wp.php'],
  'class-cb-deletion-incident-runtime.php:79': ['mysqli_real_connect uses only configured DB_HOST/USER/PASSWORD/NAME after strict host/port guards; processlist identity check fails closed.', 'class-cb-deletion-incident-runtime.php:59-86', 'smoke-deletion-incident-runtime-real-wp.php'],
  'class-cb-user-integrity-comment-cache.php:17': ['Live comment inventory uses internal comments table and prepared integer user ID before deletion; SQL error throws and stops deletion, caching would miss trigger changes.', 'class-cb-user-integrity-comment-cache.php:13-24', null],
};
let file = '';
const findings = [];
for (const line of report.toString('utf8').split(/\r?\n/)) {
  if (line.startsWith('FILE: ')) { file = line.slice(6); continue; }
  if (!line.startsWith('[') || !/^includes\/class-cb-(user-integrity(?:-migration|-comment-cache)?|deletion-incident-runtime)\.php$/.test(file)) continue;
  for (const item of JSON.parse(line)) {
    if (!item.code.startsWith('WordPress.DB.')) continue;
    const detail = details[`${path.basename(file)}:${item.line}`];
    if (!detail) throw new Error(`Unreviewed ${file}:${item.line}`);
    const sourcePath = `wordpress/carmilla-bridge/${file}`;
    const matches = wpcs.filter(x => x.path === sourcePath && x.line === item.line && x.source === item.code);
    if (matches.length !== 1 || matches[0].disposition !== 'context_justified') throw new Error(`WPCS mismatch ${file}:${item.line}:${item.code}`);
    findings.push({path: sourcePath, line: item.line, column: item.column, code: item.code, message: item.message,
      severity: item.type, sqlOriginAndGuard: detail[0], risk: 'low_validated_or_fixed_fail_closed',
      disposition: 'context_justified', fixTaskId: null, verificationTaskId: 'P02-CI-CODE-010C3B2C',
      sourceTrace: `wordpress/carmilla-bridge/includes/${detail[1]}`,
      testPath: detail[2] ? `wordpress/carmilla-bridge/tests/${detail[2]}` : null,
      wpcs: {column: matches[0].column, disposition: matches[0].disposition, matchBasis: 'path+line+code'}});
  }
}
if (findings.length !== 20 || new Set(findings.map(x => `${x.path}:${x.line}:${x.column}:${x.code}`)).size !== 20) throw new Error('Count/coverage mismatch');
fs.writeFileSync(path.join(__dirname, 'ledger.json'), JSON.stringify({schema: 1, taskId: 'P02-CI-CODE-010C3B2C', reportSha256: hash, count: findings.length, findings}, null, 2) + '\n');
console.log('findings=20 unique=20 wpcsMatched=20');
