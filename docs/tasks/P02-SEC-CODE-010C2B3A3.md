# P02-SEC-CODE-010C2B3A3 — Keep cancellation and slot lock consistent

- Status: AWAITING_MANUAL_QA
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
- Evidence: `docs/evidence/P02-SEC-CODE-010C2B3A3/verification.md` and `tests/smoke-booking-cancel-atomic-real-wp.php`
- Manual QA: public booking/cancellation route is currently disabled; authenticated HTTP cancellation and injected failure remain for owner-enabled disposable build
- Final status: AWAITING_MANUAL_QA
