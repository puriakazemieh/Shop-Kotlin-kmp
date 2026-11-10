// Rebuild nonce findings from the pinned shipped Bridge Plugin Check report.
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
  'class-cb-integrations-controller.php:82': ['GET tab only selects one of five literal admin views after can_manage_integrations; settings writes use a separate form and nonce path.', 'class-cb-integrations-controller.php:78-94,189', 'smoke-integrations-form-http-real-wp.php', 'admin GET view selector'],
  'class-cb-manifest-controller.php:59': ['GET error string is sanitized, then escaped into a notice after admin capability; POST save has a separate admin nonce gate.', 'class-cb-manifest-controller.php:24-28,54-65', 'smoke-manifest-notice-real-wp.php', 'admin GET notice'],
  'class-cb-manifest-controller.php:60': ['GET error string is sanitized, then escaped into a notice after admin capability; POST save has a separate admin nonce gate.', 'class-cb-manifest-controller.php:24-28,54-65', 'smoke-manifest-notice-real-wp.php', 'admin GET notice'],
  'class-cb-manifest-controller.php:66': ['GET updated flag selects only fixed success copy after admin capability; POST save has a separate admin nonce gate.', 'class-cb-manifest-controller.php:24-28,54-67', 'smoke-manifest-notice-real-wp.php', 'admin GET notice'],
  'class-cb-managed-user-deletion.php:28': ['GET status selects escaped fixed notice after admin capability; destructive POST checks admin nonce and exact confirmation in handle_delete.', 'class-cb-managed-user-deletion.php:26-44', 'smoke-managed-user-deletion-real-wp.php', 'admin GET notice'],
  'class-cb-admin-content-controller.php:504': ['Multipart file presence is checked inside an admin REST POST route. WordPress REST cookie nonce or signed Bearer authentication runs before the require_admin permission callback; upload runs only after that boundary.', 'class-cb-admin-content-controller.php:20-38,503-510; class-cb-plugin.php:175-196,242-244', 'probe-root-alias-auth-http-real-wp.php', 'authenticated admin REST upload'],
  'class-cb-admin-content-controller.php:551': ['Multipart file presence is checked inside an admin REST POST route. WordPress REST cookie nonce or signed Bearer authentication runs before the require_admin permission callback; upload runs only after that boundary.', 'class-cb-admin-content-controller.php:37-39,550-557; class-cb-plugin.php:175-196,242-244', 'probe-root-alias-auth-http-real-wp.php', 'authenticated admin REST upload'],
  'class-cb-admin-product-controller.php:67': ['Multipart file presence is checked inside an admin REST POST route, protected by WordPress REST authentication and require_admin before callback.', 'class-cb-admin-product-controller.php:26-30,66-74; class-cb-plugin.php:175-196,242-244', 'probe-root-alias-auth-http-real-wp.php', 'authenticated admin REST upload'],
  'class-cb-academy-controller.php:650': ['Multipart project file presence is checked inside a login-gated REST POST route. WordPress cookie nonce or signed Bearer authentication precedes require_login and callback.', 'class-cb-academy-controller.php:47-63,648-656; class-cb-plugin.php:175-196,235-240', 'probe-root-alias-auth-http-real-wp.php', 'authenticated user REST upload'],
};
let file = '';
const findings = [];
for (const line of report.toString('utf8').split(/\r?\n/)) {
  if (line.startsWith('FILE: ')) { file = line.slice(6); continue; }
  if (!line.startsWith('[')) continue;
  for (const item of JSON.parse(line)) {
    if (!item.code.startsWith('WordPress.Security.NonceVerification.')) continue;
    const key = `${path.basename(file)}:${item.line}`;
    const detail = details[key];
    if (!detail) throw new Error(`Unreviewed finding ${key}`);
    const sourcePath = `wordpress/carmilla-bridge/${file}`;
    const matches = wpcs.filter(x => x.path === sourcePath && x.line === item.line && x.source === item.code);
    const occurrence = findings.filter(x => x.path === sourcePath && x.line === item.line && x.code === item.code).length;
    const match = matches.sort((a, b) => a.column - b.column)[occurrence];
    if (!match || match.disposition !== 'context_justified') throw new Error(`WPCS mismatch ${key}`);
    findings.push({path: sourcePath, line: item.line, column: item.column, code: item.code,
      message: item.message, severity: item.type, requestBoundary: detail[3],
      disposition: 'context_justified', risk: 'low_after_authentication_or_read_only_selection',
      reason: detail[0], sourceTrace: `wordpress/carmilla-bridge/includes/${detail[1]}`,
      verificationTaskId: 'P02-CI-CODE-010C3B1B', testPath: `wordpress/carmilla-bridge/tests/${detail[2]}`,
      wpcs: {column: match.column, disposition: match.disposition, matchBasis: 'path+line+code+occurrence; scanner columns may differ'}});
  }
}
if (findings.length !== 13 || findings.filter(x => x.wpcs.column).length !== 13) throw new Error('Count/coverage mismatch');
fs.writeFileSync(path.join(__dirname, 'ledger.json'), JSON.stringify({schema: 1, taskId: 'P02-CI-CODE-010C3B1B', pluginCheckReportSha256: hash, count: findings.length, findings}, null, 2) + '\n');
console.log(`findings=${findings.length} locations=${new Set(findings.map(x => `${x.path}:${x.line}:${x.column}`)).size} wpcsMatched=13`);
