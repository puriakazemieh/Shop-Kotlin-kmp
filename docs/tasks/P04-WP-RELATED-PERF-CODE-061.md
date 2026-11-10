# P04-WP-RELATED-PERF-CODE-061 — Measure related item queries

- Status: TODO
- Phase/Area/Type: P04 / WordPress / PERF-CODE
- Priority/Risk/Size: P2 / MEDIUM / S
- Owner: AI
- Depends on: P04-WP-CONTENT-PERF-CODE-057
- Blocks: P04-CI-CODE-019

## Scope and acceptance

Measure the related-product `exclude` and related-blog `post__not_in` queries flagged by Plugin Check at 30 and 3,000 synthetic items. Record actual SQL, EXPLAIN/scanned rows, latency, response order and four-item result correctness. Replace with a bounded strategy if material, or retain source-backed exact warning disposition with measured limits. Evidence: `docs/evidence/P04-WP-RELATED-PERF-CODE-061/`.

## Completion record

- Final status: TODO; owner from P02 C3B3D triage.
