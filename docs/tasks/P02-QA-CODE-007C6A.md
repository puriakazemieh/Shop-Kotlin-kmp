# P02-QA-CODE-007C6A — guarded database-only token and adapter smokes

- Status: DONE
- Phase/Area/Type: P02 / QA / CODE
- Priority/Risk/Size: P0 / HIGH / M
- Owner: AI; completion authority: BOTH for CI evidence
- Depends on: P02-QA-CODE-007C5
- Blocks: P02-QA-CODE-007C6B
- Requirement: P02-QA-CODE-007C6 and 007A inventory; active independent-products contract.

## Scope and acceptance

Run `smoke-session-migration-ddl-fault.php`, `smoke-web-access-token.php` and `smoke-web-session-wp-adapter.php` in separate disposable MariaDB services with their exact fixture users, schemas, loopback ports and random prefixes. DDL fault user has CREATE on only its first synthetic table; repeat must fail closed. Run unqualified guard probes first (expected exit 2), then actual CI and record versions, exit codes, cleanup and URL. Keep prior matrix green.

Allowed: `.github/workflows/wordpress-integration.yml`, `wordpress/carmilla-bridge/tests/**`, `docs/**`. No production data, dependency upgrades or unrelated code. Evidence: `docs/evidence/P02-QA-CODE-007C6A/`. If a product migration/network change needs human acceptance, use AWAITING_MANUAL_QA with exact synthetic steps and expected result.

## Completion record

- Started at: 2026-10-31
- Completed at: 2026-10-31
- Changed files: `.github/workflows/wordpress-integration.yml`, this card, queue and evidence.
- Commands and exit codes: three guard-negative probes exit 2; three PHP 8.1 lints exit 0; final CI run `41199892940` passed 26/26 jobs after a path-only CI failure was fixed.
- Manual tester/date/result: N/A; CI-only fixture change, no product migration/network code change.
- Evidence paths: `docs/evidence/P02-QA-CODE-007C6A/checkpoint-20261031.md`; linked CI runs.
- Remaining risks/blockers: C6B session-store concurrency smoke remains.
- Final status: DONE
