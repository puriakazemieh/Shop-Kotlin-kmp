# P14-CLINIC-PERF-CODE-034B — Bound clinic message listing

- Status: TODO
- Phase/Area/Type: P14 / Clinic / CODE
- Priority/Risk/Size: P2 / HIGH / M
- Owner: AI
- Depends on: P14-CLINIC-PERF-CODE-034A
- Blocks: P14-CLINIC-PERF-CODE-034C

## Scope and acceptance

Replace the unbounded `CB_Clinic_Controller::list_messages` read with an explicitly bounded, stable pagination contract. Preserve per-user/therapist isolation and chronological presentation; coordinate the WordPress, Spring and KMP contract so existing consumers do not silently lose older messages. Characterize current behavior and prove pagination covers all 30,000 synthetic messages without duplication or 128 MiB PHP exhaustion. Record plan, scanned rows, latency and memory. Allowed: clinic API/client code and focused tests/docs. Evidence: `docs/evidence/P14-CLINIC-PERF-CODE-034B/`; baseline: `docs/evidence/P02-PERF-REVIEW-010C2B3B2/verification.md`.

## Completion record

- Final status: TODO
