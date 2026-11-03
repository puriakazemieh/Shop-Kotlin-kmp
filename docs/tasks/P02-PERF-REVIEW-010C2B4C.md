# P02-PERF-REVIEW-010C2B4C — Measure admin user-meta scans

- Status: TODO
- Phase/Area/Type: P02 / Performance / REVIEW
- Priority/Risk/Size: P2 / MEDIUM / M
- Owner: AI
- Depends on: P02-SEC-CODE-010C2B4B
- Blocks: P02-PERF-REVIEW-010C2B4D

## Scope and acceptance

Measure seven pinned `get_users(meta_key, number=500)` findings in admin content/admin controller with synthetic bounded cardinalities, including whether review-by-ID misses items beyond 500. Record SQL plans, scanned rows and latency. Queue M-or-smaller product fixes for material performance or correctness defects; no unmeasured waiver. Allowed: read-only product source, synthetic fixtures/tests, `docs/**`. Evidence: `docs/evidence/P02-PERF-REVIEW-010C2B4C/`.

## Completion record

- Final status: TODO
