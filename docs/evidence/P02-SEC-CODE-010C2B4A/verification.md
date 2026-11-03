# Admin therapist slot read failure — 2026-11-03

## Change

`therapist_slots` now clears stale `$wpdb->last_error`, checks the fresh booking SELECT, and returns HTTP 503 `AVAILABILITY_UNAVAILABLE` if the read fails or does not return an array. It never treats that failure as an empty booking list. Trusted table naming, prepared therapist ID and live no-cache read remain in place.

Pre-edit GitNexus impact for the exact `therapist_slots` method returned `risk: UNKNOWN`, zero resolved callers/processes. Source inspection confirmed the dynamic REST registration at `class-cb-admin-content-controller.php:64-66`, protected by `CB_Plugin::require_admin`; graph absence was not treated as no impact.

## Verification

- Baseline installed WordPress test: `docker exec -e CARMILLA_LOGIN_DISPOSABLE=yes test-env-wordpress-test-1 php /var/www/html/wp-content/plugins/carmilla-bridge/tests/smoke-admin-therapist-slots-read-failure-real-wp.php` → exit 1, expected failure: DB read error reported a free slot (see `baseline.txt`).
- After fix, same direct installed-WordPress test → exit 0, 4 PASS, 0 FAIL (`verification-output.txt`).
- Actual HTTP test against disposable Apache WordPress: temporarily installed `qa-admin-slots-mu.php` guarded by `/tmp/carmilla-qa-admin-slots-enabled`, enabled only the test's `clinic.booking` manifest route and local application-password authentication, created a synthetic admin/therapist/slot, and called `http://127.0.0.1/?rest_route=/carmilla/v1/api/admin/therapists/{id}/slots` with Basic application password. A test header redirected only the booking SELECT to a missing synthetic table. `smoke-admin-therapist-slots-http-real-wp.php` → exit 0, 6 PASS, 0 FAIL: unauthenticated denial, free slot 200, booked slot 200, injected SQL failure 503 with `AVAILABILITY_UNAVAILABLE`, recovery 200, fixture cleanup (`http-output.txt`). Temporary MU plugin and marker were removed after the run.
- PHP lint of modified controller and both smoke scripts → no syntax errors. Synthetic WordPress user, therapist and booking lock were removed; no production data or external account was used.

The HTTP acceptance route, authorization and recovery were verified here; no owner manual QA is required for this task.
