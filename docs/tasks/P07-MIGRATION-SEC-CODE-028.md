# P07-MIGRATION-SEC-CODE-028 — Prepare migration exclusion queries

- Status: TODO
- Phase/Area/Type: P07 / Migration / Security CODE
- Priority/Risk/Size: P1 / HIGH / S
- Owner: AI
- Depends on: P07-MIGRATION-CODE-019
- Blocks: P07-MIGRATION-PERF-REVIEW-029

## Scope and acceptance

Replace interpolated post-type exclusions in legacy and overlay export queries with `$wpdb->prepare()` value placeholders. Keep trusted table identifiers separate and preserve denied post types. Test generated SQL and export behavior against a disposable WordPress database with adversarial-looking post-type strings; verify excluded records remain excluded and encrypted output contains no excluded data. No production data. Allowed: migration export commands, focused tests, `docs/**`. Evidence: `docs/evidence/P07-MIGRATION-SEC-CODE-028/`.

## Completion record

- Final status: TODO
