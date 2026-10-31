# P02-QA-CODE-007C5 — token and session adapter smoke

- Status: DONE
- Phase/Area/Type: P02 / QA / CODE
- Priority/Risk/Size: P0 / HIGH / M
- Owner: AI; completion authority: BOTH for CI evidence
- Depends on: P02-QA-CODE-007C4
- Blocks: P02-QA-CODE-007C6
- Requirement: P02-QA-CODE-007C and inventory 007A; use the active independent-products contract.

## Scope

Run web-access-token, web-session-migration and web-session-wp-adapter real-WP tests with their distinct token/adapter fixtures.

Allowed: `.github/workflows/wordpress-integration.yml`, `wordpress/carmilla-bridge/tests/**`, `docs/**`. Never use production or retained data, relax security assertions, upgrade dependencies, or edit unrelated code. If an additional product fix is needed, stop and split it.

## Acceptance and evidence

All three use per-test random prefix and disposable DB, correct WordPress root and explicit opt-in. Record migration preservation checks and CI evidence. Keep clean activation and the synthetic negative CI gate from 007B intact. Place redacted evidence in `docs/evidence/P02-QA-CODE-007C5/`. If product network or migration behavior needs human acceptance, use AWAITING_MANUAL_QA with exact disposable environment, steps and expected result; never infer approval.

## Completion record

- Started at: 2026-10-31
- Completed at: 2026-10-31
- Changed files: `.github/workflows/wordpress-integration.yml`, this card, queue and evidence.
- Commands and exit codes: three guard-negative Docker probes exit 2; three PHP 8.1 lints exit 0; CI run `41198628730` passed 23/23 jobs.
- Manual tester/date/result: N/A; CI-only fixture change, no product token or migration code change.
- Evidence paths: `docs/evidence/P02-QA-CODE-007C5/checkpoint-20261031.md`; linked CI run.
- Remaining risks/blockers: C6 database-only smokes remain; other CI workflows are not claimed green.
- Final status: DONE
