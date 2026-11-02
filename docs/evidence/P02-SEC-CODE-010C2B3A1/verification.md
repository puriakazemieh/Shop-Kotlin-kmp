# P02-SEC-CODE-010C2B3A1 verification — 2026-11-02

Changed the bookings read to return an explicit unavailable result on SQL error or non-array result. The result propagates through the therapist detail, list and matching responses as HTTP 503 `AVAILABILITY_UNAVAILABLE`. Healthy reads continue to count and hide slots from a fresh database read. The booking write path was not changed.

GitNexus pre-edit impact: `booked_slot_times` had one direct caller (`slot_dtos`), five reachable symbols, two affected flows (`therapist_detail`, `submit_match`), LOW graph risk. The public REST callbacks have UNKNOWN caller impact in the graph; registration and callers were confirmed in the controller source.

Automated checks:

- PHP 8.1 lint of controller and smoke test: both exit 0.
- Disposable installed WordPress `CARMILLA_LOGIN_DISPOSABLE=yes php /tmp/smoke-clinic-availability-read-failure-real-wp.php`: exit 0, 8 PASS, 0 FAIL. A synthetic query filter redirected the bookings SELECT to a missing table for each availability endpoint. Healthy detail, list and match returned one free slot; a booked slot was hidden; each injected failure returned 503; normal detail recovered after removing the fault.

## Manual network QA for owner

Environment: disposable installed WordPress with the updated Bridge, a published synthetic `cb_therapist`, a future `cb_slots` entry, and the bookings table created by plugin activation. Do not use a production site. The test script `wordpress/carmilla-bridge/tests/smoke-clinic-availability-read-failure-real-wp.php` creates and cleans its own therapist fixture.

1. With a browser or REST client, GET `/wp-json/carmilla/v1/api/therapists` and GET `/wp-json/carmilla/v1/api/therapists/{slug}` for the synthetic therapist; POST `/wp-json/carmilla/v1/api/clinic/therapist-match/submit` with `{"selectedTags":["synthetic-anxiety"]}`. Use the actual namespace from `CB_REST_NAMESPACE` if configured differently. Expect HTTP 200 and one available slot / count 1 in all three applicable payloads.
2. Add a synthetic booking for that therapist and slot, repeat the reads. Expect HTTP 200 with no free slot / count 0.
3. In the disposable environment only, inject a missing-table error for `SELECT slot_time FROM <prefix>cb_bookings` using the test's query filter, then repeat all three HTTP requests. Expect HTTP 503 with `errorCode=AVAILABILITY_UNAVAILABLE`; no response may report free capacity.
4. Remove the fault, remove the synthetic booking and repeat. Expect HTTP 200 and the free slot restored. Confirm a normal booking still succeeds through the existing booking route when enabled in that fixture.

Record the WordPress/PHP versions, exact URLs, HTTP status and response evidence, tester/date, and whether the behavior passed. Keep this card `AWAITING_MANUAL_QA` until owner confirmation.
