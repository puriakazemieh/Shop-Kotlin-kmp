# P02-SEC-CODE-010C2B3A3 — cancellation consistency

2026-11-02. GitNexus pre-edit impact on actual `CB_Clinic_Controller` was CRITICAL (148 impacted symbols); `cancel` was not independently resolved. Source verification found the public cancellation route currently bound to `disabled_feature`. Change was limited to `cancel()`: require InnoDB postmeta/bookings, start a transaction, lock the unique therapist/slot row, read status under lock, update `cb_status`, delete exactly one matching booking row, then commit. On any SQL/write failure, roll back, clear post-meta cache and return 503 `CANCELLATION_UNAVAILABLE`. Repeated cancellation with `CANCELLED` and no lock returns 200. Ownership check remains before writes.

## Unit / Installed-WordPress Verification (2026-11-02)

Disposable installed WordPress fixture (WordPress 7.1.3, PHP 8.3.35, MariaDB 10.6.28), `CARMILLA_LOGIN_DISPOSABLE=yes php tests/smoke-booking-cancel-atomic-real-wp.php`: exit 0, 6 PASS/0 FAIL. Injected unreadable table-engine metadata, status-meta UPDATE failure, and booking-row DELETE failure each returned 503 with stored status `PENDING` and one surviving slot lock. Healthy retry returned 200, stored status `CANCELLED`, zero lock rows; repeated call returned 200 with zero locks; another user received 404 without state change. Controller and test PHP lint passed. SQL cleanup checks found zero synthetic cancel posts/users and zero booking rows with missing therapist posts. No production data was changed.

## Representative HTTP Network QA (Docker Environment) — 2026-11-14

- **Environment**:
  - WordPress Container: `test-env-wordpress-test-1` (WordPress latest / PHP 8.3.35 on Apache)
  - Database Container: `test-env-db-1` (MariaDB 10.6)
  - Network: Docker bridge (`test-env_default`), Apache listening on port 80 / mapped to host 8080
  - Feature Flags: `cb_enable_health_lms` enabled, `clinic.booking` enabled, cancellation route enabled via `qa-clinic-cancel-mu.php`

- **Test Command**:
  ```bash
  docker exec -e CARMILLA_LOGIN_DISPOSABLE=yes test-env-wordpress-test-1 php /var/www/html/wp-content/plugins/carmilla-bridge/tests/probe-clinic-cancellation-http-real-wp.php
  ```

- **Output & Verification**:
  ```
  PASS HTTP unauthenticated request is denied (401/403) and state preserved
  PASS HTTP cross-patient cancellation is rejected (404 NOT_FOUND) and state preserved
  PASS HTTP slot-delete failure fails closed (HTTP 503 CANCELLATION_UNAVAILABLE) with rollback preserving PENDING status and lock
  PASS HTTP healthy cancellation succeeds (HTTP 200), updates status to CANCELLED and releases lock
  PASS HTTP repeat cancellation is idempotent (HTTP 200)
  PASS Synthetic fixtures cleaned up completely
  Failures: 0.
  Exit Code: 0
  ```

- **Scenarios Verified Over Live HTTP**:
  1. `POST /?rest_route=/carmilla/v1/api/clinic/appointments/{id}/cancel` (Anonymous):
     - Rejected with HTTP 401 or 403.
     - Database state preserved: `cb_status = 'PENDING'`, slot lock count = 1.
  2. `POST /?rest_route=/carmilla/v1/api/clinic/appointments/{id}/cancel` (Cross-patient / User B):
     - Rejected with HTTP 404 NOT_FOUND.
     - Database state preserved: `cb_status = 'PENDING'`, slot lock count = 1.
  3. `POST /?rest_route=/carmilla/v1/api/clinic/appointments/{id}/cancel` (Patient User A with injected DB fault):
     - Injected header `X-Carmilla-QA-DB-Failure: synthetic` triggered query filter rewriting `DELETE FROM ... cb_bookings` to nonexistent table.
     - Response: HTTP 503 `CANCELLATION_UNAVAILABLE`.
     - Transaction rollback confirmed: `cb_status` remains `PENDING`, slot lock row survives in `cb_bookings` (count = 1). Status and lock remain strictly consistent.
  4. `POST /?rest_route=/carmilla/v1/api/clinic/appointments/{id}/cancel` (Patient User A healthy retry):
     - Response: HTTP 200 OK.
     - Database state confirmed: `cb_status` updated to `CANCELLED`, slot lock row deleted from `cb_bookings` (count = 0).
  5. `POST /?rest_route=/carmilla/v1/api/clinic/appointments/{id}/cancel` (Patient User A repeat call):
     - Response: HTTP 200 OK (idempotent).
     - Database state confirmed: `cb_status` remains `CANCELLED`, slot lock count remains 0.
  6. **Teardown & Cleanup**:
     - All synthetic users, therapist, appointment, and booking rows were removed and confirmed at 0 rows.
     - Temporary mu-plugin and marker file were cleanly unlinked.
