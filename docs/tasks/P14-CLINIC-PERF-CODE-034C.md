# P14-CLINIC-PERF-CODE-034C — Optimize clinic message status count

- Status: TODO
- Phase/Area/Type: P14 / Clinic / CODE
- Priority/Risk/Size: P2 / MEDIUM / S
- Owner: AI
- Depends on: P14-CLINIC-PERF-CODE-034B

## Scope and acceptance

Make `CB_Clinic_Controller::messaging_status` count accurately for the authorized user/therapist without a global `wp_commentmeta` scan. Preserve pending/approved semantics and response shape. Compare EXPLAIN/ANALYZE, scanned rows and latency on 30/3,000/30,000 synthetic messages, including cross-user isolation and migration/restore checks for any index. Allowed: clinic WordPress code, focused tests/fixtures, migration and docs. Evidence: `docs/evidence/P14-CLINIC-PERF-CODE-034C/`; baseline: `docs/evidence/P02-PERF-REVIEW-010C2B3B2/verification.md`.

## Completion record

- Final status: TODO
