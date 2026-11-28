# Evidence Summary: P04-WPPLUGIN-CODE-014

- **Task ID:** `P04-WPPLUGIN-CODE-014`
- **Title:** API تنظیمات مشترک سایت با کنترل دسترسی و audit
- **Date:** 2026-11-28
- **Base Commit:** `5976c483`
- **Status:** `DONE`

## Summary of Changes

1. **Strict Operational Settings Service (`wordpress/packages/carmilla-core/inc/Settings/SettingsService.php`)**:
   - Defined canonical operational allowlist schema (`OPERATIONAL_SCHEMA`: `sms_provider`, `payment_gateway`, `enable_debug_log`, `maintenance_mode`, `max_upload_size_mb`) stored in `carmilla_settings`.
   - Enforced authorization (`UNAUTHENTICATED` 401, `FORBIDDEN` 403), CSRF/Nonce validation (`CSRF_INVALID` 403), entitlement forgery rejection (`ENTITLEMENT_WRITE_FORBIDDEN` 403), theme presentation separation (`PRESENTATION_KEY_SEPARATED` 400), unknown key rejection (`UNKNOWN_OPTION` 400), and type/bounds validation (`INVALID_OPTION_VALUE` 400).
   - Redacted audit logging via `HealthChecker::redact_payload()` storing `user_id`, `host_mode`, `old`, `new`, and `timestamp` in `carmilla_settings_audit_log`.
   - Exposed REST API routes: `GET /carmilla/v1/settings`, `POST/PUT/PATCH /carmilla/v1/settings`, and `GET /carmilla/v1/settings/audit`.
2. **Kernel & Host Wiring**:
   - `Carmilla_Kernel.php`: Included `SettingsService.php`.
   - `RestInfrastructure.php`: Registered `SettingsService::register_rest_routes()` in `on_rest_api_init()`.
   - `ThemeHostAdapter.php` & `CB_Standalone_Boot.php`: Added `save_operational_settings()` delegating to the canonical `SettingsService`.
3. **Real WordPress Test Suite (`wordpress/packages/carmilla-core/tests/test-settings-api-014.php`)**:
   - Verified auth guards (401/403), missing CSRF nonce rejection (403), entitlement forgery rejection (403), presentation key separation (400), unknown option (400), out-of-bounds value (400), canonical persistence across `Theme-Only`, `Plugin-Only` (TT4), and `Co-Install`, and redacted audit logging.

## Verification Commands & Results

- **Environment:** Docker `test-env-wordpress-test-1` (WordPress 7.1.3, MariaDB 10.6.28, Apache)
- **Command:**
  `docker exec test-env-wordpress-test-1 sh -c "php -l ...SettingsService.php && php ...test-settings-api-014.php && php ...verify-all-phase04-real-wp.php"`
- **Results:**
  - PHP syntax lint (5 modified files): **0 errors (exit code 0)**
  - `test-settings-api-014.php`: **21 / 21 passed (exit code 0)**
  - `verify-all-phase04-real-wp.php`: **78 / 78 passed (exit code 0)**
