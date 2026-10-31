# P02-QA-CODE-007C1 — WP CLI commerce smoke

- Status: DONE
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

- Started at: 2026-10-31
- Completed at: 2026-10-31
- Changed files: `.github/workflows/wordpress-integration.yml`, this card, queue and evidence.
- Commands and exit codes: YAML parse 0; two PHP lints 0; unqualified Woo guard exit 2 as expected; CI run `41194859046` six jobs success.
- Manual tester/date/result: N/A; CI orchestration only, no product network or migration code change.
- Evidence paths: `docs/evidence/P02-QA-CODE-007C1/checkpoint-20261031.md`; linked CI run.
- Remaining risks/blockers: Remaining 17 real-WP smokes and four DB-only smokes belong to C2–C6.
- Final status: DONE
