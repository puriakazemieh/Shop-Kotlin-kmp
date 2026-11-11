#!/usr/bin/env node
/**
 * Test suite for Theme Check exact baseline enforcer.
 * Covers positive, negative, tamper, and version drift scenarios.
 * Task: P02-CI-CODE-010C3E
 */
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const cp = require('node:child_process');

const root = path.resolve(__dirname, '../..');
const checker = path.join(__dirname, 'check_theme_check_baseline.cjs');
const baseline = JSON.parse(fs.readFileSync(path.join(__dirname, 'theme-check-baseline.json'), 'utf8'));
const pinnedReport = JSON.parse(fs.readFileSync(path.join(root, 'docs/evidence/P02-CI-CODE-010C3A1/theme-check.json'), 'utf8'));

const tempDir = fs.mkdtempSync(path.join(os.tmpdir(), 'cb-theme-baseline-'));
const defaultArgs = report => [
  checker,
  '--report', report,
  '--scanner-version', '20260901',
  '--wp-version', '6.4.3'
];

function writeReport(name, items) {
  const filename = path.join(tempDir, name);
  fs.writeFileSync(filename, JSON.stringify(items, null, 2), 'utf8');
  return filename;
}

function runCheck(name, filename, expectPass, extraArgs = [], extraEnv = {}) {
  const result = cp.spawnSync(process.execPath, [...defaultArgs(filename), ...extraArgs], {
    encoding: 'utf8',
    env: { ...process.env, ...extraEnv }
  });

  const passed = (result.status === 0);
  if (passed !== expectPass) {
    throw new Error(`${name}: expected ${expectPass ? 'PASS' : 'FAIL'}, got exit code ${result.status}.\nSTDOUT: ${result.stdout}\nSTDERR: ${result.stderr}`);
  }
  console.log(`PASS ${name}`);
}

try {
  console.log('=== Running Theme Check Baseline Scenario Tests ===\n');

  // 1. Positive: Approved subset (only the 32 allowed findings)
  const allowedItems = pinnedReport.filter(p => {
    const match = baseline.findings.find(b => b.type === p.type && b.value === p.value.trim());
    return match && match.policy === 'allow';
  });
  if (allowedItems.length !== 32) {
    throw new Error(`Expected 32 allowed items from pinned report, got ${allowedItems.length}`);
  }
  const approvedFile = writeReport('approved-subset.json', allowedItems);
  runCheck('approved subset of 32 findings passes', approvedFile, true);

  // 2. Positive: Even fewer findings (improvements / resolved items)
  const fewerItems = allowedItems.slice(2);
  const fewerFile = writeReport('fewer-items.json', fewerItems);
  runCheck('fewer findings (resolved allowed debt) passes', fewerFile, true);

  // 3. Negative: Original un-remediated report (contains 26 must_disappear findings)
  const originalFile = writeReport('original-pinned.json', pinnedReport);
  runCheck('original unremediated report rejected (26 must_disappear items)', originalFile, false);

  // 4. Negative: Previously fixed P02 findings individually rejected
  const p02Tasks = ['P02-SEC-CODE-010C3D1', 'P02-THEME-CODE-010C3D2', 'P02-THEME-CODE-010C3D3', 'P02-CORE-CODE-010C3D4'];
  for (const taskId of p02Tasks) {
    const fixedItem = baseline.findings.find(b => b.fixTaskId === taskId);
    if (!fixedItem) throw new Error(`Missing finding for ${taskId}`);
    const regressed = [...allowedItems, { type: fixedItem.type, value: fixedItem.value }];
    runCheck(`regression for ${taskId} rejected`, writeReport(`regression-${taskId}.json`, regressed), false);
  }

  // 5. Negative: Brand new findings of each severity type rejected
  for (const sev of ['REQUIRED', 'WARNING', 'RECOMMENDED', 'INFO']) {
    const newFinding = [...allowedItems, {
      type: sev,
      value: `Synthetic new ${sev} finding at Line 99 in inc/synthetic.php.`
    }];
    runCheck(`new unreviewed ${sev} finding rejected`, writeReport(`new-${sev}.json`, newFinding), false);
  }

  // 6. Negative: Finding type mutation
  const mutatedType = structuredClone(allowedItems);
  mutatedType[0].type = mutatedType[0].type === 'REQUIRED' ? 'WARNING' : 'REQUIRED';
  runCheck('finding type mutation rejected', writeReport('type-mutation.json', mutatedType), false);

  // 7. Negative: Finding message mutation
  const mutatedMsg = structuredClone(allowedItems);
  mutatedMsg[0].value += ' with altered trailing parameter';
  runCheck('finding message mutation rejected', writeReport('message-mutation.json', mutatedMsg), false);

  // 8. Negative: Duplicate findings in report
  const duplicateReport = [...allowedItems, { ...allowedItems[0] }];
  runCheck('duplicate report items rejected', writeReport('duplicate.json', duplicateReport), false);

  // 9. Negative: Empty and missing reports
  const emptyFile = path.join(tempDir, 'empty.json');
  fs.writeFileSync(emptyFile, '');
  runCheck('empty report rejected', emptyFile, false);
  runCheck('missing report file rejected', path.join(tempDir, 'non-existent.json'), false);

  // 10. Negative: Malformed JSON or non-array
  const nonArrayFile = path.join(tempDir, 'non-array.json');
  fs.writeFileSync(nonArrayFile, JSON.stringify({ error: 'invalid' }));
  runCheck('non-array report rejected', nonArrayFile, false);

  // 11. Negative: Scanner and WP version drifts
  runCheck('scanner version drift rejected', approvedFile, false, ['--scanner-version', '20260902']);
  runCheck('wordpress version drift rejected', approvedFile, false, ['--wp-version', '6.5.0']);

  // 12. Negative: Tampered baseline file
  const tamperedBaseline = { ...baseline, findings: baseline.findings.slice(1) };
  const tamperedBaselinePath = path.join(tempDir, 'tampered-baseline.json');
  fs.writeFileSync(tamperedBaselinePath, JSON.stringify(tamperedBaseline, null, 2));
  runCheck('tampered baseline count rejected', approvedFile, false, ['--baseline', tamperedBaselinePath]);

  // 13. Fixture directory testing for ledger and pinned report tampering
  const fixtureRoot = path.join(tempDir, 'fixture');
  const fixtureEvidenceD = path.join(fixtureRoot, 'docs/evidence/P02-CI-CODE-010C3D');
  const fixtureEvidenceA1 = path.join(fixtureRoot, 'docs/evidence/P02-CI-CODE-010C3A1');
  const fixtureTasks = path.join(fixtureRoot, 'docs/tasks');

  fs.mkdirSync(fixtureEvidenceD, { recursive: true });
  fs.mkdirSync(fixtureEvidenceA1, { recursive: true });
  fs.mkdirSync(fixtureTasks, { recursive: true });

  const origDispositions = path.join(root, 'docs/evidence/P02-CI-CODE-010C3D/theme-check-dispositions.json');
  const fixtureDispositions = path.join(fixtureEvidenceD, 'theme-check-dispositions.json');
  fs.copyFileSync(origDispositions, fixtureDispositions);

  const origPinned = path.join(root, 'docs/evidence/P02-CI-CODE-010C3A1/theme-check.json');
  const fixturePinned = path.join(fixtureEvidenceA1, 'theme-check.json');
  fs.copyFileSync(origPinned, fixturePinned);

  // Copy task cards for fixTaskId verification
  for (const f of baseline.findings) {
    if (f.fixTaskId) {
      const srcTask = path.join(root, 'docs/tasks', f.fixTaskId + '.md');
      if (fs.existsSync(srcTask)) {
        fs.copyFileSync(srcTask, path.join(fixtureTasks, f.fixTaskId + '.md'));
      }
    }
  }

  const fixtureEnv = { CARMILLA_THEME_CHECK_FIXTURE_ROOT: fixtureRoot };
  runCheck('fixture run passes with authentic files', approvedFile, true, [], fixtureEnv);

  // Tamper dispositions in fixture
  const dispoBytes = fs.readFileSync(fixtureDispositions);
  fs.appendFileSync(fixtureDispositions, '\n ');
  runCheck('dispositions ledger hash drift rejected', approvedFile, false, [], fixtureEnv);
  fs.writeFileSync(fixtureDispositions, dispoBytes);

  // Tamper pinned report in fixture
  const pinnedBytes = fs.readFileSync(fixturePinned);
  fs.appendFileSync(fixturePinned, '\n ');
  runCheck('pinned report hash drift rejected', approvedFile, false, [], fixtureEnv);
  fs.writeFileSync(fixturePinned, pinnedBytes);

  console.log('\nAll Theme Check baseline scenarios passed successfully!');
} finally {
  fs.rmSync(tempDir, { recursive: true, force: true });
}
