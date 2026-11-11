# Evidence Summary: P02-CI-CODE-010C3 — Plugin and Theme Check Bounded Baselines

- **Task ID**: `P02-CI-CODE-010C3`
- **Phase / Area / Type**: `P02 / CI / CODE`
- **Date**: 2026-11-11
- **Status**: DONE

---

## 1. Executive Summary

This closure report synthesizes and finalizes the parent epic `P02-CI-CODE-010C3`.
Originally an L-sized quality gate card facing 3,381 raw Plugin Check findings and 58 Theme Check findings, it was methodically decomposed into ordered, bounded subtasks (C3A through C3E plus genuine fix tasks `010C3D1` through `010C3D4`).

Both official WordPress scanner suites (Plugin Check and Theme Check) now operate under **exact cryptographic baselines** in GitHub Actions CI (`.github/workflows/code-quality.yml`).
- No broad severity-wide or code-wide waiver was granted.
- Zero untriaged security, escaping, or database findings were suppressed.
- All genuine defects identified during scanner triage were fixed via dedicated production code cards and verified with regression test suites.
- Scanner versions, action refs, and WordPress core versions are strictly pinned and cryptographically checked on every CI run.

---

## 2. Complete Subtask Trace & Resolution Matrix

| Subtask ID | Type / Focus | Resolution / Deliverable | Status |
|---|---|---|---|
| `P02-CI-CODE-010C3A` | CI / CAPTURE | Pinned initial Plugin Check & Theme Check raw reports | DONE |
| `P02-CI-CODE-010C3A1` | CI / PACKAGE | Unified local & workflow ZIP builders (`build-theme-zip.sh`, `build-bridge-zip.sh`); excluded test directories | DONE (`4db12379`) |
| `P02-CI-CODE-010C3B1` | CI / TRIAGE | Security triage of Plugin Check findings (C3B1A1–D) | DONE |
| `P02-CI-CODE-010C3B2` | CI / TRIAGE | Database & file operations triage of Plugin Check findings (C3B2A1–G2) | DONE |
| `P02-CI-CODE-010C3B3` | CI / TRIAGE | Remaining Plugin Check findings triage (C3B3A1–D) | DONE |
| `P02-CI-CODE-010C3C` | CI / ENFORCE | Plugin Check exact baseline enforcer (`check_plugin_check_baseline.cjs`) & reconciliation (C3C1–C4D) | DONE |
| `P02-CI-CODE-010C3D` | CI / TRIAGE | Exhaustive triage of 58 Theme Check findings; generated `theme-check-dispositions.json` | DONE (`d458531a`) |
| `P02-SEC-CODE-010C3D1` | CODE / FIX | Replaced `base64_encode`/`base64_decode` with hex serialization in `MessageSettings.php` | DONE (`cbf35268`) |
| `P02-THEME-CODE-010C3D2` | THEME / FIX | Implemented `wp_link_pages()` across all 5 single/page templates & `get_search_form()` in `footer.php` | DONE (`e7066de3`) |
| `P02-THEME-CODE-010C3D3` | THEME / FIX | Added `screenshot.png` (1200x900), `readme.txt`, and completed `style.css` header metadata | DONE (`d61e8473`) |
| `P02-CORE-CODE-010C3D4` | CORE / FIX | Refactored `PrivacyManager.php` policy string formatting, eliminating 12 parser warnings & fake text-domain | DONE (`95c519d4`) |
| `P02-CI-CODE-010C3E` | CI / ENFORCE | Implemented exact Theme Check baseline enforcer (`check_theme_check_baseline.cjs`) & CI gate | DONE (`51eca035`) |

---

## 3. Cryptographic Baseline & Tool Pinning Standards

### 3.1 WordPress Plugin Check
- **Scanner Version**: `2.1.0`
- **WordPress Core**: `6.4.3`
- **Action Reference**: `10857da14b6c2246d15402b3e69f777edcf8c12e`
- **Pinned Raw Report SHA-256**: `50f0c694447c4b9aab40b1d66c4091cecdad0614ba18c8f70208c3f2b36fd0bd`
- **Reconciled Report SHA-256**: `3c45375b51d1c525be3accd509ddc5f6d11e7c37fc27c5f6974cf2465c2fecbb`
- **Current Ledger SHA-256**: `35275460c102242c4a47c58247d4c3af44c775ab442f24b588f880cf914937cc`
- **Enforcer Tool**: `tools/code-quality/check_plugin_check_baseline.cjs`
- **Automated Verification**: `tools/code-quality/test_plugin_check_baseline.cjs` (19 scenarios: positive, negative, tamper, drift)

### 3.2 WordPress Theme Check
- **Scanner Version**: `20260901`
- **WordPress Core**: `6.4.3`
- **Pinned Raw Report SHA-256**: `e5c56a2c74e5bde8e8e6b5f256fee5d5a408567eeb001d3de4958341060200b5`
- **Dispositions Ledger SHA-256**: `410acf2c463fc58c827b273fc10d3e6e3b73626dae9d690b9bd57fe56845ac4c`
- **Baseline File**: `tools/code-quality/theme-check-baseline.json` (58 reviewed: 26 `must_disappear`, 32 `allow`)
- **Enforcer Tool**: `tools/code-quality/check_theme_check_baseline.cjs`
- **Automated Verification**: `tools/code-quality/test_theme_check_baseline.cjs` (23 scenarios: positive, negative, tamper, drift)

---

## 4. Verification & Gate Evidence

### 4.1 CI Workflow Integration
In `.github/workflows/code-quality.yml`:
- Both `theme-check` and `wp-plugin-check` jobs:
  1. Build clean installable ZIPs via unified shell scripts.
  2. Install disposable WordPress 6.4.3 and exact pinned scanners.
  3. Validate versions against strict strings (`test "$scanner_version" = ...`).
  4. Execute scans with JSON/text capture.
  5. Enforce exact baseline via node enforcers (`exit 1` on new/drifted/regressed items).
  6. Fingerprint and upload reports as CI artifacts.

### 4.2 Comprehensive Local Test Suite Execution
```bash
# Baseline check scenarios
node tools/code-quality/test_plugin_check_baseline.cjs   # 19 scenarios passed
node tools/code-quality/test_theme_check_baseline.cjs    # 23 scenarios passed

# Package builds
bash wordpress/build-theme-zip.sh /tmp/carmilla-theme.zip
bash wordpress/build-bridge-zip.sh /tmp/carmilla-bridge.zip

# Kernel & Theme regression test suites
php packages/carmilla-core/tests/smoke-carmilla-core.php                 # 39 passed
php packages/carmilla-core/tests/test-message-settings-encryption.php   # 11 passed
php packages/carmilla-core/tests/test-privacymanager-policy-content.php  # 14 passed
php carmilla-theme/tests/smoke-manifest-theme.php                       # 5 passed
php carmilla-theme/tests/test-template-pagination-searchform.php        # 13 passed
php carmilla-theme/tests/test-theme-metadata-assets.php                 # 33 passed
```
Total: **157 test assertions passing with exit code 0**.

---

## 5. Architecture Alignment & Compliance

Per `docs/INDEPENDENT_PRODUCTS_SPEC_FA.md` Sections 1, 6, and 12:
- **Commercial Theme Autonomy**: The 6 REQUIRED findings concerning CPT and shortcode definitions in the independent theme are preserved as intentional commercial capabilities and tracked as approved commercial debt owned by `P04-WORDPRESS-CODE-051`.
- **WordPress.org Channel Clarity**: No false claim of WordPress.org directory compliance is made. The exact baseline enforces that no unexpected findings or security defects are introduced while honoring the commercial architecture.

---

## 6. Conclusion & Next Step

Task `P02-CI-CODE-010C3` is fully accomplished with complete, reproducible evidence.
The next task in sequence is **`P02-CI-CODE-010C4`** (Quality gate positive and negative PR proof).
