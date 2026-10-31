# P02-QA-CODE-007C6B — isolated session-store concurrency smoke

- Status: TODO
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

- Started at:
- Completed at:
- Changed files:
- Commands and exit codes:
- Manual tester/date/result:
- Evidence paths:
- Remaining risks/blockers:
- Final status: TODO
