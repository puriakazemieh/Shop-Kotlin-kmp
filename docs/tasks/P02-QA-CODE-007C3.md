# P02-QA-CODE-007C3 — shared-root identity and deletion smoke

- Status: DONE
- Phase/Area/Type: P02 / QA / CODE
- Priority/Risk/Size: P0 / HIGH / M
- Owner: AI; completion authority: BOTH for CI evidence
- Depends on: P02-QA-CODE-007C2
- Blocks: P02-QA-CODE-007C4
- Requirement: P02-QA-CODE-007C and inventory 007A; use the active independent-products contract.

## Scope

Run the six `/usr/src/wordpress/` identity and incident tests: notice, observer, runtime, store, exact audit and supported writer.

Allowed: `.github/workflows/wordpress-integration.yml`, `wordpress/carmilla-bridge/tests/**`, `docs/**`. Never use production or retained data, relax security assertions, upgrade dependencies, or edit unrelated code. If an additional product fix is needed, stop and split it.

## Acceptance and evidence

Each test uses its declared fixture and random prefix, disposable database and captured exit code; fault triggers and synthetic users are discarded. Record matrix rationale and CI URL. Keep clean activation and the synthetic negative CI gate from 007B intact. Place redacted evidence in `docs/evidence/P02-QA-CODE-007C3/`. If product network or migration behavior needs human acceptance, use AWAITING_MANUAL_QA with exact disposable environment, steps and expected result; never infer approval.

## Completion record

- Started at: 2026-10-31
- Completed at: 2026-10-31
- Changed files: `.github/workflows/wordpress-integration.yml`, this card, queue and evidence.
- Commands and exit codes: six unqualified Docker guard probes each exit 2; six PHP 8.1 lints exit 0; final CI run `41197637470` passed 16/16 jobs.
- Manual tester/date/result: N/A; CI-only fixture change, no product migration or network code change.
- Evidence paths: `docs/evidence/P02-QA-CODE-007C3/checkpoint-20261031.md`; linked CI runs.
- Remaining risks/blockers: C4–C6 smoke groups remain; separate lint workflow is not claimed green.
- Final status: DONE
