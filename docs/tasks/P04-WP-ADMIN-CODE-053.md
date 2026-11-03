# P04-WP-ADMIN-CODE-053 — Resolve admin review item directly

- Status: TODO
- Phase/Area/Type: P04 / WordPress Admin / CODE
- Priority/Risk/Size: P1 / HIGH / M
- Owner: AI
- Depends on: P04-WP-ADMIN-DATA-052
- Blocks: P04-WP-ADMIN-CODE-054

## Scope and acceptance

Use the indexed ownership mapping in `interpret`, shared refund/switch review and `update_return`. Do not return false 404 for an existing item beyond the first 500 users; never mutate a different user's item. Preserve admin authorization, status transitions and response shapes. Verify 30/501/3,000 synthetic-user scenarios and missing/stale mapping behavior through real WordPress routes. Allowed: WordPress admin controllers, focused tests, docs. Evidence: `docs/evidence/P04-WP-ADMIN-CODE-053/`.

## Completion record

- Final status: TODO
