# P02-QA-CODE-007C1 — WP CLI commerce smoke

- Status: READY
- Phase/Area/Type: P02 / QA / CODE
- Priority/Risk/Size: P0 / HIGH / M
- Owner: AI; completion authority: BOTH for CI evidence
- Depends on: P02-QA-CODE-007B
- Blocks: P02-QA-CODE-007C2
- Requirement: P02-QA-CODE-007C and inventory 007A; use the active independent-products contract.

## Scope

Run `smoke-p07-wordpress.php` and `smoke-woo-reference.php` in fresh WP/Woo cells. Woo reference is limited to WP 6.4.3/Woo 8.5.2 because the test asserts that exact Woo version.

Allowed: `.github/workflows/wordpress-integration.yml`, `wordpress/carmilla-bridge/tests/**`, `docs/**`. Never use production or retained data, relax security assertions, upgrade dependencies, or edit unrelated code. If an additional product fix is needed, stop and split it.

## Acceptance and evidence

Both tests run only on disposable installations with `qa_<12 hex>_` prefixes, opt-in for Woo, exit-code propagation and cleanup. Record PHP/WP/Woo versions, commands and CI URL. Keep clean activation and the synthetic negative CI gate from 007B intact. Place redacted evidence in `docs/evidence/P02-QA-CODE-007C1/`. If product network or migration behavior needs human acceptance, use AWAITING_MANUAL_QA with exact disposable environment, steps and expected result; never infer approval.

## Completion record

- Started at:
- Completed at:
- Changed files:
- Commands and exit codes:
- Manual tester/date/result:
- Evidence paths:
- Remaining risks/blockers:
- Final status: READY
