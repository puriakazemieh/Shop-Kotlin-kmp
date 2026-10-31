# P02-CI-CODE-010A — runnable Kotlin lint and detekt jobs

- Status: READY
- Phase/Area/Type: P02 / CI / CODE
- Priority/Risk/Size: P1 / MEDIUM / M
- Owner: AI; completion authority: BOTH for CI evidence
- Depends on: P02-CI-CODE-009
- Blocks: P02-CI-CODE-010B
- Requirement: parent P02-CI-CODE-010, active independent-products contract, Master P02 CI row and RTK.

## Scope and acceptance

Replace the unavailable detekt action and verify ktlint/detekt execute in a disposable PR without hiding failures. Provide any necessary checked-in config under allowed paths, select maintained/pinned tooling without unrelated upgrades, and record how existing findings are separated from new violations. Preserve the WordPress jobs. Allowed: `.github/workflows/code-quality.yml`, `gradle/**`, `build-logic/**`, `tools/**`, `docs/**`. No product source edits. Run JVM baseline, YAML validation and actual CI; record job URLs, versions, exits and redacted findings in `docs/evidence/P02-CI-CODE-010A/`. If unresolved action/version decisions or product code changes are needed, document blocker. Human product QA N/A.

## Completion record

- Started at:
- Completed at:
- Changed files:
- Commands and exit codes:
- Manual tester/date/result:
- Evidence paths:
- Remaining risks/blockers:
- Final status: READY (2026-10-31)
