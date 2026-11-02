# P02-SEC-REVIEW-010C2B3A2A — booking partial writes

2026-11-02. Disposable installed WordPress fixture (WordPress 7.1.3, PHP 8.3.35, MariaDB 10.6.28), `CARMILLA_LOGIN_DISPOSABLE=yes php /tmp/cb-partial-write-probe.php`, exit 0, four scenario observations, PHP lint exit 0. The test creates a synthetic therapist and future slot per case, invokes `CB_Clinic_Controller::book()` with a `WP_REST_Request`, rewrites exactly one SQL query *after* the slot-lock `INSERT IGNORE` to a missing synthetic table, then reads the actual booking row, appointment post and metadata. It cleans all synthetic rows after each case; final SQL checks found zero `QA booking` posts and zero booking rows with missing therapist posts. The public booking route is currently disabled; `api=` below is the controller response/status or thrown exception, not a live HTTP route result.

| Fault after lock | Injected | Controller result | Residual lock | Appointment post | `cb_*` meta | Observation |
| --- | --- | --- | ---: | ---: | ---: | --- |
| `wp_insert_post` SQL | yes | `TypeError` in `appointment_dto(null)` | 1, `appointment_id=0` | 0 | 0 | Slot remains unusable; no API response. |
| First `cb_therapist_id` metadata INSERT | yes | 201 | 1, points to post | 1 | 4/5 | False success with incomplete appointment. |
| Booking-row `appointment_id` UPDATE | yes | 201 | 1, `appointment_id=0` | 1 | 5/5 | False success; slot lock is disconnected from appointment. |
| Healthy | no | 201 | 1, points to post | 1 | 5/5 | Baseline succeeds. |

Bounded correction for the next code card: run the lock insert, `wp_insert_post`, required metadata writes, and booking-row update inside one InnoDB transaction; check each return value and the stored row identity, then commit only after all succeed. On any failure, roll back the database transaction, clear any WordPress post/meta caches touched before rollback, and return an explicit unavailable response. Preserve UNIQUE slot semantics and distinguish duplicate slot from SQL failure. Test the same three injected faults, successful DTO and duplicate slot on installed WordPress. WordPress hooks can have effects outside the database transaction; inspect those side effects and do not claim external atomicity from DB rollback alone. This review changed no product code and does not claim public booking is enabled.
