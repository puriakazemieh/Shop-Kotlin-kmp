# P14-CLINIC-PERF-CODE-034D — Bound admin clinic patient messages

- Status: TODO
- Phase/Area/Type: P14 / Clinic / CODE
- Priority/Risk/Size: P2 / MEDIUM / M
- Owner: AI
- Depends on: P14-CLINIC-PERF-CODE-034C

## Scope and acceptance

Replace the unbounded `CB_Admin_Clinic_Controller::list_messages` per-patient query with an explicitly bounded, stable pagination contract. Preserve admin authorization, therapist/patient isolation, chronological order, and old-message access across WordPress, Spring and KMP consumers. Characterize existing behavior, then prove complete 30,000-message traversal without duplicates or silent truncation. Record SQL plan, actual scanned rows, latency and memory at 30/3,000/30,000 synthetic comments. Allowed: admin clinic API/client code and focused tests/docs. Evidence: `docs/evidence/P14-CLINIC-PERF-CODE-034D/`; baseline: `docs/evidence/P02-PERF-REVIEW-010C2B3B3/verification.md`.

## Completion record

- Final status: TODO
