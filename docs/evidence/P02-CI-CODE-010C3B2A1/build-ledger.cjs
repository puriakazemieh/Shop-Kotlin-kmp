// Rebuild the first clinic DB review from the pinned shipped ZIP report.
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
  90: ['Live availability SELECT uses an internal bookings table identifier and a prepared therapist ID; SQL errors return unavailable instead of empty availability.', 'class-cb-clinic-controller.php:86-91; helpers.php:540-542', 'smoke-clinic-availability-read-failure-real-wp.php'],
  229: ['Live information_schema engine check binds three WordPress/internal table names as values and rejects missing or non-InnoDB rows before booking.', 'class-cb-clinic-controller.php:227-240', 'smoke-booking-atomic-real-wp.php'],
  241: ['START TRANSACTION is fixed SQL with no variable; failure returns BOOKING_UNAVAILABLE.', 'class-cb-clinic-controller.php:241-243', 'smoke-booking-atomic-real-wp.php'],
  247: ['INSERT query is wpdb-prepared: internal table identifier, bound therapist/slot/user/timestamp values, and unique slot constraint.', 'class-cb-clinic-controller.php:226-255; helpers.php:540-542', 'smoke-booking-atomic-real-wp.php'],
  248: ['Only the internal table identifier is interpolated; all data values use %d/%s placeholders in wpdb->prepare.', 'class-cb-clinic-controller.php:227-250; helpers.php:540-542', 'smoke-booking-atomic-real-wp.php'],
  252: ['ROLLBACK is fixed SQL after duplicate-slot detection, preserving transaction consistency.', 'class-cb-clinic-controller.php:247-253', 'smoke-booking-atomic-real-wp.php'],
  270: ['wpdb->update supplies the internal table name and explicit integer formats for data and WHERE values; failure aborts transaction.', 'class-cb-clinic-controller.php:266-272; helpers.php:540-542', 'smoke-booking-atomic-real-wp.php'],
  271: ['Link verification uses internal table identifier and prepared integer IDs; live read inside transaction verifies the appointment link.', 'class-cb-clinic-controller.php:270-272; helpers.php:540-542', 'smoke-booking-atomic-real-wp.php'],
  276: ['COMMIT is fixed SQL after appointment and link verification; failure enters rollback/error path.', 'class-cb-clinic-controller.php:270-279', 'smoke-booking-atomic-real-wp.php'],
  279: ['ROLLBACK is fixed SQL in exception handler; no request value is interpolated.', 'class-cb-clinic-controller.php:276-282', 'smoke-booking-atomic-real-wp.php'],
};
let file = '';
const findings = [];
for (const line of report.toString('utf8').split(/\r?\n/)) {
  if (line.startsWith('FILE: ')) { file = line.slice(6); continue; }
  if (file !== 'includes/class-cb-clinic-controller.php' || !line.startsWith('[')) continue;
  for (const item of JSON.parse(line)) {
    if (!item.code.startsWith('WordPress.DB.') || item.line > 279) continue;
    const detail = details[item.line];
    if (!detail) throw new Error(`Unreviewed line ${item.line}`);
    const sourcePath = `wordpress/carmilla-bridge/${file}`;
    const matches = wpcs.filter(x => x.path === sourcePath && x.line === item.line && x.source === item.code);
    if (matches.length !== 1 || matches[0].disposition !== 'context_justified') throw new Error(`WPCS mismatch ${item.line}:${item.code}`);
    findings.push({path: sourcePath, line: item.line, column: item.column, code: item.code,
      message: item.message, severity: item.type, sqlOriginAndGuard: detail[0],
      risk: 'low_prepared_or_fixed_sql_live_transaction', disposition: 'context_justified',
      verificationTaskId: 'P02-CI-CODE-010C3B2A1', fixTaskId: null,
      sourceTrace: `wordpress/carmilla-bridge/includes/${detail[1]}`,
      testPath: `wordpress/carmilla-bridge/tests/${detail[2]}`,
      wpcs: {column: matches[0].column, disposition: matches[0].disposition, matchBasis: 'path+line+code; scanner columns differ'}});
  }
}
if (findings.length !== 15 || new Set(findings.map(x => `${x.path}:${x.line}:${x.column}:${x.code}`)).size !== 15) throw new Error('Count/coverage mismatch');
fs.writeFileSync(path.join(__dirname, 'ledger.json'), JSON.stringify({schema: 1, taskId: 'P02-CI-CODE-010C3B2A1', reportSha256: hash, count: findings.length, findings}, null, 2) + '\n');
console.log('findings=15 unique=15 wpcsMatched=15');
