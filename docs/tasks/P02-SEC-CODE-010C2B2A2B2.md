# P02-SEC-CODE-010C2B2A2B2 — Migrate deletion integrity constraints

- Status: TODO
- Phase/Area/Type: P02 / Security / CODE
- Priority/Risk/Size: P1 / HIGH / M
- Owner: AI
- Depends on: P02-SEC-CODE-010C2B2A2B1
- Blocks: P02-SEC-CODE-010C2B2A2B3

## Scope and acceptance

Implement explicit idempotent migration for nullable generated columns and RESTRICT foreign keys on posts/comments for only the verified single-site InnoDB topology. Require preflight with zero nonzero orphans and a documented database backup before real-site migration; no silent repair. Fail closed on unsupported engine/version, privileges, partial DDL or invalid schema. Verify fresh install, upgrade with valid/invalid legacy rows, interrupted migration, rollback and native WordPress writes on disposable copies. Never change production data in this card. Allowed: Bridge integrity helper/migration bootstrap, focused tests and `docs/**`; decompose if this exceeds M. Evidence: `docs/evidence/P02-SEC-CODE-010C2B2A2B2/`.

## Completion record

- Final status: TODO
