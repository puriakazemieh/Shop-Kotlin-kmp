// Rebuild direct-DB security findings from the pinned shipped Bridge scan.
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
  'includes/class-cb-user-integrity-migration.php:22': ['DDL identifiers derive from WordPress table names or fixed/hash-generated names. inspect validates all table names against ASCII identifier allowlist and schema before install; SQL values are not request-supplied.', 'class-cb-user-integrity.php:17-53; class-cb-user-integrity-migration.php:7-24', 'smoke-user-integrity-migration-real-wp.php'],
  'includes/class-cb-user-integrity-migration.php:29': ['Trigger DDL uses the same inspected table identifiers and fixed/hash-generated constraint names; no request-supplied SQL fragments.', 'class-cb-user-integrity.php:6-27; class-cb-user-integrity-migration.php:27-31', 'smoke-user-integrity-migration-real-wp.php'],
  'includes/class-cb-user-integrity.php:75': ['Internal read_var receives fixed SQL or wpdb->prepare output, plus a query built only after table-name identifier allowlist and schema checks.', 'class-cb-user-integrity.php:17-40,58-75', 'smoke-user-integrity-verifier-real-wp.php'],
  'includes/class-cb-user-integrity.php:82': ['Internal read_row receives wpdb->prepare output from fixed information_schema queries with bound table/column/constraint values.', 'class-cb-user-integrity.php:28-61,79-83', 'smoke-user-integrity-verifier-real-wp.php'],
  'includes/Migration/Export_Command.php:34': ['Denied post types are a fixed Migration_Config list and each value is esc_sql-escaped before quoting. No request input reaches the SQL list. Placeholder conversion is queued for the migration security gate.', 'Migration/Migration_Config.php:12-23; Migration/Export_Command.php:26-35', 'smoke-migration-export-no-plaintext.php'],
  'includes/Migration/Export_Overlay_Command.php:35': ['Denied post types are the fixed escaped Migration_Config list; seed table is WordPress configured prefix plus a literal suffix. Placeholder conversion is queued for the migration security gate.', 'Migration/Migration_Config.php:12-23; Migration/Export_Overlay_Command.php:26-35', 'smoke-migration-export-no-plaintext.php'],
  'includes/class-cb-admin-content-controller.php:465': ['Table identifier is cb_bookings_table: configured wpdb prefix plus a fixed suffix; therapist ID is bound with %d through wpdb->prepare.', 'helpers.php:540-542; class-cb-admin-content-controller.php:458-468', 'smoke-clinic-availability-read-failure-real-wp.php'],
  'includes/class-cb-clinic-controller.php:90': ['Table identifier is configured wpdb prefix plus fixed suffix; therapist ID is bound with %d through wpdb->prepare.', 'helpers.php:540-542; class-cb-clinic-controller.php:86-91', 'smoke-clinic-availability-read-failure-real-wp.php'],
  'includes/class-cb-clinic-controller.php:247': ['Booking table identifier is internal; therapist, slot, user and timestamp values use %d/%s placeholders. Transaction checks engine and rolls back on failure.', 'helpers.php:540-542; class-cb-clinic-controller.php:227-253', 'smoke-booking-atomic-real-wp.php'],
  'includes/class-cb-clinic-controller.php:271': ['Booking table identifier is internal; booking, therapist and user IDs use bound %d placeholders.', 'helpers.php:540-542; class-cb-clinic-controller.php:270-272', 'smoke-booking-atomic-real-wp.php'],
  'includes/class-cb-clinic-controller.php:316': ['Booking table identifier is internal; therapist ID and slot are bound via %d/%s in the FOR UPDATE query.', 'helpers.php:540-542; class-cb-clinic-controller.php:296-317', 'smoke-booking-cancel-atomic-real-wp.php'],
};
const decode = s => s.replace(/&#039;/g, "'").replace(/&gt;/g, '>').replace(/&amp;/g, '&');
let file = '';
const findings = [];
for (const line of report.toString('utf8').split(/\r?\n/)) {
  if (line.startsWith('FILE: ')) { file = line.slice(6); continue; }
  if (!line.startsWith('[')) continue;
  for (const item of JSON.parse(line)) {
    if (item.code !== 'PluginCheck.Security.DirectDB.UnescapedDBParameter') continue;
    const key = `${file}:${item.line}`;
    const detail = details[key];
    if (!detail) throw new Error(`Unreviewed finding ${key}`);
    const deferred = file.startsWith('includes/Migration/');
    const sourcePath = `wordpress/carmilla-bridge/${file}`;
    const nearby = wpcs.filter(x => x.path === sourcePath && Math.abs(x.line - item.line) <= 1 && x.source.startsWith('WordPress.DB.PreparedSQL.'));
    if (!nearby.length) throw new Error(`No nearby WPCS PreparedSQL finding ${key}`);
    findings.push({path: sourcePath, line: item.line, column: item.column, code: item.code,
      message: decode(item.message), severity: item.type, variableOriginAndGuard: detail[0],
      sqlInjectionRisk: deferred ? 'low_static_values_but_placeholder_work_pending' : 'low_validated_identifier_or_prepared_values',
      disposition: deferred ? 'deferred_to_migration_security_gate' : 'context_justified',
      fixTaskId: deferred ? 'P07-MIGRATION-SEC-CODE-028' : null,
      verificationTaskId: 'P02-CI-CODE-010C3B1D',
      sourceTrace: `wordpress/carmilla-bridge/includes/${detail[1]}`,
      testPath: `wordpress/carmilla-bridge/tests/${detail[2]}`,
      wpcsNearby: nearby.map(x => ({line: x.line, column: x.column, source: x.source, disposition: x.disposition, targetTask: x.targetTask || null})),
      comparisonNote: 'Plugin Check DirectDB and WPCS PreparedSQL are different rules; nearby source lines, not identical finding IDs.'});
  }
}
if (findings.length !== 11 || findings.filter(x => x.fixTaskId).length !== 2) throw new Error('Count/coverage mismatch');
fs.writeFileSync(path.join(__dirname, 'ledger.json'), JSON.stringify({schema: 1, taskId: 'P02-CI-CODE-010C3B1D', pluginCheckReportSha256: hash, count: findings.length, findings}, null, 2) + '\n');
console.log('findings=11 deferredToP07=2 wpcsNearby=11');
