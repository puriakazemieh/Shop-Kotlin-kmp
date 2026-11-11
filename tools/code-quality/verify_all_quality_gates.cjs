#!/usr/bin/env node
/**
 * Unified verification runner for all Code Quality gates (Theme Check, Plugin Check, WPCS, Builders, Actionlint).
 * Validates positive baselines and negative synthetic rejections across all quality subsystems.
 */

const { execSync } = require('child_process');
const path = require('path');
const fs = require('fs');

const repoRoot = path.resolve(__dirname, '../..');

function run(cmd, desc) {
  process.stdout.write(`[RUN] ${desc}... `);
  try {
    const out = execSync(cmd, { cwd: repoRoot, encoding: 'utf-8', stdio: ['pipe', 'pipe', 'pipe'] });
    console.log('✅ PASS');
    return { success: true, output: out };
  } catch (err) {
    console.log('❌ FAIL');
    console.error(err.stdout || err.stderr || err.message);
    return { success: false, error: err };
  }
}

console.log('=====================================================');
console.log('Starting Unified Quality Gate Verification (P02-CI-CODE-010C4)');
console.log('=====================================================\n');

let allPassed = true;

// 1. Theme Check exact baseline and negative rejection scenarios
const tcResult = run(
  `node "${path.join(__dirname, 'test_theme_check_baseline.cjs')}"`,
  'Theme Check baseline scenario tests (23 scenarios)'
);
if (!tcResult.success) allPassed = false;

// 2. Plugin Check exact baseline and negative rejection scenarios
const pcResult = run(
  `node "${path.join(__dirname, 'test_plugin_check_baseline.cjs')}"`,
  'Plugin Check baseline scenario tests (19 scenarios)'
);
if (!pcResult.success) allPassed = false;

// 3. Workflow syntax check via actionlint
const alResult = run(
  'docker run --rm -v "' + repoRoot + ':/repo" rhysd/actionlint:1.7.7 -color /repo/.github/workflows/code-quality.yml',
  'Workflow syntax verification (rhysd/actionlint:1.7.7)'
);
if (!alResult.success) allPassed = false;

// 4. Generate current WPCS report and run baseline checks
const artifactsDir = path.join(repoRoot, '.artifacts');
if (!fs.existsSync(artifactsDir)) fs.mkdirSync(artifactsDir, { recursive: true });
const wpcsReport = path.join(artifactsDir, 'current-wpcs-report.json');

const wpcsGenResult = run(
  'docker run --rm -v "' + repoRoot + ':/repo" -w /repo composer:2 sh -c "mkdir -p /tmp/wpcs && cd /tmp/wpcs && composer init --no-interaction --name=qa/wpcs >/dev/null 2>&1 && composer config allow-plugins.dealerdirect/phpcodesniffer-composer-installer true >/dev/null 2>&1 && composer require --no-interaction --dev wp-coding-standards/wpcs:3.4.1 squizlabs/php_codesniffer:3.13.6 >/dev/null 2>&1 && /tmp/wpcs/vendor/bin/phpcs --standard=WordPress --ignore=\'*/vendor/*,*/node_modules/*,*/tests/*\' --report=json --report-file=/repo/.artifacts/current-wpcs-report.json /repo/wordpress/carmilla-bridge || true"',
  'Generate current Bridge WPCS scan report'
);
if (!wpcsGenResult.success) allPassed = false;

if (fs.existsSync(wpcsReport)) {
  const wpcsCheckResult = run(
    'docker run --rm -v "' + repoRoot + ':/repo" -w /repo python:3.12-alpine python3 tools/code-quality/check_wpcs_baseline.py --report .artifacts/current-wpcs-report.json --baseline tools/code-quality/wpcs-style-baseline.json --security-ledger tools/code-quality/wpcs-security-dispositions.json --mode phase',
    'WPCS baseline gate check (phase mode: zero new style, zero unreviewed security)'
  );
  if (!wpcsCheckResult.success) allPassed = false;

  const wpcsProbeResult = run(
    'docker run --rm -v "' + repoRoot + ':/repo" -w /repo python:3.12-alpine python3 tools/code-quality/probe_wpcs_baseline.py .artifacts/current-wpcs-report.json tools/code-quality/wpcs-style-baseline.json tools/code-quality/wpcs-security-dispositions.json',
    'WPCS baseline probe & synthetic negative proof (6 scenarios)'
  );
  if (!wpcsProbeResult.success) allPassed = false;

  // Cleanup temporary report
  try { fs.unlinkSync(wpcsReport); } catch (_) {}
}

// 5. Theme and Bridge zip builder tests
const builderResult = run(
  'docker run --rm -v "' + repoRoot + ':/repo" -w /repo composer:2 sh -c "bash /repo/wordpress/build-theme-zip.sh /tmp/theme.zip && bash /repo/wordpress/build-bridge-zip.sh /tmp/bridge.zip && test -f /tmp/theme.zip && test -f /tmp/bridge.zip"',
  'Theme and Bridge zip builder validation (composer:2)'
);
if (!builderResult.success) allPassed = false;

console.log('\n=====================================================');
if (allPassed) {
  console.log('✅ ALL QUALITY GATES VERIFIED SUCCESSFULLY (PASS)');
  console.log('=====================================================');
  process.exit(0);
} else {
  console.error('❌ ONE OR MORE GATES FAILED');
  console.log('=====================================================');
  process.exit(1);
}
