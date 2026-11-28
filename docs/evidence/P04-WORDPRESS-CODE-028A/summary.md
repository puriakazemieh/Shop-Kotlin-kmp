# Evidence: P04-WORDPRESS-CODE-028A

- **Task ID**: `P04-WORDPRESS-CODE-028A`
- **Status**: `DONE`
- **Date**: `2026-11-28`
- **Repository**: `d:\Android\AndroidStudioProjects\kmp-shop` (`develop`)

## Changes Implemented

1. **Shared Core Appointment Service** (`wordpress/packages/carmilla-core/inc/Clinic/AppointmentBookingService.php`):
   - Centralized atomic slot locking via `{prefix}cb_bookings` (`UNIQUE KEY uniq_slot (therapist_id, slot_time)`), `ensure_bookings_table()` (idempotent DDL for Theme-only mode), InnoDB engine verification, and transactional `START TRANSACTION` / `COMMIT` / `ROLLBACK`.
   - `book()`: supports both canonical `{ slotId, notes }` and legacy `booking.js` `{ therapistId, slot, notes }` payloads.
   - `cancel()`: atomic `FOR UPDATE` lock + status transition to `CANCELLED` + slot lock release (idempotent, owner-checked).
   - `my_appointments()`, `receipt()`, `admin_list_appointments()`, `admin_confirm_appointment()`, `admin_complete_appointment()`.
   - Gated by `clinic.booking` (`TherapistCatalogService::is_enabled()`).

2. **Shared REST Controller** (`wordpress/packages/carmilla-core/inc/Clinic/AppointmentBookingRestController.php`):
   - Singleton registration in `RestInfrastructure::on_rest_api_init()` with duplicate-route guard.
   - Canonical endpoints under `/carmilla/v1`:
     - `GET /api/clinic/my-appointments`
     - `POST /api/clinic/appointments`
     - `POST /api/clinic/appointments/(?P<id>\d+)/cancel`
     - `GET /api/clinic/appointments/(?P<id>\d+)/receipt`
     - `GET /api/admin/therapists/appointments`
     - `POST /api/admin/therapists/appointments/(?P<id>\d+)/confirm`
     - `POST /api/admin/therapists/appointments/(?P<id>\d+)/complete`
   - Backwards-compatible legacy aliases for Theme JS:
     - `GET /appointments`, `POST /appointments`, `POST /appointments/(?P<id>\d+)/cancel`, `GET /appointments/(?P<id>\d+)/receipt`

3. **Route Mapping (`booking.js` & `appointments.js` -> Canonical Contract)**:
   - `wordpress/carmilla-theme/assets/js/booking.js`: `POST {restUrl}appointments` -> `POST {restUrl}api/clinic/appointments` (payload `{ therapistId, slot }`, accepted by `AppointmentBookingService::book()` alongside `{ slotId }`).
   - `wordpress/carmilla-theme/assets/js/appointments.js`: `GET {restUrl}appointments` -> `GET {restUrl}api/clinic/my-appointments`; `POST {restUrl}appointments/{id}/cancel` -> `POST {restUrl}api/clinic/appointments/{id}/cancel`; `GET {restUrl}appointments/{id}/receipt` -> `GET {restUrl}api/clinic/appointments/{id}/receipt`.
   - Legacy `/carmilla/v1/appointments*` routes preserved as aliases in `AppointmentBookingRestController`.

4. **Bridge & Theme Host Integration**:
   - `wordpress/packages/carmilla-core/Carmilla_Kernel.php`: loads `AppointmentBookingService` & `AppointmentBookingRestController`, ensures bookings table on kernel boot.
   - `wordpress/packages/carmilla-core/inc/Rest/RestInfrastructure.php`: registers `AppointmentBookingRestController::register_routes()`.
   - `wordpress/carmilla-bridge/includes/class-cb-clinic-controller.php`: guards duplicate REST route registration and delegates `book()`/`cancel()` to `AppointmentBookingService`.
   - `wordpress/carmilla-bridge/includes/class-cb-admin-content-controller.php`: guards duplicate `/api/admin/therapists/appointments*` route registration.
   - `wordpress/carmilla-bridge/includes/class-cb-public-renderer.php` & `wordpress/carmilla-theme/inc/class-theme-host-adapter.php`: registered `[carmilla_booking]` and `[carmilla_my_appointments]` shortcodes with `clinic.booking` gate and Theme overrides (`wordpress/carmilla-theme/carmilla/booking-form.php`, `wordpress/carmilla-theme/carmilla/my-appointments.php`).

## Verification & Results (Real WordPress 7.1.3 / MariaDB 10.6.28 Docker)

- **PHP Lint (`php -l` in `test-env-wordpress-test-1`)**: 10/10 files passed with 0 syntax errors.
- **Task Behavioral Test (`test-appointment-booking-028a.php`)**: `20 passed, 0 failed.`
- **Atomic Booking Smoke Test (`smoke-booking-atomic-real-wp.php`)**: `Failures: 0.` (all fault injections rolled back cleanly, healthy booking + `409 SLOT_TAKEN` verified).
- **Atomic Cancel Smoke Test (`smoke-booking-cancel-atomic-real-wp.php`)**: `Failures: 0.` (all fault injections rolled back cleanly, idempotent cancel + ownership check verified).
- **Master Phase 04 Verification (`verify-all-phase04-real-wp.php`)**: `ALL 78 MASTER PHASE 04 REAL WORDPRESS CHECKS PASSED!` (Theme-only, Plugin-only on Twenty Twenty-Four, Co-install).
- **`git diff --check`**: exit code `0`.

## Limitations

- Production readiness of full clinical consultation/private files remains gated behind Phase P14 (`P14-CLINIC-*`), as specified in the task card.
