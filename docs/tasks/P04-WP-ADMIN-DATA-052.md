# P04-WP-ADMIN-DATA-052 — Index admin request/attempt ownership

- Status: TODO
- Phase/Area/Type: P04 / WordPress Admin / DATA
- Priority/Risk/Size: P1 / HIGH / M
- Owner: AI
- Depends on: P04-ENTITLEMENT-CODE-044
- Blocks: P04-WP-ADMIN-CODE-053

## Scope and acceptance

Design and add a durable mapping from refund, therapist-switch, psych-attempt and return item IDs to their owning user IDs so review-by-ID does not search a capped `get_users(meta_key)` result. Preserve existing user-meta payloads and IDs. Backfill historical synthetic fixtures, test idempotent migration/restore and ownership consistency, and specify repair behavior for missing/stale mappings. No real user data. Allowed: WordPress admin data/migration code, focused tests, docs. Evidence: `docs/evidence/P04-WP-ADMIN-DATA-052/`; baseline: `docs/evidence/P02-PERF-REVIEW-010C2B4C/verification.md`.

## Completion record

- Final status: TODO
