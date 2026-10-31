# P02-QA-CODE-007C6B — isolated session-store concurrency smoke

- Status: DONE
- Phase/Area/Type: P02 / QA / CODE
- Priority/Risk/Size: P0 / HIGH / M
- Owner: AI; completion authority: BOTH for CI evidence
- Depends on: P02-QA-CODE-007C6A
- Blocks: P02-QA-CODE-007C6
- Requirement: P02-QA-CODE-007C6 and 007A inventory; active independent-products contract.

## Scope and acceptance

Run `smoke-web-session-store.php` against a dedicated loopback-only `carmilla_session_qa` database. The current test hardcodes root with an empty password; characterize and replace that test-only connection with an explicit disposable fixture credential or prove an equally isolated setup without relaxing assertions. Run guard-negative probe (expected exit 2), then positive concurrency/commit-failure CI test with timeout, random prefix, redacted logs and service cleanup. Preserve other matrix cells.

Allowed: `.github/workflows/wordpress-integration.yml`, `wordpress/carmilla-bridge/tests/**`, `docs/**`. No production data, dependency upgrades or unrelated code. Evidence: `docs/evidence/P02-QA-CODE-007C6B/`. If product migration/network behavior changes, use AWAITING_MANUAL_QA with exact synthetic steps and expected result.

## Completion record

- Started at: 2026-10-31
- Completed at: 2026-10-31
- Changed files: `.github/workflows/wordpress-integration.yml`, `wordpress/carmilla-bridge/tests/smoke-web-session-store.php`, this card, `docs/tasks.md`, and the evidence checkpoint.
- Commands and exit codes: PHP 8.1 lint exit 0; local guard-negative with valid port and prefix but no disposable opt-in exit 2; YAML parse 27 cells exit 0; GitHub WordPress Integration run 41215947307 on `fb4274e` succeeded with 27/27 jobs. The `db-session-store` job 124580378172 ran 36 checks with 0 failures.
- Manual tester/date/result: Not required; only the disposable test harness and CI workflow changed, with no product migration or network behavior change.
- Evidence paths: `docs/evidence/P02-QA-CODE-007C6B/checkpoint-20261031.md`; [CI run](https://github.com/puriakazemieh/Shop-Kotlin-kmp/actions/runs/41215947307).
- Remaining risks/blockers: CI validates a disposable MariaDB fixture, not a production database.
- Final status: DONE (2026-10-31)
