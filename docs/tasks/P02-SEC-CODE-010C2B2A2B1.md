# P02-SEC-CODE-010C2B2A2B1 — Verify deletion integrity topology

- Status: READY
- Phase/Area/Type: P02 / Security / CODE
- Priority/Risk/Size: P1 / HIGH / S
- Owner: AI
- Depends on: P02-SEC-DISC-010C2B2A2A
- Blocks: P02-SEC-CODE-010C2B2A2B2

## Scope and acceptance

Create a narrowly scoped read-only PHP verifier for single-site InnoDB users/posts/comments, exact supported column types, expected generated-column/FK state, and nonzero post/comment orphan counts. It must distinguish uninstalled, installed, partial, unsupported and unreadable states and never return a clean result after SQL failure. No DDL or deletion changes. Exercise healthy, missing/partial constraint, orphan and read-failure fixtures in disposable WordPress. Allowed: Bridge integrity helper, focused tests and `docs/**`. Evidence: `docs/evidence/P02-SEC-CODE-010C2B2A2B1/`.

## Completion record

- Final status: READY
