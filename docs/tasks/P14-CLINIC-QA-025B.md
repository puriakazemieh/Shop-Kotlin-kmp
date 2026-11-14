# P14-CLINIC-QA-025B — HTTP cancellation/slot-lock consistency proof

- Status: TODO
- Phase/Area/Type: P14 / Clinic / QA
- Priority/Risk/Size: P1 / HIGH / S
- Owner: AI; HUMAN only if a representative disposable HTTP environment cannot be supplied or reached
- Depends on: P14-CLINIC-QA-025A
- Blocks: P14-QA-MANUAL-026

## Scope and acceptance

Carry the outstanding network acceptance of `P02-SEC-CODE-010C2B3A3` into the clinic phase. On an owner-enabled disposable WordPress build with public clinic routes, synthetic therapist/patient, and an authenticated appointment, call the real cancellation REST route. Inject a bookings-table delete failure: response must be unavailable and both appointment status and slot lock must remain consistent. Remove the fault and retry: cancellation succeeds and releases the slot exactly once. Verify unauthorized/cross-patient denial, HTTP status/body, database rows before/after, sanitized logs and rollback. Record exact route/build fingerprint, steps and results under `docs/evidence/P14-CLINIC-QA-025B/`. No production or real patient data. Agent may mark DONE only after full representative HTTP proof; otherwise AWAITING_MANUAL_QA and exact Persian steps in chat. Earlier installed-WordPress evidence: `docs/evidence/P02-SEC-CODE-010C2B3A3/verification.md`.

## Completion record

- Status: TODO (queued for Phase 14).
- Note (2026-11-14): The representative HTTP network QA for `P02-SEC-CODE-010C2B3A3` was executed directly in Docker (`test-env-wordpress-test-1` / PHP 8.3 Apache + MariaDB 10.6) via `wordpress/carmilla-bridge/tests/probe-clinic-cancellation-http-real-wp.php` with 6/6 PASS (exit 0). Complete HTTP evidence recorded in `docs/evidence/P02-SEC-CODE-010C2B3A3/verification.md`.
