# P02-SEC-CODE-010C2B2A2B — Enforce managed deletion integrity

- Status: TODO
- Phase/Area/Type: P02 / Security / CODE
- Priority/Risk/Size: P1 / HIGH / M
- Owner: AI
- Depends on: P02-SEC-DISC-010C2B2A2A
- Blocks: P02-SEC-QA-010C2B2A2C

## Scope and acceptance

Implement only the approved bounded invariant and migration design from the prerequisite card, with fail-closed topology checks and no changes to production data during development. Preserve native deletion protection, ordinary managed deletion, and unrelated WordPress writes. Test fresh install, upgrade with legacy rows, failed migration, and rollback on disposable WordPress. The implementation scope and affected files must be set by the approved design before coding; stop and decompose again if it exceeds M. Evidence: `docs/evidence/P02-SEC-CODE-010C2B2A2B/`.

## Completion record

- Final status: TODO
