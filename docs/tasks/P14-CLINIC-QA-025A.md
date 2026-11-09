# P14-CLINIC-QA-025A — HTTP availability fail-closed proof

- Status: TODO
- Phase/Area/Type: P14 / Clinic / QA
- Priority/Risk/Size: P1 / HIGH / S
- Owner: AI; HUMAN only if a representative disposable HTTP environment cannot be supplied or reached
- Depends on: P14-QA-AUTO-025
- Blocks: P14-CLINIC-QA-025B, P14-QA-MANUAL-026

## Scope and acceptance

Carry the outstanding network acceptance of `P02-SEC-CODE-010C2B3A1` into the clinic phase. On an owner-enabled disposable WordPress build with the public clinic route enabled and synthetic therapist/appointments, authenticate the allowed roles and call the actual availability REST endpoints. Compare healthy occupied/free slots with a deliberately failed bookings-table read. Every affected availability response must fail closed, never advertise a free slot on SQL failure; healthy response and role/ownership gates remain correct. Inspect HTTP status and body, sanitized logs and database state. Record build/route fingerprint, exact requests, responses, fault injection and rollback under `docs/evidence/P14-CLINIC-QA-025A/`. No production or real patient data. If the agent can run the full representative HTTP scenario, it may mark DONE with evidence; otherwise AWAITING_MANUAL_QA and exact Persian steps in chat. Earlier installed-WordPress evidence: `docs/evidence/P02-SEC-CODE-010C2B3A1/verification.md`.

## Completion record

- Final status: TODO; HTTP acceptance not yet executed.
