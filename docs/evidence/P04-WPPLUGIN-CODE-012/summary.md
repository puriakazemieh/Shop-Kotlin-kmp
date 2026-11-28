# Evidence Summary: P04-WPPLUGIN-CODE-012

- **Task ID:** `P04-WPPLUGIN-CODE-012`
- **Title:** چرخه فعال‌سازی دو میزبان و پاک‌سازی صریح داده سایت
- **Date:** 2026-11-28
- **Base Commit:** `dcf6b6f5`
- **Status:** `DONE`

## Summary of Changes

1. **Dual-Host Lifecycle & Data Retention Policy (`wordpress/packages/carmilla-core/inc/Lifecycle/LifecycleManager.php`)**:
   - Enforced default non-destructive data retention (`carmilla_delete_data_on_uninstall` defaults to `false`).
   - Updated `handle_deactivation()` and `handle_theme_switch()` to distinguish between single-host continuation (`active_single_host` when a compatible Carmilla host remains) and last-host shutdown (`halted_no_active_host` when both Theme and Bridge are inactive), while preserving all site posts, orders, and settings.
   - Implemented `execute_explicit_cleanup(int $user_id, string $nonce, bool $explicit_opt_in)` enforcing 3 independent gates: Admin `delete` capability (`FORBIDDEN` 403), valid WordPress nonce (`INVALID_NONCE` 403), and explicit opt-in confirmation (`OPT_IN_REQUIRED` 400).
   - Added `register_admin_cleanup_handler()` (`admin_post_carmilla_explicit_cleanup`) and `get_host_lifecycle_status()` referencing `P04-WORDPRESS-DATA-042` for cross-host adoption and license transfer.
2. **Kernel & Host Wiring**:
   - `Carmilla_Kernel.php`: Included `LifecycleManager.php` and registered `register_theme_switch_handler()` and `register_admin_cleanup_handler()`.
   - `wordpress/carmilla-bridge/carmilla-bridge.php`: Wired activation, deactivation, and uninstall hooks to `LifecycleManager`.
   - `wordpress/carmilla-theme/functions.php`: Wired `after_switch_theme` to `LifecycleManager::handle_activation()`.
3. **Admin Settings Lifecycle & Cleanup UI (`wordpress/packages/carmilla-core/inc/Settings/AdminSettingsPage.php`)**:
   - Added lifecycle status display, active hosts list, `P04-WORDPRESS-DATA-042` adoption reference, and explicit nonce+opt-in cleanup form.
4. **Real WordPress Test Suite (`wordpress/packages/carmilla-core/tests/test-lifecycle-retention-012.php`)**:
   - Verified activation retention defaults, single-host continuation on plugin deactivation and theme switch, explicit `halted_no_active_host` on last-host shutdown with full data preservation, uninstall without opt-in preservation, and negative/positive cleanup security fixtures.

## Verification Commands & Results

- **Environment:** Docker `test-env-wordpress-test-1` (WordPress 7.1.3, MariaDB 10.6.28, Apache)
- **Command:**
  `docker exec test-env-wordpress-test-1 sh -c "php -l /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/inc/Lifecycle/LifecycleManager.php && php ...test-lifecycle-retention-012.php && php ...verify-all-phase04-real-wp.php"`
- **Results:**
  - PHP syntax lint (`LifecycleManager.php`, `Carmilla_Kernel.php`, `AdminSettingsPage.php`, `functions.php`, `carmilla-bridge.php`): **0 errors (exit code 0)**
  - `test-lifecycle-retention-012.php`: **30 / 30 passed (0 failed, exit code 0)**
  - `verify-all-phase04-real-wp.php`: **78 / 78 passed (exit code 0)**
