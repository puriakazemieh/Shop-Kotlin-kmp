# P02-SEC-CODE-010C2B3A1 verification — 2026-11-02 / 2026-11-14

Changed the bookings read to return an explicit unavailable result on SQL error or non-array result. The result propagates through the therapist detail, list and matching responses as HTTP 503 `AVAILABILITY_UNAVAILABLE`. Healthy reads continue to count and hide slots from a fresh database read. The booking write path was not changed.

GitNexus pre-edit impact: `booked_slot_times` had one direct caller (`slot_dtos`), five reachable symbols, two affected flows (`therapist_detail`, `submit_match`), LOW graph risk. The public REST callbacks have UNKNOWN caller impact in the graph; registration and callers were confirmed in the controller source.

## Automated checks

- PHP 8.1 / 8.3 lint of controller and smoke tests: exit 0.
- Disposable installed WordPress unit smoke `CARMILLA_LOGIN_DISPOSABLE=yes php tests/smoke-clinic-availability-read-failure-real-wp.php`: exit 0, 8 PASS, 0 FAIL. A synthetic query filter redirected the bookings SELECT to a missing table for each availability endpoint. Healthy detail, list and match returned one free slot; a booked slot was hidden; each injected failure returned 503; normal detail recovered after removing the fault.

## Representative HTTP Network QA (Docker Environment) — 2026-11-14

- **Environment**:
  - WordPress Container: `test-env-wordpress-test-1` (WordPress latest / PHP 8.3.35 on Apache)
  - Database Container: `test-env-db-1` (MariaDB 10.6)
  - Network: Docker bridge (`test-env_default`), Apache listening on port 80 / mapped to host 8080
  - Feature Flags: `cb_enable_health_lms` enabled, `clinic.booking` enabled via `qa-admin-slots-mu.php`

- **Test Command**:
  ```bash
  docker exec -e CARMILLA_LOGIN_DISPOSABLE=yes test-env-wordpress-test-1 php /var/www/html/wp-content/plugins/carmilla-bridge/tests/probe-clinic-availability-http-real-wp.php
  ```

- **Output & Verification**:
  ```
  PASS HTTP GET /api/therapists reports free slot for therapist
  PASS HTTP GET /api/therapists/{slug} returns available slot in slots list
  PASS HTTP POST /api/clinic/therapist-match/submit returns therapist with free slot
  PASS HTTP GET /api/therapists/{slug} hides booked slot (count = 0)
  PASS HTTP GET /api/therapists fails closed on DB error (HTTP 503 AVAILABILITY_UNAVAILABLE)
  PASS HTTP GET /api/therapists/{slug} fails closed on DB error (HTTP 503 AVAILABILITY_UNAVAILABLE)
  PASS HTTP POST /api/clinic/therapist-match/submit fails closed on DB error (HTTP 503 AVAILABILITY_UNAVAILABLE)
  PASS HTTP GET /api/therapists/{slug} recovers healthy availability after fault removal
  PASS Synthetic fixtures cleaned up completely
  Failures: 0.
  Exit Code: 0
  ```

- **Endpoints Verified Over Live HTTP**:
  1. `GET /?rest_route=/carmilla/v1/api/therapists`: Returns HTTP 200 with therapist list and available slots count under normal conditions; returns HTTP 503 `AVAILABILITY_UNAVAILABLE` on simulated DB read failure.
  2. `GET /?rest_route=/carmilla/v1/api/therapists/{slug}`: Returns HTTP 200 with detailed slots under normal conditions; returns HTTP 200 with 0 slots when booked; returns HTTP 503 `AVAILABILITY_UNAVAILABLE` on simulated DB read failure.
  3. `POST /?rest_route=/carmilla/v1/api/clinic/therapist-match/submit`: Returns HTTP 200 with matched therapist and slots under normal conditions; returns HTTP 503 `AVAILABILITY_UNAVAILABLE` on simulated DB read failure.

- **Fault Injection & Recovery**:
  - Injected SQL failure header `X-Carmilla-QA-DB-Failure: synthetic` caught by `qa-admin-slots-mu.php`, rewriting queries for `cb_bookings` to nonexistent table `cb_synthetic_missing_bookings`.
  - All 3 availability endpoints failed closed immediately with HTTP 503 `AVAILABILITY_UNAVAILABLE`, never leaking unverified free capacity.
  - After removing the fault header, subsequent requests immediately recovered healthy HTTP 200 responses.
  - All synthetic posts, meta, and bookings were deleted and verified at 0 rows. Temporary marker and mu-plugin were verified removed.
