# P02-QA-CODE-007C6 — database-only disposable smoke

- Status: DONE
- Phase/Area/Type: P02 / QA / CODE
- Priority/Risk/Size: P0 / HIGH / M
- Owner: AI; completion authority: BOTH for CI evidence
- Depends on: P02-QA-CODE-007C6B (ordered children C6A then C6B; original C5 dependency moves to C6A)
- Blocks: P02-QA-CODE-007C
- Requirement: P02-QA-CODE-007C and inventory 007A; use the active independent-products contract.

## Scope

Run four database-only smokes from 007A inventory: session-migration DDL fault, web-access-token, web-session-store and web-session-wp-adapter. Keep DB and network isolated.

Allowed: `.github/workflows/wordpress-integration.yml`, `wordpress/carmilla-bridge/tests/**`, `docs/**`. Never use production or retained data, relax security assertions, upgrade dependencies, or edit unrelated code. If an additional product fix is needed, stop and split it.

## Acceptance and evidence

Run guard-negative probes first (expected exit 2), then four positive tests against dedicated disposable schemas; record commands, versions, exit codes, cleanup and CI URL. Keep clean activation and the synthetic negative CI gate from 007B intact. Place redacted evidence in `docs/evidence/P02-QA-CODE-007C6/`. If product network or migration behavior needs human acceptance, use AWAITING_MANUAL_QA with exact disposable environment, steps and expected result; never infer approval.

## Completion record

- Started at: 2026-10-31
- Completed at: 2026-10-31
- Changed files: CI workflow and test fixture through children C6A/C6B; this card, `docs/tasks.md`, and parent evidence.
- Commands and exit codes: C6A three guard-negative probes exited 2, PHP lints exited 0, final CI run 41199892940 passed 26/26 jobs; C6B guard-negative exited 2, lint exited 0, final CI run 41215947307 passed 27/27 jobs with session-store 36 checks and 0 failures.
- Manual tester/date/result: Not required for CI fixture-only changes; no product network or migration implementation changed.
- Evidence paths: `docs/evidence/P02-QA-CODE-007C6/verification-20261031.md`, child C6A/C6B checkpoints and linked CI jobs.
- Remaining risks/blockers: Disposable MariaDB and WordPress fixtures do not establish production migration acceptance.
- Final status: DONE (2026-10-31)
