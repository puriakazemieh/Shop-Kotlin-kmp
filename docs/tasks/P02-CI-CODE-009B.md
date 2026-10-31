# P02-CI-CODE-009B — negative PR gate and artifact proof

- Status: TODO
- Phase/Area/Type: P02 / CI / QA
- Priority/Risk/Size: P0 / HIGH / S
- Owner: AI; completion authority: BOTH for CI evidence
- Depends on: P02-CI-CODE-009A
- Blocks: P02-CI-CODE-009
- Requirement: parent P02-CI-CODE-009, active independent-products contract and 009A evidence.

## Scope and acceptance

Create a disposable draft PR or workflow run with a synthetic failure in one critical gate step. Prove the overall PR Gate fails and report artifacts are uploaded when available, then restore the safe workflow before completion. Preserve branch protection and user changes; do not merge the failing fixture or claim a gate successful from YAML inspection alone. Allowed: `.github/workflows/pr-gate.yml`, `docs/**` and an isolated synthetic CI branch; no product code, production data or secrets. Record PR/run/job URL, revision, failing step exit, final check conclusion, artifact list, and cleanup. Evidence: `docs/evidence/P02-CI-CODE-009B/`. Human behavior QA is N/A because this changes only CI.

## Completion record

- Started at:
- Completed at:
- Changed files:
- Commands and exit codes:
- Manual tester/date/result:
- Evidence paths:
- Remaining risks/blockers:
- Final status: TODO (2026-10-31: waits for 009A)
