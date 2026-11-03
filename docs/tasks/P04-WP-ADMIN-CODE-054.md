# P04-WP-ADMIN-CODE-054 — Page admin user-meta lists

- Status: TODO
- Phase/Area/Type: P04 / WordPress Admin / CODE
- Priority/Risk/Size: P2 / MEDIUM / M
- Owner: AI
- Depends on: P04-WP-ADMIN-CODE-053
- Blocks: P04-CI-CODE-019

## Scope and acceptance

Expose bounded, stable pagination for refund, switch, pending interpretation and return request lists. Preserve the legacy route's response shape until consumers can opt into the new contract; never silently omit records after user 500 in the new route. Verify full traversal of 3,000 synthetic users without duplicates, admin authorization, SQL plan/scanned rows and memory/latency. Coordinate client adoption as a separate task if required. Allowed: WordPress admin API, focused tests, docs. Evidence: `docs/evidence/P04-WP-ADMIN-CODE-054/`.

## Completion record

- Final status: TODO
