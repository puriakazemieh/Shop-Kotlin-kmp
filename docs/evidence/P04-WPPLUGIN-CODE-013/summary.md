# Evidence Summary: P04-WPPLUGIN-CODE-013

- **Task ID:** `P04-WPPLUGIN-CODE-013`
- **Title:** زیرساخت مشترک export،erase و retention بدون وابستگی میزبان
- **Date:** 2026-11-28
- **Base Commit:** `68d50fde`
- **Status:** `DONE`

## Summary of Changes

1. **Host-Agnostic Privacy Exporter, Eraser & Retention Registry (`wordpress/packages/carmilla-core/inc/Privacy/PrivacyManager.php`)**:
   - Added `class_exists` guard to prevent redeclaration across co-install/container paths.
   - Implemented `get_retention_policy_registry()` declaring statutory financial retention (`anonymize_retain_ledger`, 5–10 years, referencing `P04-WPPLUGIN-CODE-015`) and approved clinical/psych retention (`anonymize_strip_phi` / `scrub_personal_identifiers`), plus explicit `allow_authorized_maintenance` when a feature is disabled.
   - Implemented `can_perform_privacy_action(int $acting_user_id, int $target_user_id)` enforcing ownership & privacy admin authorization (rejecting unauthenticated with `401` and non-owners with `403 FORBIDDEN`).
   - Added `reset_state()` for clean test isolation.
2. **Kernel Boot Wiring (`wordpress/packages/carmilla-core/Carmilla_Kernel.php`)**:
   - Required `PrivacyManager.php` and invoked `PrivacyManager::boot()` inside `init_core()` (guarded by `SchemaRunner::register_checkpoint('privacy_manager_boot')` so co-install never registers duplicate hooks).
   - Wired `PrivacyManager::reset_state()` inside `Carmilla_Kernel::reset_state()`.
3. **Real WordPress Test Suite (`wordpress/packages/carmilla-core/tests/test-privacy-retention-013.php`)**:
   - Verified co-install hook idempotency (exporter filter registered exactly once), retention policy registry, non-owner 403 rejection, export/erase continuity even when all domain features are toggled OFF, and host-agnostic parity across `Theme-Only`, `Plugin-Only` (TT4), and `Co-Install`.

## Verification Commands & Results

- **Environment:** Docker `test-env-wordpress-test-1` (WordPress 7.1.3, MariaDB 10.6.28, Apache)
- **Command:**
  `docker exec test-env-wordpress-test-1 sh -c "php -l ...PrivacyManager.php && php -l ...Carmilla_Kernel.php && php ...test-privacymanager-policy-content.php && php ...test-privacy-retention-013.php && php ...verify-all-phase04-real-wp.php"`
- **Results:**
  - PHP syntax lint (`PrivacyManager.php`, `Carmilla_Kernel.php`): **0 errors (exit code 0)**
  - `test-privacymanager-policy-content.php`: **14 / 14 passed (exit code 0)**
  - `test-privacy-retention-013.php`: **21 / 21 passed (exit code 0)**
  - `verify-all-phase04-real-wp.php`: **78 / 78 passed (exit code 0)**
