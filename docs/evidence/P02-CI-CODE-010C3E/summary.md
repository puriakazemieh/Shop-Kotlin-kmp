# Evidence Summary: P02-CI-CODE-010C3E

- **Task ID**: `P02-CI-CODE-010C3E`
- **Phase / Area / Type**: `P02 / CI / CODE+QA`
- **Date**: 2026-11-11
- **Status**: DONE

---

## 1. Scope & Objective

Implement an exact, pinned Theme Check finding baseline and regression gate derived directly from `P02-CI-CODE-010C3D`'s approved triage of the shipped `carmilla-theme` ZIP.
- Fail on any new REQUIRED, security, or unreviewed findings.
- Fail on reappearance of any of the 26 findings resolved by preceding fix cards (`P02-SEC-CODE-010C3D1`, `P02-THEME-CODE-010C3D2`, `P02-THEME-CODE-010C3D3`, `P02-CORE-CODE-010C3D4`).
- Fail on tool, scanner, or WordPress version drift (`scannerVersion !== 20260901`, `wpVersion !== 6.4.3`).
- Fail on missing, empty, or tampered reports or dispositions ledgers.
- Enforce strict exact-fingerprint matching without broad severity-count waivers.
- Preserve independent commercial theme architecture per `docs/INDEPENDENT_PRODUCTS_SPEC_FA.md` Section 12 (tracking plugin territory debt to `P04-WORDPRESS-CODE-051`) without falsely claiming WordPress.org directory compliance.

---

## 2. Artifacts & Components Created

1. **`tools/code-quality/theme-check-baseline.json`**:
   - Machine-readable canonical baseline of all 58 reviewed findings.
   - Pinned metadata:
     - `schema`: 1
     - `scanner`: `theme-check`
     - `scannerVersion`: `20260901`
     - `wpVersion`: `6.4.3`
     - `pinnedReportSha256`: `e5c56a2c74e5bde8e8e6b5f256fee5d5a408567eeb001d3de4958341060200b5`
     - `dispositionsSha256`: `410acf2c463fc58c827b273fc10d3e6e3b73626dae9d690b9bd57fe56845ac4c`
     - `totalReviewed`: 58
     - `mustDisappearCount`: 26
     - `allowedCount`: 32
   - Finding breakdown:
     - 26 findings with policy `must_disappear` (attributed to P02 fix cards).
     - 32 findings with policy `allow` (6 commercial domain architecture items tracked by `P04-WORDPRESS-CODE-051`, 5 INFO items, 21 RECOMMENDED items).

2. **`tools/code-quality/check_theme_check_baseline.cjs`**:
   - Exact CLI baseline enforcer.
   - Verifies scanner version and WordPress core version against pinned standards.
   - Verifies cryptographic hash of dispositions ledger (`theme-check-dispositions.json`) and pinned report (`theme-check.json`).
   - Verifies report structure, rejects duplicates, detects any new unreviewed finding, rejects regressions of previously fixed findings, and enforces exact message integrity.
   - Supports `--generate` for automated canonical baseline generation.

3. **`tools/code-quality/test_theme_check_baseline.cjs`**:
   - Automated unit test suite covering 23 positive, negative, regression, mutation, drift, and tampering scenarios.

4. **`.github/workflows/code-quality.yml`**:
   - Updated `theme-check` job:
     - Records and asserts pinned scanner version (`20260901`) and WordPress version (`6.4.3`).
     - Executes `check_theme_check_baseline.cjs` against generated `$RUNNER_TEMP/theme-check.json`.
     - Preserves report fingerprinting and artifact upload.

---

## 3. Verification & Results

### 3.1 Automated Scenario Test Suite
```bash
node tools/code-quality/test_theme_check_baseline.cjs
```
**Output**:
```
=== Running Theme Check Baseline Scenario Tests ===

PASS approved subset of 32 findings passes
PASS fewer findings (resolved allowed debt) passes
PASS original unremediated report rejected (26 must_disappear items)
PASS regression for P02-SEC-CODE-010C3D1 rejected
PASS regression for P02-THEME-CODE-010C3D2 rejected
PASS regression for P02-THEME-CODE-010C3D3 rejected
PASS regression for P02-CORE-CODE-010C3D4 rejected
PASS new unreviewed REQUIRED finding rejected
PASS new unreviewed WARNING finding rejected
PASS new unreviewed RECOMMENDED finding rejected
PASS new unreviewed INFO finding rejected
PASS finding type mutation rejected
PASS finding message mutation rejected
PASS duplicate report items rejected
PASS empty report rejected
PASS missing report file rejected
PASS non-array report rejected
PASS scanner version drift rejected
PASS wordpress version drift rejected
PASS tampered baseline count rejected
PASS fixture run passes with authentic files
PASS dispositions ledger hash drift rejected
PASS pinned report hash drift rejected

All Theme Check baseline scenarios passed successfully!
```
Exit code: `0`.

### 3.2 Regression Check of Plugin Check Baseline Suite
```bash
node tools/code-quality/test_plugin_check_baseline.cjs
```
**Output**:
All 19 scenarios passed. Exit code: `0`.

### 3.3 Git Diff & Quality Checks
```bash
git diff --check
```
Clean, zero trailing whitespace or formatting warnings.

---

## 4. Policy Alignment & Scope Boundaries

- **Commercial Architecture Boundary**: In accordance with `docs/INDEPENDENT_PRODUCTS_SPEC_FA.md` Section 12, the 6 REQUIRED Theme Check findings concerning CPT and shortcode declarations remain registered as approved commercial debt owned by `P04-WORDPRESS-CODE-051`. No false claim of WordPress.org directory acceptance is made.
- **Zero Product Source Changes**: Only CI workflow, quality tools, and documentation were touched.
