# P02-SEC-CODE-010C2B2A2B2A — Implement constrained FK migration core

- Status: DONE
- Phase/Area/Type: P02 / Security / CODE
- Priority/Risk/Size: P1 / HIGH / M
- Owner: AI
- Depends on: P02-SEC-CODE-010C2B2A2B1
- Blocks: P02-SEC-CODE-010C2B2A2B2B

## Scope and acceptance

Implement a callable, explicit migration core for the verified MariaDB single-site InnoDB topology only: zero-orphan preflight, idempotent installation of both generated nullable columns and RESTRICT foreign keys, exact post-DDL verifier, partial-state failure and fail-closed errors. Do not wire activation or change retained/production data. Test fresh and legacy valid/invalid scratch schemas and a simulated interrupted second DDL statement. Allowed: Bridge integrity migration helper, focused disposable DB tests, `docs/**`. Evidence: `docs/evidence/P02-SEC-CODE-010C2B2A2B2A/`.

## Completion record

- Completed at: 2026-11-02
- Changed files: explicit migration core, disposable installed-WordPress test, this card, queue and evidence
- Verification: PHP 8.1 syntax checks passed; installed WordPress 7.1.3/MariaDB 10.6.28 scratch-table test exit 0, 7 PASS, 0 FAIL
- Evidence: `docs/evidence/P02-SEC-CODE-010C2B2A2B2A/verification.md`
- Remaining: operator-only entrypoint/backup prerequisite and migration recovery QA are separate cards; no product schema was changed
- Final status: DONE
