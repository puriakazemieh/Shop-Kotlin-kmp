# P02-CI-CODE-009A — fail-closed PR gate wiring and reports

- Status: READY
- Phase/Area/Type: P02 / CI / CODE
- Priority/Risk/Size: P0 / HIGH / M
- Owner: AI; completion authority: BOTH for CI evidence
- Depends on: P02-QA-CODE-008
- Blocks: P02-CI-CODE-009B
- Requirement: parent P02-CI-CODE-009, active independent-products contract, Master P02 CI row and RTK.

## Scope and acceptance

Ensure the PR Gate makes lint, unit, package and the existing WordPress integration suite mandatory in the PR check result. Keep the standalone WordPress workflow's dispatch/push coverage, avoid a duplicate PR matrix, and upload available reports on both success and failure without masking failures. Use the existing Gradle commands and existing 27-cell disposable WP suite; no new product test logic or dependency upgrade. Allowed: `.github/workflows/pr-gate.yml`, `.github/workflows/wordpress-integration.yml`, `docs/**`. Verify YAML/schema, baseline JVM compile, actual positive PR run and artifact availability/absence honestly. Evidence: `docs/evidence/P02-CI-CODE-009A/`. No product UI/network/migration behavior changes, so human behavior QA is N/A.

## Completion record

- Started at:
- Completed at:
- Changed files:
- Commands and exit codes:
- Manual tester/date/result:
- Evidence paths:
- Remaining risks/blockers:
- Final status: READY (2026-10-31)
