# P02-QA-CODE-007C2 — installed-root login smoke

- Status: READY
- Phase/Area/Type: P02 / QA / CODE
- Priority/Risk/Size: P0 / HIGH / M
- Owner: AI; completion authority: BOTH for CI evidence
- Depends on: P02-QA-CODE-007C1
- Blocks: P02-QA-CODE-007C3
- Requirement: P02-QA-CODE-007C and inventory 007A; use the active independent-products contract.

## Scope

Run the four tests requiring `/var/www/html/wp-load.php`: auth error contract, synthetic migration, local OTP sink and registration. Isolate each test's WP tables and synthetic identities.

Allowed: `.github/workflows/wordpress-integration.yml`, `wordpress/carmilla-bridge/tests/**`, `docs/**`. Never use production or retained data, relax security assertions, upgrade dependencies, or edit unrelated code. If an additional product fix is needed, stop and split it.

## Acceptance and evidence

All four tests run with per-test fresh database/prefix and `CARMILLA_LOGIN_DISPOSABLE=yes`; mail/OTP stays local. Record redacted logs, CI URL and cleanup. Keep clean activation and the synthetic negative CI gate from 007B intact. Place redacted evidence in `docs/evidence/P02-QA-CODE-007C2/`. If product network or migration behavior needs human acceptance, use AWAITING_MANUAL_QA with exact disposable environment, steps and expected result; never infer approval.

## Completion record

- Started at:
- Completed at:
- Changed files:
- Commands and exit codes:
- Manual tester/date/result:
- Evidence paths:
- Remaining risks/blockers:
- Final status: READY (2026-10-31: C1 CI smoke matrix passed)
