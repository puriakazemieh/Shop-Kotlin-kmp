# P02-CI-CODE-010A — runnable Kotlin lint and detekt jobs

- Status: DONE
- Phase/Area/Type: P02 / CI / CODE
- Priority/Risk/Size: P1 / MEDIUM / M
- Owner: AI; completion authority: BOTH for CI evidence
- Depends on: P02-CI-CODE-009
- Blocks: P02-CI-CODE-010B
- Requirement: parent P02-CI-CODE-010, active independent-products contract, Master P02 CI row and RTK.

## Scope and acceptance

Replace the unavailable detekt action and verify ktlint/detekt execute in a disposable PR without hiding failures. Provide any necessary checked-in config under allowed paths, select maintained/pinned tooling without unrelated upgrades, and record how existing findings are separated from new violations. Preserve the WordPress jobs. Allowed: `.github/workflows/code-quality.yml`, `gradle/**`, `build-logic/**`, `tools/**`, `docs/**`. No product source edits. Run JVM baseline, YAML validation and actual CI; record job URLs, versions, exits and redacted findings in `docs/evidence/P02-CI-CODE-010A/`. If unresolved action/version decisions or product code changes are needed, document blocker. Human product QA N/A.

## Completion record

- Started at: 2026-10-31
- Completed at: 2026-10-31
- Changed files: `.github/workflows/code-quality.yml`, this card, `docs/tasks.md`, and evidence checkpoint.
- Commands and exit codes: JVM baseline exit 0; YAML parse and diff check exit 0; actual Lint & Code Quality runs 41220096371 and 41220523149 executed detekt CLI. In the final run the archive SHA-256 check passed, detekt returned exit 2 for 2,621 existing weighted issues and uploaded SARIF; ktlint concluded success.
- Manual tester/date/result: N/A; CI-only change.
- Evidence paths: `docs/evidence/P02-CI-CODE-010A/checkpoint-20261031.md`, [final CI run](https://github.com/puriakazemieh/Shop-Kotlin-kmp/actions/runs/41220523149).
- Remaining risks/blockers: Existing detekt findings remain unsuppressed and make the independent quality workflow red. WPCS action and Plugin Check findings are for 010B/010C. No clean detekt result is claimed.
- Final status: DONE (2026-10-31: tool executes fail-closed; baseline policy remains 010C)
