# P02-SEC-CODE-010C2B2A2B2B — Expose controlled integrity migration

- Status: TODO
- Phase/Area/Type: P02 / Security / CODE
- Priority/Risk/Size: P1 / HIGH / S
- Owner: AI
- Depends on: P02-SEC-CODE-010C2B2A2B2A
- Blocks: P02-SEC-QA-010C2B2A2B2C

## Scope and acceptance

Provide an explicit operator-only invocation for the migration core, with a documented verified backup prerequisite and a dry-run/preflight mode. Never run migration implicitly on ordinary plugin activation or request. Refuse missing privilege/topology/backup evidence, report partial DDL without claiming rollback, and leave managed deletion closed until installed state is proven. Test the invocation and denial paths on a disposable installed WordPress. Allowed: Bridge migration entrypoint/bootstrap, focused tests, `docs/**`. Evidence: `docs/evidence/P02-SEC-CODE-010C2B2A2B2B/`.

## Completion record

- Final status: TODO
