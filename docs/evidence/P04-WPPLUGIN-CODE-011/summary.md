# Evidence Summary: P04-WPPLUGIN-CODE-011

- **Task ID:** `P04-WPPLUGIN-CODE-011`
- **Title:** راه‌اندازی و preflight مستقل هر محصول طبق SKU
- **Date:** 2026-11-28
- **Base Commit:** `706ac289`
- **Status:** `DONE`

## Summary of Changes

1. **SKU-Aware Preflight & Secret Redaction (`wordpress/packages/carmilla-core/inc/Health/HealthChecker.php`)**:
   - Added `run_preflight(string $host_context = 'auto', array $purchased_skus = [], array $overrides = [])` separating shared site checks (`https`, `permalinks`, `rest_api`, `wp_cron`, `kernel_version`) from SKU-scoped checks (`woocommerce` scoped to `sku:sku_commerce`).
   - Base SKUs (`sku_theme_base`, `sku_plugin_base`) pass onboarding readiness even when WooCommerce is absent (`status: info`), whereas `sku_commerce` blocks with clear remediation when WooCommerce is missing.
   - Added `redact_payload()` to recursively redact secrets, tokens, passwords, salts, and PII (`[REDACTED]`), keeping diagnostic metadata (`secrets_redacted: true`) intact.
   - Added REST endpoints `GET /carmilla/v1/preflight` and `POST /carmilla/v1/preflight/recheck` protected via `CapabilityRegistry::get_permission_callback('admin', 'read', 'theme.base')`.
2. **REST Route Wiring (`wordpress/packages/carmilla-core/inc/Rest/RestInfrastructure.php`)**:
   - Hooked `HealthChecker::register_rest_routes()` into `RestInfrastructure::on_rest_api_init()`.
3. **Independent Host Onboarding Entrypoints**:
   - **Theme Host (`wordpress/carmilla-theme/inc/class-theme-host-adapter.php`):** Added `ThemeHostAdapter::get_onboarding_preflight()`.
   - **Plugin Host (`wordpress/carmilla-bridge/includes/class-cb-standalone-boot.php`):** Added `CB_Standalone_Boot::get_onboarding_preflight()`.
4. **Shared Admin Preflight & Diagnostics Panel (`wordpress/packages/carmilla-core/inc/Settings/AdminSettingsPage.php`)**:
   - Rendered host-aware preflight status, per-check scope/remediation table, "Re-run Preflight" action, and redacted diagnostics JSON preview.
5. **Real WordPress Test Suite (`wordpress/packages/carmilla-core/tests/test-preflight-onboarding-011.php`)**:
   - Verified Base vs Commerce SKU WooCommerce behavior, REST/cron/permalink/SSL/kernel failure simulations with remediations, secret/PII redaction, REST authorization (401/403 for anon/subscriber, 200 for admin), and independent onboarding across `Theme-Only`, `Plugin-Only` (TT4), and `Co-Install`.

## Verification Commands & Results

- **Environment:** Docker `test-env-wordpress-test-1` (WordPress 7.1.3, MariaDB 10.6.28, Apache)
- **Command:**
  `docker exec test-env-wordpress-test-1 sh -c "php -l /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/inc/Health/HealthChecker.php && php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/test-preflight-onboarding-011.php && php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/verify-all-phase04-real-wp.php"`
- **Results:**
  - PHP syntax lint (`HealthChecker.php`, `RestInfrastructure.php`, `AdminSettingsPage.php`, `class-theme-host-adapter.php`, `class-cb-standalone-boot.php`): **0 errors (exit code 0)**
  - `test-preflight-onboarding-011.php`: **40 / 40 passed (0 failed, exit code 0)**
  - `verify-all-phase04-real-wp.php`: **78 / 78 passed (exit code 0)**
