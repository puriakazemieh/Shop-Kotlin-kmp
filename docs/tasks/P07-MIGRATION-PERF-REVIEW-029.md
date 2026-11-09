# P07-MIGRATION-PERF-REVIEW-029 — Measure migration snapshot query cost

- Status: TODO
- Phase/Area/Type: P07 / Migration / Performance REVIEW
- Priority/Risk/Size: P1 / MEDIUM / S
- Owner: AI
- Depends on: P07-MIGRATION-SEC-CODE-028
- Blocks: P07-MIGRATION-GATE-027

## Scope and acceptance

Measure legacy and overlay export full-post/attachment scans, per-post meta queries, memory use, and elapsed time in disposable WordPress at small and representative high cardinalities. Confirm whether one-shot uncached reads are justified and whether bounded streaming or a code fix is needed before the migration gate. Queue a bounded implementation card if measurements exceed the stated resource ceiling; do not waive the WPCS direct-query/caching findings without evidence. No production data. Allowed: read-only migration source, focused probes, `docs/**`. Evidence: `docs/evidence/P07-MIGRATION-PERF-REVIEW-029/`.

## Completion record

- Final status: TODO
