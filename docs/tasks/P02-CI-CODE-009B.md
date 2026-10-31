# P02-CI-CODE-009B — negative PR gate and artifact proof

- Status: DONE
- Phase/Area/Type: P02 / CI / QA
- Priority/Risk/Size: P0 / HIGH / S
- Owner: AI; completion authority: BOTH for CI evidence
- Depends on: P02-CI-CODE-009A
- Blocks: P02-CI-CODE-009
- Requirement: parent P02-CI-CODE-009, active independent-products contract and 009A evidence.

## Scope and acceptance

Create a disposable draft PR or workflow run with a synthetic failure in one critical gate step. Prove the overall PR Gate fails and report artifacts are uploaded when available, then restore the safe workflow before completion. Preserve branch protection and user changes; do not merge the failing fixture or claim a gate successful from YAML inspection alone. Allowed: `.github/workflows/pr-gate.yml`, `docs/**` and an isolated synthetic CI branch; no product code, production data or secrets. Record PR/run/job URL, revision, failing step exit, final check conclusion, artifact list, and cleanup. Evidence: `docs/evidence/P02-CI-CODE-009B/`. Human behavior QA is N/A because this changes only CI.

## Completion record

- Started at: 2026-10-31
- Completed at: 2026-10-31
- Changed files: Isolated draft PR #31 contained a temporary synthetic failure and a restoration commit; local changes are this card, `docs/tasks.md`, and evidence only.
- Commands and exit codes: Temporary PR Gate run 41218333725 at `42385cde` concluded failure; synthetic probe exited 17 after successful lint/unit/package. Validate and final `PR Gate Result` jobs failed. Test and lint report artifact uploads both succeeded. Safe workflow restored at `011b86dc` and PR #31 closed unmerged.
- Manual tester/date/result: N/A; CI-only probe.
- Evidence paths: `docs/evidence/P02-CI-CODE-009B/verification-20261031.md`; [negative run](https://github.com/puriakazemieh/Shop-Kotlin-kmp/actions/runs/41218333725).
- Remaining risks/blockers: Branch protection required-check configuration has not been audited; independent Lint & Code Quality workflow remains red.
- Final status: DONE (2026-10-31)
