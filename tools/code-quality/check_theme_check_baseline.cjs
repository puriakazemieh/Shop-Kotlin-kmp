#!/usr/bin/env node
/**
 * Exact shipped-theme Theme Check baseline enforcer.
 * Enforces pinned scanner versions, dispositions ledger verification, and regression prevention.
 * No finding or severity is globally waived.
 * Task: P02-CI-CODE-010C3E
 */
const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');

const root = process.env.CARMILLA_THEME_CHECK_FIXTURE_ROOT
  ? path.resolve(process.env.CARMILLA_THEME_CHECK_FIXTURE_ROOT)
  : path.resolve(__dirname, '../..');

const baselinePath = path.join(__dirname, 'theme-check-baseline.json');
const pinnedReportPath = path.join(root, 'docs/evidence/P02-CI-CODE-010C3A1/theme-check.json');
const pinnedReportSha256 = 'e5c56a2c74e5bde8e8e6b5f256fee5d5a408567eeb001d3de4958341060200b5';

const dispositionsPath = path.join(root, 'docs/evidence/P02-CI-CODE-010C3D/theme-check-dispositions.json');
const dispositionsSha256 = '410acf2c463fc58c827b273fc10d3e6e3b73626dae9d690b9bd57fe56845ac4c';

const scannerVersion = '20260901';
const wpVersion = '6.4.3';

const sha = data => crypto.createHash('sha256').update(data).digest('hex');
const fail = message => { throw new Error(message); };
const key = x => `${x.type}\u0000${x.value || x.fullMessage}`;

function parseReport(data) {
  let items;
  try {
    items = JSON.parse(data.toString('utf8'));
  } catch (err) {
    fail('Malformed Theme Check report JSON: ' + err.message);
  }
  if (!Array.isArray(items)) {
    fail('Theme Check report is not a JSON array');
  }

  const validTypes = new Set(['REQUIRED', 'WARNING', 'RECOMMENDED', 'INFO']);
  const rows = [];
  const seen = new Set();

  for (const item of items) {
    if (!item || typeof item.type !== 'string' || !validTypes.has(item.type) || typeof item.value !== 'string') {
      fail('Malformed Theme Check finding entry: ' + JSON.stringify(item));
    }
    const row = { type: item.type, value: item.value.trim() };
    const k = key(row);
    if (seen.has(k)) {
      fail(`Duplicate Theme Check finding in report: ${k}`);
    }
    seen.add(k);
    rows.push(row);
  }

  return rows;
}

function loadDispositions() {
  if (!fs.existsSync(dispositionsPath)) {
    fail(`Missing dispositions ledger: ${dispositionsPath}`);
  }
  const bytes = fs.readFileSync(dispositionsPath);
  const fileHash = sha(bytes);
  if (fileHash !== dispositionsSha256) {
    fail(`Dispositions ledger hash drift: expected ${dispositionsSha256}, got ${fileHash}`);
  }

  const ledger = JSON.parse(bytes.toString('utf8'));
  if (ledger.taskId !== 'P02-CI-CODE-010C3D' ||
      ledger.scannerVersion !== scannerVersion ||
      ledger.wpVersion !== wpVersion ||
      ledger.pinnedReportSha256 !== pinnedReportSha256 ||
      !Array.isArray(ledger.findings) ||
      ledger.findings.length !== 58) {
    fail('Malformed dispositions ledger content');
  }

  const rows = [];
  const seen = new Set();

  for (const f of ledger.findings) {
    if (!f || typeof f.type !== 'string' || typeof f.fullMessage !== 'string' || !f.disposition) {
      fail(`Incomplete finding in dispositions ledger index ${f ? f.index : 'unknown'}`);
    }
    if (f.fixTaskId && !fs.existsSync(path.join(root, 'docs/tasks', f.fixTaskId + '.md'))) {
      fail(`Missing fix task card: ${f.fixTaskId}`);
    }

    const policy = (f.fixTaskId && f.fixTaskId.startsWith('P02-')) ? 'must_disappear' : 'allow';
    const row = {
      index: f.index,
      fingerprint: f.fingerprint,
      type: f.type,
      file: f.file,
      line: f.line,
      category: f.category,
      channel: f.channel,
      disposition: f.disposition,
      fixTaskId: f.fixTaskId || null,
      fullMessage: f.fullMessage.trim(),
      policy
    };

    const k = key(row);
    if (seen.has(k)) {
      fail(`Duplicate finding in dispositions ledger: ${k}`);
    }
    seen.add(k);
    rows.push(row);
  }

  if (rows.length !== 58) {
    fail(`Expected 58 reviewed findings, found ${rows.length}`);
  }

  return { rows, fileHash };
}

function comparePinned(ledgerRows) {
  if (!fs.existsSync(pinnedReportPath)) {
    fail(`Missing pinned Theme Check report: ${pinnedReportPath}`);
  }
  const raw = fs.readFileSync(pinnedReportPath);
  const fileHash = sha(raw);
  if (fileHash !== pinnedReportSha256) {
    fail(`Pinned report hash changed: expected ${pinnedReportSha256}, got ${fileHash}`);
  }

  const pinned = parseReport(raw);
  if (pinned.length !== 58) {
    fail(`Pinned report count changed: expected 58, got ${pinned.length}`);
  }

  const pinnedMap = new Map(pinned.map(x => [key(x), x]));
  for (const row of ledgerRows) {
    const k = key(row);
    if (!pinnedMap.has(k)) {
      fail(`Ledger finding not found in pinned report: ${k}`);
    }
  }

  return pinnedMap;
}

function parseArgs(argv) {
  const out = {};
  for (let i = 0; i < argv.length; i++) {
    const a = argv[i];
    if (a === '--generate') {
      out.generate = true;
      continue;
    }
    if (!['--report', '--scanner-version', '--wp-version', '--baseline'].includes(a) || !argv[i + 1]) {
      fail(`Unknown or missing argument: ${a}`);
    }
    out[a.slice(2)] = argv[++i];
  }
  return out;
}

function main() {
  const opts = parseArgs(process.argv.slice(2));
  const { rows, fileHash } = loadDispositions();
  const pinnedMap = comparePinned(rows);

  if (opts.generate) {
    if (Object.keys(opts).length !== 1) {
      fail('Generate takes no other arguments');
    }
    const baseline = {
      schema: 1,
      scanner: 'theme-check',
      scannerVersion,
      wpVersion,
      pinnedReportSha256,
      dispositionsSha256: fileHash,
      totalReviewed: rows.length,
      mustDisappearCount: rows.filter(x => x.policy === 'must_disappear').length,
      allowedCount: rows.filter(x => x.policy === 'allow').length,
      findings: rows.map(r => ({
        index: r.index,
        fingerprint: r.fingerprint,
        type: r.type,
        file: r.file,
        line: r.line,
        category: r.category,
        channel: r.channel,
        disposition: r.disposition,
        fixTaskId: r.fixTaskId,
        policy: r.policy,
        value: r.fullMessage
      }))
    };

    fs.writeFileSync(baselinePath, JSON.stringify(baseline, null, 2) + '\n', 'utf8');
    console.log(`Generated exact Theme Check baseline: ${rows.length} reviewed, ${baseline.mustDisappearCount} must disappear, ${baseline.allowedCount} allowed`);
    return;
  }

  if (!opts.report || !opts['scanner-version'] || !opts['wp-version']) {
    fail('Report and pinned scanner/WordPress versions are required');
  }

  const effectiveBaselinePath = opts.baseline || baselinePath;
  if (!fs.existsSync(effectiveBaselinePath)) {
    fail(`Baseline file missing: ${effectiveBaselinePath}`);
  }

  const baselineBytes = fs.readFileSync(effectiveBaselinePath);
  let baseline;
  try {
    baseline = JSON.parse(baselineBytes.toString('utf8'));
  } catch (err) {
    fail('Malformed baseline JSON: ' + err.message);
  }

  if (baseline.schema !== 1 ||
      baseline.scanner !== 'theme-check' ||
      baseline.scannerVersion !== scannerVersion ||
      baseline.wpVersion !== wpVersion ||
      baseline.pinnedReportSha256 !== pinnedReportSha256 ||
      baseline.dispositionsSha256 !== fileHash ||
      !Array.isArray(baseline.findings) ||
      baseline.findings.length !== 58) {
    fail('Malformed or drifted Theme Check baseline');
  }

  if (opts['scanner-version'] !== scannerVersion || opts['wp-version'] !== wpVersion) {
    fail(`Scanner or WordPress version drift: got scanner=${opts['scanner-version']}, wp=${opts['wp-version']}; expected scanner=${scannerVersion}, wp=${wpVersion}`);
  }

  if (!fs.existsSync(opts.report)) {
    fail(`Report file not found: ${opts.report}`);
  }

  const reportData = fs.readFileSync(opts.report);
  if (!reportData.length) {
    fail('Empty Theme Check report');
  }

  const actualFindings = parseReport(reportData);
  const baselineMap = new Map(baseline.findings.map(x => [key(x), x]));

  const problems = [];
  const actualKeys = new Set();

  for (const row of actualFindings) {
    const k = key(row);
    actualKeys.add(k);
    const known = baselineMap.get(k);

    if (!known) {
      problems.push(`New unreviewed finding: [${row.type}] ${row.value}`);
    } else if (known.policy === 'must_disappear') {
      problems.push(`Previously fixed finding remains (fix task ${known.fixTaskId}): [${row.type}] ${row.value}`);
    }
  }

  if (problems.length > 0) {
    fail(problems.slice(0, 20).join('\n') + (problems.length > 20 ? `\n... ${problems.length - 20} more` : ''));
  }

  const resolvedCount = baseline.findings.filter(x => x.policy === 'must_disappear' && !actualKeys.has(key(x))).length;
  console.log(`PASS Theme Check exact baseline: actual=${actualFindings.length} reviewed=${baseline.totalReviewed} resolved=${resolvedCount} allowed=${baseline.allowedCount}`);
}

try {
  main();
} catch (error) {
  console.error(`FAIL Theme Check baseline: ${error.message}`);
  process.exitCode = 1;
}
