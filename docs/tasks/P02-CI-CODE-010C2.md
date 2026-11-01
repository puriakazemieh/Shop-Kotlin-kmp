# P02-CI-CODE-010C2 — Kotlin and WPCS bounded baselines

- Status: READY
- Phase/Area/Type: P02 / CI / CODE
- Priority/Risk/Size: P1 / HIGH / M
- Owner: AI; completion authority: BOTH for CI evidence
- Depends on: P02-SEC-CODE-010C1A, P02-SEC-CODE-010C1B
- Blocks: P02-CI-CODE-010C3
- Requirement: 010C1 triage and 010C parent.

## Scope and acceptance

Using fixed tool versions and the approved C1 classification, record detekt and WPCS existing findings in bounded baselines, then make unseen issues fail CI without blanket count thresholds. Prove actual CI execution and reports. Allowed: `.github/workflows/code-quality.yml`, `tools/**`, `docs/**`. No product source edit or waiving security issues. Evidence: `docs/evidence/P02-CI-CODE-010C2/`. Human QA N/A.

## Completion record

- Started at:
- Completed at:
- Changed files:
- Commands and exit codes:
- Manual tester/date/result:
- Evidence paths:
- Remaining risks/blockers:
- Final status: READY (2026-11-01: owner accepted installed synthetic QA of both security prerequisites; CI baseline work is next)
