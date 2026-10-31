# P02-QA-CODE-007A — WordPress smoke inventory and fixture contract

- Status: DONE
- Phase/Area/Type: P02 / QA / CODE
- Priority/Risk/Size: P0 / MEDIUM / S
- Owner: AI; completion authority: AI
- Depends on: P02-QA-CODE-006
- Blocks: P02-QA-CODE-007B
- Requirement: parent P02-QA-CODE-007 and `docs/evidence/P02-QA-CODE-007/scope-review-20261028.md`.

## Goal and scope

Inventory every active Bridge `smoke*.php` test, classify hermetic versus real-WordPress, identify PHP/WP/Woo/DB prerequisites, disposable flags, path assumptions and unsafe side effects. Define a reproducible synthetic fixture and cleanup contract for the matrix. Do not modify product behavior or CI in this card.

Allowed: `wordpress/carmilla-bridge/tests/**`, read-only inspection of `wordpress/carmilla-theme/**`, `docs/**`. Forbidden: `.github/**`, customer/production data, destructive operations against non-disposable databases, dependency upgrades and unrelated refactors.

## Acceptance and evidence

- Complete inventory with each test's exact command, expected exit, fixtures and reason for classification.
- Reproducible disposable database/prefix and reset contract; no customer identifiers or secrets in evidence.
- Run safe focused baseline smoke commands with Docker/PHP where available; record command, versions, exit codes and any blocker. A static inventory alone does not prove the parent matrix.
- Evidence in `docs/evidence/P02-QA-CODE-007A/`; Master parent checkbox remains open.

Manual QA: N/A for documentation-only inventory. If a network or migration behavior change becomes necessary, stop and create a scoped child; do not mark it done without human verification.

## Completion record

- Started at: 2026-10-31
- Completed at: 2026-10-31
- Changed files: `docs/tasks.md`, `docs/tasks/P02-QA-CODE-007.md`, `docs/tasks/P02-QA-CODE-007A.md`, `docs/tasks/P02-QA-CODE-007B.md`, `docs/tasks/P02-QA-CODE-007C.md`, `docs/evidence/P02-QA-CODE-007A/inventory-20261031.md`
- Commands and exit codes: Docker 29.2.1 available; PHP 8.1 lint 37/37 exit 0; hermetic smoke 14/14 exit 0; four unqualified DB/WP probes exit 2 as expected. Exact commands and classifications in evidence.
- Manual tester/date/result: N/A, documentation and hermetic smoke only.
- Evidence paths: `docs/evidence/P02-QA-CODE-007A/inventory-20261031.md`
- Remaining risks/blockers: Actual WP/Woo matrix and DB-backed tests are assigned to 007B/007C; no parent CI PASS claimed.
- Final status: DONE
