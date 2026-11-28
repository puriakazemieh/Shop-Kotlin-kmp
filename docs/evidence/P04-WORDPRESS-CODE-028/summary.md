# Evidence — P04-WORDPRESS-CODE-028

## Summary
Migrated Therapist (`cb_therapist`) catalog and timezone-aware availability slots (`cb_slots`, `cb_timezone`, synthetic slot IDs `therapistId * 100000 + index`) into `Carmilla\Core\Clinic\TherapistCatalogService` and `TherapistCatalogRestController`, strictly separated from PsychTest (`psych.tests`) and private clinical files (`P14`), and wired `[carmilla_therapists]` / `[carmilla_therapist]` and theme templates across both Theme and Plugin.

## Changed Files
- `wordpress/packages/carmilla-core/inc/Clinic/TherapistCatalogService.php` — Canonical therapist catalog & availability service with timezone validation (`INVALID_TIMEZONE`), future slot validation (`INVALID_SLOT_TIME`), and `clinic.booking` gate.
- `wordpress/packages/carmilla-core/inc/Clinic/TherapistCatalogRestController.php` — Singleton REST route registration for `/api/therapists*`, `/api/admin/therapists*`, `/api/admin/therapists/{id}/slots`.
- `wordpress/packages/carmilla-core/Carmilla_Kernel.php`, `wordpress/packages/carmilla-core/inc/Rest/RestInfrastructure.php` — Loaded and registered `TherapistCatalogRestController`.
- `wordpress/carmilla-bridge/includes/class-cb-clinic-controller.php`, `class-cb-admin-content-controller.php` — Skipped duplicate therapist/slots routes when `TherapistCatalogRestController` is active.
- `wordpress/carmilla-bridge/includes/class-cb-public-renderer.php` — Added `[carmilla_therapists]` and `[carmilla_therapist]` shortcodes.
- `wordpress/carmilla-theme/inc/class-theme-host-adapter.php`, `wordpress/carmilla-theme/carmilla/therapists-list.php`, `wordpress/carmilla-theme/carmilla/therapist-detail.php` — Added standalone shortcodes and theme override templates.
- `wordpress/packages/carmilla-core/tests/test-therapist-catalog-028.php` — 11 real-WordPress assertions.

## Verification Commands & Results
```sh
docker exec test-env-wordpress-test-1 sh -c "php -l ... && php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/test-therapist-catalog-028.php && php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/verify-all-phase04-real-wp.php"
```
- `php -l`: 0 syntax errors.
- `test-therapist-catalog-028.php`: **11 / 11 passed** (exit code `0`).
- `verify-all-phase04-real-wp.php`: **78 / 78 passed** (exit code `0`).
- `git diff --check`: exit code `0`.
