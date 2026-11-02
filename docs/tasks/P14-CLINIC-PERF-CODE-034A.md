# P14-CLINIC-PERF-CODE-034A — Bound appointment ownership lookup

- Status: TODO
- Phase/Area/Type: P14 / Clinic / CODE
- Priority/Risk/Size: P2 / MEDIUM / M
- Owner: AI
- Depends on: P14-CLINIC-DATA-023
- Blocks: P14-CLINIC-PERF-CODE-034B

## Scope and acceptance

Optimize `CB_Clinic_Controller::my_appointments` so lookup for a user does not scan all appointment metadata as the synthetic dataset grows. Preserve authorization, newest-first ordering, and the 100-item contract. Characterize existing behavior, implement a targeted query/index strategy, and compare EXPLAIN/ANALYZE and latency on 30/3,000/30,000 synthetic appointments. Verify no cross-user leakage and no schema migration loss. Allowed: clinic WordPress code, its tests/fixtures, migration and docs. Evidence: `docs/evidence/P14-CLINIC-PERF-CODE-034A/`; baseline: `docs/evidence/P02-PERF-REVIEW-010C2B3B2/verification.md`.

## Completion record

- Final status: TODO
