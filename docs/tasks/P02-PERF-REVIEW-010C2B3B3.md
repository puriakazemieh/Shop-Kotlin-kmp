# P02-PERF-REVIEW-010C2B3B3 — Measure admin clinic patient query

- Status: TODO
- Phase/Area/Type: P02 / Performance / REVIEW
- Priority/Risk/Size: P2 / MEDIUM / S
- Owner: AI
- Depends on: P02-PERF-REVIEW-010C2B3B2
- Blocks: P02-CI-CODE-010C2C

## Scope and acceptance

Measure the two pinned admin-clinic `list_messages` SlowDBQuery findings with synthetic per-patient comment counts. Record query plan, scanned rows and latency; create a scoped fix card if materially slow. No unmeasured waiver. Allowed: read-only admin-clinic source, synthetic fixture/test and `docs/**`. Evidence: `docs/evidence/P02-PERF-REVIEW-010C2B3B3/`.

## Completion record

- Final status: TODO
