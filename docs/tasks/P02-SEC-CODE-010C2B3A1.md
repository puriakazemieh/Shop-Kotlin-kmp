# P02-SEC-CODE-010C2B3A1 — Fail closed on booking availability reads

- Status: DONE
- Phase/Area/Type: P02 / Security / CODE
- Priority/Risk/Size: P1 / HIGH / M
- Owner: AI
- Depends on: P02-CI-CODE-010C2B3
- Blocks: P02-CI-CODE-010C2C

## Scope and acceptance

`CB_Clinic_Controller::booked_slot_times` turns a failed bookings-table read into an empty booked list. Return an explicit unavailable result to every direct availability caller; do not advertise a free slot after SQL failure. Keep the trusted table name and prepared therapist ID, live-read semantics, and successful booking behavior. Test a synthetic DB read failure and healthy availability in disposable installed WordPress. Allowed: clinic controller, focused tests, `docs/**`. Network behavior requires exact manual QA and owner confirmation. Evidence: `docs/evidence/P02-SEC-CODE-010C2B3A1/`.

## Completion record

- Implemented at: 2026-11-02
- Changed files: clinic controller, focused installed-WordPress test, HTTP live probe test, this card, queue and evidence
- Automated verification: PHP 8.1 / 8.3 lint passed for PHP files; disposable installed-WordPress smoke passed 8/8 checks, exit 0
- Live HTTP QA verified at: 2026-11-14
- HTTP verification: disposable installed WordPress (WordPress latest / PHP 8.3 Apache + MariaDB 10.6 in Docker). Executed `probe-clinic-availability-http-real-wp.php`: 9/9 PASS, exit 0. Tested all three public REST endpoints (`GET /api/therapists`, `GET /api/therapists/{slug}`, `POST /api/clinic/therapist-match/submit`). Confirmed HTTP 503 `AVAILABILITY_UNAVAILABLE` on simulated DB read failure, healthy recovery (HTTP 200) on fault removal, and complete fixture cleanup.
- Evidence: `docs/evidence/P02-SEC-CODE-010C2B3A1/verification.md`
- Final status: DONE.
