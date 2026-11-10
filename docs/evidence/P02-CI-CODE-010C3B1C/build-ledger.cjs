// Rebuild shipped output-escaping findings from the pinned Plugin Check report.
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
  'class-cb-integrations-controller.php:18': ['wp_die HTML message', 'Fixed translation string with no request data after failed capability check; translation catalog is trusted plugin content.', 'class-cb-integrations-controller.php:16-21', 'smoke-integrations-form-http-real-wp.php'],
  'class-cb-integrations-controller.php:28': ['wp_die HTML message', 'Fixed translation string for unsupported enumerated message type; no request value is interpolated.', 'class-cb-integrations-controller.php:23-32', 'smoke-integrations-form-http-real-wp.php'],
  'class-cb-integrations-controller.php:32': ['wp_die HTML message', 'Fixed translation string for empty recipient/body; no request value is interpolated.', 'class-cb-integrations-controller.php:23-32', 'smoke-integrations-form-http-real-wp.php'],
  'class-cb-integrations-controller.php:56': ['wp_die HTML message', 'Fixed translated prefix is concatenated only with esc_html-escaped exception detail.', 'class-cb-integrations-controller.php:54-57', 'smoke-integrations-form-http-real-wp.php'],
  'class-cb-deletion-incident-notice.php:27': ['admin notice HTML text', 'Code selects one of two esc_html__ fixed strings; account ID is cast to int and escaped before output.', 'class-cb-deletion-incident-notice.php:11-29', 'smoke-deletion-incident-notice-real-wp.php'],
  'includes/Migration/Import_Command.php:61': ['CLI exception message', 'Line number is a local integer array index plus one; no NDJSON line content is included and no HTML sink exists here.', 'includes/Migration/Import_Command.php:53-66', null],
  'tools/user-integrity.php:37': ['CLI process exit status', 'Result code is a fixed internal integer status and exit() emits no HTML or text.', 'tools/user-integrity.php:11-37', 'smoke-user-integrity-operator-real-wp.php'],
};
let file = '';
const findings = [];
for (const line of report.toString('utf8').split(/\r?\n/)) {
  if (line.startsWith('FILE: ')) { file = line.slice(6); continue; }
  if (!line.startsWith('[')) continue;
  for (const item of JSON.parse(line)) {
    if (!item.code.startsWith('WordPress.Security.EscapeOutput.')) continue;
    const key = `${file.startsWith('includes/Migration/') || file.startsWith('tools/') ? file : path.basename(file)}:${item.line}`;
    const detail = details[key];
    if (!detail) throw new Error(`Unreviewed finding ${key}`);
    const sourcePath = `wordpress/carmilla-bridge/${file}`;
    const matches = wpcs.filter(x => x.path === sourcePath && x.line === item.line && x.source === item.code);
    if (matches.length !== 1 || matches[0].disposition !== 'context_justified') throw new Error(`WPCS mismatch ${key}`);
    findings.push({path: sourcePath, line: item.line, column: item.column, code: item.code,
      message: item.message, severity: item.type, outputContext: detail[0], dataOriginAndRisk: detail[1],
      disposition: 'context_justified', risk: 'low_trusted_or_already_escaped',
      sourceTrace: `wordpress/carmilla-bridge/${detail[2].startsWith('includes/') || detail[2].startsWith('tools/') ? '' : 'includes/'}${detail[2]}`, verificationTaskId: 'P02-CI-CODE-010C3B1C',
      testPath: detail[3] ? `wordpress/carmilla-bridge/tests/${detail[3]}` : null,
      wpcs: {column: matches[0].column, disposition: matches[0].disposition, matchBasis: 'path+line+code'}});
  }
}
if (findings.length !== 7 || new Set(findings.map(x => `${x.path}:${x.line}:${x.column}`)).size !== 7) throw new Error('Count/coverage mismatch');
fs.writeFileSync(path.join(__dirname, 'ledger.json'), JSON.stringify({schema: 1, taskId: 'P02-CI-CODE-010C3B1C', pluginCheckReportSha256: hash, count: findings.length, findings}, null, 2) + '\n');
console.log('findings=7 locations=7 wpcsMatched=7');
