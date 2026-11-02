# P02-SEC-CODE-010C2B2A2B2B — Expose controlled integrity migration

- Status: DONE
- Phase/Area/Type: P02 / Security / CODE
- Priority/Risk/Size: P1 / HIGH / S
- Owner: AI
- Depends on: P02-SEC-CODE-010C2B2A2B2A
- Blocks: P02-SEC-QA-010C2B2A2B2C

## Scope and acceptance

Provide an explicit operator-only invocation for the migration core, with a documented verified backup prerequisite and a dry-run/preflight mode. Never run migration implicitly on ordinary plugin activation or request. Refuse missing privilege/topology/backup evidence, report partial DDL without claiming rollback, and leave managed deletion closed until installed state is proven. Test the invocation and denial paths on a disposable installed WordPress. Allowed: Bridge migration entrypoint/bootstrap, focused tests, `docs/**`. Evidence: `docs/evidence/P02-SEC-CODE-010C2B2A2B2B/`.

## Completion record

- Completed at: 2026-11-02
- Changed files: explicit CLI operator command, disposable installed-WordPress test, this card, queue and evidence
- Verification: PHP 8.1 syntax checks passed; installed WordPress 7.1.3/MariaDB 10.6.28 scratch-table command test exit 0, 8 PASS, 0 FAIL; direct CLI preflight exit 0 and missing/mismatched backup denial exit 3
- Evidence: `docs/evidence/P02-SEC-CODE-010C2B2A2B2B/verification.md`
- Remaining: real backup restore proof and migration recovery are tested in the next QA card; no product schema was changed
- Final status: DONE
