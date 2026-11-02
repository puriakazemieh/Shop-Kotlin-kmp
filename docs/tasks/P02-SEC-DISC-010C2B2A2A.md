# P02-SEC-DISC-010C2B2A2A — Define managed deletion integrity contract

- Status: READY
- Phase/Area/Type: P02 / Security / DISC
- Priority/Risk/Size: P1 / HIGH / S
- Owner: AI
- Depends on: P02-SEC-CODE-010C2B2A1; owner authorization 2026-11-02
- Blocks: P02-SEC-CODE-010C2B2A2B

## Scope and acceptance

Read-only design for standard single-site WordPress. Inventory the installed schema, storage engines, post/comment writer behavior, current orphan rows and existing constraints/triggers in a disposable fixture. Specify the database-enforced invariant for managed user deletion, including `post_author=0`, anonymous `comment.user_id=0`, legacy rows, other plugins/direct SQL, fail-closed unsupported topologies, migration, backup and rollback. Demonstrate the present race with two independent synthetic connections. Produce a bounded implementation plan, compatibility risks and exact SQL/WordPress verification steps; do not alter product schema or code in this card. Evidence: `docs/evidence/P02-SEC-DISC-010C2B2A2A/`.

## Completion record

- Final status: READY
