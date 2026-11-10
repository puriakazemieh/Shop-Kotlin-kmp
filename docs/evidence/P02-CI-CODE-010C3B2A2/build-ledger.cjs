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
  299: ['Live InnoDB engine inspection binds two internal table names as values and fails closed on missing/error rows.', 'class-cb-clinic-controller.php:296-309', 'smoke-booking-cancel-atomic-real-wp.php'],
  311: ['START TRANSACTION is fixed SQL with no request data; error returns CANCELLATION_UNAVAILABLE.', 'class-cb-clinic-controller.php:311-313', 'smoke-booking-cancel-atomic-real-wp.php'],
  316: ['FOR UPDATE lock query uses internal bookings table identifier and prepared therapist ID/slot; live row lock must bypass cache.', 'class-cb-clinic-controller.php:314-317; helpers.php:540-542', 'smoke-booking-cancel-atomic-real-wp.php'],
  319: ['FOR UPDATE status read uses internal postmeta table and prepared post ID; fixed meta key and SQL-error check.', 'class-cb-clinic-controller.php:318-320', 'smoke-booking-cancel-atomic-real-wp.php'],
  322: ['wpdb->delete uses internal bookings table and integer WHERE format; failure aborts transaction.', 'class-cb-clinic-controller.php:321-326; helpers.php:540-542', 'smoke-booking-cancel-atomic-real-wp.php'],
  323: ['COMMIT is fixed SQL after lock, status and release; failure enters rollback path.', 'class-cb-clinic-controller.php:319-328', 'smoke-booking-cancel-atomic-real-wp.php'],
  326: ['ROLLBACK is fixed SQL in catch path with no variable.', 'class-cb-clinic-controller.php:323-329', 'smoke-booking-cancel-atomic-real-wp.php'],
  338: ['get_posts meta_key filters appointment ownership; numberposts=100 bounds output but meta scan needs indexed ownership work in P14.', 'class-cb-clinic-controller.php:332-346', 'smoke-booking-atomic-real-wp.php'],
  339: ['get_posts meta_value is the current integer user ID; ownership meta scan needs indexed ownership work in P14.', 'class-cb-clinic-controller.php:332-346', 'smoke-booking-atomic-real-wp.php'],
  473: ['get_comments meta_key filters message ownership; current listing lacks bounded pagination, assigned to P14.', 'class-cb-clinic-controller.php:467-480', null],
  474: ['get_comments meta_value is the current integer user ID; current listing lacks bounded pagination, assigned to P14.', 'class-cb-clinic-controller.php:467-480', null],
  519: ['Message count filters current user via commentmeta key/value. An indexed ownership predicate is assigned to P14.', 'class-cb-clinic-controller.php:516-524', null],
};
const targets = {338: 'P14-CLINIC-PERF-CODE-034A', 339: 'P14-CLINIC-PERF-CODE-034A', 473: 'P14-CLINIC-PERF-CODE-034B', 474: 'P14-CLINIC-PERF-CODE-034B', 519: 'P14-CLINIC-PERF-CODE-034C'};
let file = '';
const findings = [];
for (const line of report.toString('utf8').split(/\r?\n/)) {
  if (line.startsWith('FILE: ')) { file = line.slice(6); continue; }
  if (file !== 'includes/class-cb-clinic-controller.php' || !line.startsWith('[')) continue;
  for (const item of JSON.parse(line)) {
    if (!item.code.startsWith('WordPress.DB.') || item.line < 299) continue;
    const detail = details[item.line];
    if (!detail) throw new Error(`Unreviewed line ${item.line}`);
    const sourcePath = `wordpress/carmilla-bridge/${file}`;
    const matches = wpcs.filter(x => x.path === sourcePath && x.line === item.line && x.source === item.code);
    if (matches.length !== 1) throw new Error(`WPCS mismatch ${item.line}:${item.code}`);
    const target = targets[item.line] || null;
    if (matches[0].disposition !== (target ? 'deferred' : 'context_justified') || (matches[0].targetTask || null) !== target) throw new Error(`Disposition mismatch ${item.line}`);
    findings.push({path: sourcePath, line: item.line, column: item.column, code: item.code,
      message: item.message, severity: item.type, sqlOriginAndGuard: detail[0],
      risk: target ? 'performance_work_pending' : 'low_fixed_or_prepared_live_transaction',
      disposition: target ? 'deferred_to_clinic_phase' : 'context_justified', fixTaskId: target,
      verificationTaskId: 'P02-CI-CODE-010C3B2A2', sourceTrace: `wordpress/carmilla-bridge/includes/${detail[1]}`,
      testPath: detail[2] ? `wordpress/carmilla-bridge/tests/${detail[2]}` : null,
      wpcs: {column: matches[0].column, disposition: matches[0].disposition, targetTask: matches[0].targetTask || null, matchBasis: 'path+line+code'}});
  }
}
if (findings.length !== 17 || findings.filter(x => x.fixTaskId).length !== 6) throw new Error('Count/coverage mismatch');
fs.writeFileSync(path.join(__dirname, 'ledger.json'), JSON.stringify({schema: 1, taskId: 'P02-CI-CODE-010C3B2A2', reportSha256: hash, count: findings.length, findings}, null, 2) + '\n');
console.log('findings=17 context=11 deferredToP14=6 wpcsMatched=17');
