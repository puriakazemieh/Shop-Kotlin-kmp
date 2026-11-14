# P02-SEC-CODE-010C2B3A3 — Keep cancellation and slot lock consistent

- Status: DONE
- Phase/Area/Type: P02 / Security / CODE
- Priority/Risk/Size: P1 / HIGH / M
- Owner: AI
- Depends on: P02-CI-CODE-010C2B3
- Blocks: P02-CI-CODE-010C2C

## Scope and acceptance

`CB_Clinic_Controller::cancel` currently writes CANCELLED before an unchecked bookings-table delete. On synthetic delete failure, return an unavailable result and keep appointment and lock state consistent; on success, release the slot exactly once. Test installed WordPress with disposable appointment data and injected DB fault. Allowed: clinic controller, focused tests, `docs/**`. Network behavior requires manual QA and owner confirmation. Evidence: `docs/evidence/P02-SEC-CODE-010C2B3A3/`.

## Completion record

- Implemented at: 2026-11-02
- Verification: installed synthetic WordPress six checks PASS; controller and test PHP lint PASS; no synthetic SQL residue
- Live HTTP QA verified at: 2026-11-14
- HTTP verification: disposable installed WordPress (WordPress latest / PHP 8.3 Apache + MariaDB 10.6 in Docker). Executed `probe-clinic-cancellation-http-real-wp.php`: 6/6 PASS, exit 0. Tested authenticated REST cancellation route (`POST /api/clinic/appointments/{id}/cancel`). Confirmed HTTP 401/403 for unauthenticated, HTTP 404 for cross-patient, HTTP 503 `CANCELLATION_UNAVAILABLE` on simulated slot delete failure with atomic rollback preserving PENDING status and surviving lock, healthy cancellation (HTTP 200) updating status to CANCELLED and releasing lock, idempotent repeat call (HTTP 200), and complete fixture cleanup.
- Evidence: `docs/evidence/P02-SEC-CODE-010C2B3A3/verification.md` and `tests/smoke-booking-cancel-atomic-real-wp.php`
- Final status: DONE.
