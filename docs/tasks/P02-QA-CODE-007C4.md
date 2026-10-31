# P02-QA-CODE-007C4 — shared-root login route smoke

- Status: READY
- Phase/Area/Type: P02 / QA / CODE
- Priority/Risk/Size: P0 / HIGH / M
- Owner: AI; completion authority: BOTH for CI evidence
- Depends on: P02-QA-CODE-007C3
- Blocks: P02-QA-CODE-007C5
- Requirement: P02-QA-CODE-007C and inventory 007A; use the active independent-products contract.

## Scope

Run managed-user-deletion, web-auth-route-activation, web-login and web-refresh-logout under the shared login fixture.

Allowed: `.github/workflows/wordpress-integration.yml`, `wordpress/carmilla-bridge/tests/**`, `docs/**`. Never use production or retained data, relax security assertions, upgrade dependencies, or edit unrelated code. If an additional product fix is needed, stop and split it.

## Acceptance and evidence

Four tests run with explicit login opt-in, isolated DB and path, intercepted outbound effects, exit-code propagation and CI evidence. Keep clean activation and the synthetic negative CI gate from 007B intact. Place redacted evidence in `docs/evidence/P02-QA-CODE-007C4/`. If product network or migration behavior needs human acceptance, use AWAITING_MANUAL_QA with exact disposable environment, steps and expected result; never infer approval.

## Completion record

- Started at:
- Completed at:
- Changed files:
- Commands and exit codes:
- Manual tester/date/result:
- Evidence paths:
- Remaining risks/blockers:
- Final status: READY (2026-10-31: C3 CI matrix passed)
