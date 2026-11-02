# P02-PERF-REVIEW-010C2B3B1 — Measure academy lesson query

- Status: DONE
- Phase/Area/Type: P02 / Performance / REVIEW
- Priority/Risk/Size: P2 / MEDIUM / S
- Owner: AI
- Depends on: P02-CI-CODE-010C2B3
- Blocks: P02-PERF-REVIEW-010C2B3B2

## Scope and acceptance

Measure the two pinned `lesson_questions` SlowDBQuery findings with synthetic small and large course comment counts. Record query plan, scanned rows and latency; classify bounded behavior or create a scoped fix card for material slow scans. No unmeasured waiver. Allowed: read-only academy source, synthetic fixture/test and `docs/**`. Evidence: `docs/evidence/P02-PERF-REVIEW-010C2B3B1/`.

## Completion record

- Completed at: 2026-11-02
- Verification: actual SQL `EXPLAIN`/`ANALYZE` and timings at 30, 3,000, 30,000 synthetic course comments; temporary index experiment; complete cleanup
- Evidence: `docs/evidence/P02-PERF-REVIEW-010C2B3B1/verification.md`, raw plans and probe
- Follow-up: `P02-PERF-DISC-010C2B3B1A` for bounded pagination contract
- Final status: DONE
