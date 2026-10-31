# P02-CI-CODE-010C — baseline debt and new violation regression gate

- Status: TODO
- Phase/Area/Type: P02 / CI / CODE
- Priority/Risk/Size: P1 / HIGH / L (decomposed into ordered C1–C4)
- Owner: AI; completion authority: BOTH for CI evidence
- Depends on: P02-CI-CODE-010C4 (ordered children C1–C4; original 010B prerequisite moves to C1)
- Blocks: P02-CI-CODE-010
- Requirement: parent P02-CI-CODE-010 and active independent-products contract.

## Scope and acceptance

Record pre-existing Kotlin/PHP/Plugin Check/Theme Check findings as bounded baseline debt, then make new violations fail CI. Prove one positive PR and one synthetic new-violation negative PR with actual conclusions, reports and cleanup. Do not suppress stop-ship security findings, edit product source to make the baseline green, or treat a failed runner setup as a passed check. Allowed: `.github/workflows/code-quality.yml`, `tools/**`, `docs/**`, isolated synthetic CI branch. Record exact baseline revision/tool versions, diff method, job/run URLs and evidence under `docs/evidence/P02-CI-CODE-010C/`. Human product QA N/A.

## Completion record

- Started at:
- Completed at:
- Changed files:
- Commands and exit codes:
- Manual tester/date/result:
- Evidence paths:
- Remaining risks/blockers:
- Final status: TODO (2026-10-31: size gate split into C1–C4; C1 READY)
