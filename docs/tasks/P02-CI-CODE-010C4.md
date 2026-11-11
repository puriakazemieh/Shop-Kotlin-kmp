# P02-CI-CODE-010C4 — quality gate positive and negative PR proof

- Status: READY
- Phase/Area/Type: P02 / CI / QA
- Priority/Risk/Size: P1 / HIGH / M
- Owner: AI; completion authority: BOTH for CI evidence
- Depends on: P02-CI-CODE-010C3
- Blocks: P02-CI-CODE-010C
- Requirement: 010C2/C3 bounded baselines and 010C parent.

## Scope and acceptance

Run one positive disposable PR with all quality jobs enforcing the bounded baseline, and one isolated synthetic new-violation PR that fails the quality check. Record run/job/revision/artifact evidence, restore and close the negative probe unmerged. Preserve unrelated workflows. Allowed: `.github/workflows/code-quality.yml`, `tools/**`, `docs/**`, isolated synthetic branch. Human QA N/A. Evidence: `docs/evidence/P02-CI-CODE-010C4/`.

## Completion record

- Started at:
- Completed at:
- Changed files:
- Commands and exit codes:
- Manual tester/date/result:
- Evidence paths:
- Remaining risks/blockers:
- Final status: TODO (waits for C3)
