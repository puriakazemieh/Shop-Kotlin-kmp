# P02-CI-CODE-010C2 — Kotlin and WPCS bounded baselines

- Status: TODO
- Phase/Area/Type: P02 / CI / CODE
- Priority/Risk/Size: P1 / HIGH / L (decomposed into ordered C2A, C2B1–B6, C2C, C2D)
- Owner: AI; completion authority: BOTH for CI evidence
- Depends on: P02-CI-CODE-010C2D
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
- Final status: TODO (2026-11-01: size gate found 5,297 WPCS findings across 65 files, including 135 Security/DB findings across 27 files; split before implementation)
