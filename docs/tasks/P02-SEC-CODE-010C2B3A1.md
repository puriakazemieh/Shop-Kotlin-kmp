# P02-SEC-CODE-010C2B3A1 — Fail closed on booking availability reads

- Status: READY
- Phase/Area/Type: P02 / Security / CODE
- Priority/Risk/Size: P1 / HIGH / M
- Owner: AI
- Depends on: P02-CI-CODE-010C2B3
- Blocks: P02-CI-CODE-010C2C

## Scope and acceptance

`CB_Clinic_Controller::booked_slot_times` turns a failed bookings-table read into an empty booked list. Return an explicit unavailable result to every direct availability caller; do not advertise a free slot after SQL failure. Keep the trusted table name and prepared therapist ID, live-read semantics, and successful booking behavior. Test a synthetic DB read failure and healthy availability in disposable installed WordPress. Allowed: clinic controller, focused tests, `docs/**`. Network behavior requires exact manual QA and owner confirmation. Evidence: `docs/evidence/P02-SEC-CODE-010C2B3A1/`.

## Completion record

- Final status: READY
