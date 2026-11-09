# P02-SEC-CODE-010C2B3A1 — Fail closed on booking availability reads

- Status: CODE_COMPLETE (HTTP acceptance transferred to P14-CLINIC-QA-025A)
- Phase/Area/Type: P02 / Security / CODE
- Priority/Risk/Size: P1 / HIGH / M
- Owner: AI
- Depends on: P02-CI-CODE-010C2B3
- Blocks: P02-CI-CODE-010C2C

## Scope and acceptance

`CB_Clinic_Controller::booked_slot_times` turns a failed bookings-table read into an empty booked list. Return an explicit unavailable result to every direct availability caller; do not advertise a free slot after SQL failure. Keep the trusted table name and prepared therapist ID, live-read semantics, and successful booking behavior. Test a synthetic DB read failure and healthy availability in disposable installed WordPress. Allowed: clinic controller, focused tests, `docs/**`. Network behavior requires exact manual QA and owner confirmation. Evidence: `docs/evidence/P02-SEC-CODE-010C2B3A1/`.

## Completion record

- Implemented at: 2026-11-02
- Changed files: clinic controller, focused installed-WordPress test, this card, queue and evidence
- Automated verification: PHP 8.1 lint passed for both PHP files; disposable installed-WordPress smoke passed 8/8 checks, exit 0
- Evidence: `docs/evidence/P02-SEC-CODE-010C2B3A1/verification.md`
- Manual QA: pending owner confirmation of the three REST responses on a disposable WordPress installation
- Prior status: AWAITING_MANUAL_QA
- Reclassified 2026-11-09: implementation and installed-WordPress checks remain as recorded; HTTP availability QA is owned by `P14-CLINIC-QA-025A` before the P14 clinic UAT/gate. This card is CODE_COMPLETE, not a claim that HTTP acceptance passed.
- Current status: CODE_COMPLETE.
