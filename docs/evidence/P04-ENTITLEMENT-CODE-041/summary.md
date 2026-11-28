# Evidence Summary: P04-ENTITLEMENT-CODE-041

- **Task ID:** `P04-ENTITLEMENT-CODE-041`
- **Title:** پنل مشترک قابلیت‌های خریداری‌شده در هر دو میزبان
- **Date:** 2026-11-28
- **Base Commit:** `75ed523d`
- **Status:** `DONE`

## Summary of Changes

1. **Shared Entitlement Toggle & Builder Target Service (`wordpress/packages/carmilla-core/inc/Entitlement/FeatureToggleService.php`)**:
   - Implemented `get_panel_state()` surfacing per-feature badges (`ACTIVE`, `OFF`, `NOT_ENTITLED`, `NOT_PACKAGED` with upgrade prompt, `MISSING_PREREQUISITE`, `RELEASE_GATE_RESTRICTED`) and independent state for all 4 builder targets (`android`, `ios`, `pwa`, `desktop`).
   - Implemented `save_toggles()` enforcing admin capability (`403 FORBIDDEN`), CSRF nonce (`403 CSRF_INVALID`), packaging & entitlement gates (`403 FEATURE_NOT_PACKAGED` / `403 FEATURE_NOT_ENTITLED` / `403 BUILDER_NOT_ENTITLED`), dependency closure validation via `CapabilityResolver::get_dependency_violations()` (`400 INVALID_DEPENDENCIES`), and non-destructive data retention when disabling features.
   - Exposed `GET/POST /carmilla/v1/entitlements/panel` and `admin_post_carmilla_save_feature_toggles`.
2. **Shared Admin Toggle Panel View (`wordpress/packages/carmilla-core/inc/Settings/AdminSettingsPage.php`)**:
   - Added Purchased Features table with status badges, disabled toggles for non-entitled/non-packaged items, upgrade notice for `NOT_PACKAGED`, and independent Builder Targets checkboxes.
3. **Kernel, REST & Host Adapter Wiring**:
   - `Carmilla_Kernel.php`, `RestInfrastructure.php`, `ThemeHostAdapter.php`, `CB_Standalone_Boot.php`.
4. **Real WordPress Test Suite (`wordpress/packages/carmilla-core/tests/test-entitlement-panel-041.php`)**:
   - Verified all 5 panel badges, non-admin/CSRF rejections, non-packaged/non-entitled rejections, invalid dependency rejection, independent builder targets (`ios` off without affecting `android`/`pwa`), data retention on feature disable, and identical save/read parity across `Theme-Only` and `Plugin-Only` (TT4).

## Verification Commands & Results

- **Environment:** Docker `test-env-wordpress-test-1` (WordPress 7.1.3, MariaDB 10.6.28, Apache)
- **Command:**
  `docker exec test-env-wordpress-test-1 sh -c "php -l ...FeatureToggleService.php && php ...test-entitlement-panel-041.php && php ...verify-all-phase04-real-wp.php"`
- **Results:**
  - PHP syntax lint (6 modified files): **0 errors (exit code 0)**
  - `test-entitlement-panel-041.php`: **24 / 24 passed (exit code 0)**
  - `verify-all-phase04-real-wp.php`: **78 / 78 passed (exit code 0)**
