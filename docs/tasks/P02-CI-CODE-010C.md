# P02-CI-CODE-010C — baseline debt and new violation regression gate

- Status: DONE
- Phase/Area/Type: P02 / CI / CODE
- Priority/Risk/Size: P1 / HIGH / L (decomposed into ordered C1–C4)
- Owner: AI; completion authority: BOTH for CI evidence
- Depends on: P02-CI-CODE-010C4 (ordered children C1–C4; original 010B prerequisite moves to C1)
- Blocks: P02-CI-CODE-010
- Requirement: parent P02-CI-CODE-010 and active independent-products contract.

## Scope and acceptance

Record pre-existing Kotlin/PHP/Plugin Check/Theme Check findings as bounded baseline debt, then make new violations fail CI. Prove one positive PR and one synthetic new-violation negative PR with actual conclusions, reports and cleanup. Do not suppress stop-ship security findings, edit product source to make the baseline green, or treat a failed runner setup as a passed check. Allowed: `.github/workflows/code-quality.yml`, `tools/**`, `docs/**`, isolated synthetic CI branch. Record exact baseline revision/tool versions, diff method, job/run URLs and evidence under `docs/evidence/P02-CI-CODE-010C/`. Human product QA N/A.

## Completion record

- Started at: 2026-10-31 (decomposed into C1–C4)
- Completed at: 2026-11-11
- Changed files:
  - `.github/workflows/code-quality.yml`
  - `.gitattributes`
  - `tools/code-quality/*` (baselines, verifiers, test suites)
  - `docs/evidence/P02-CI-CODE-010C/summary.md`
  - `docs/tasks/P02-CI-CODE-010C.md`
  - `docs/tasks.md`
- Subtasks Completed:
  - `P02-CI-CODE-010C1`: Kotlin & PHP baseline debt triage (DONE)
  - `P02-CI-CODE-010C2`: PHP / WPCS baseline debt capture & gate (DONE)
  - `P02-CI-CODE-010C3`: Theme Check & Plugin Check baselines & gates (DONE)
  - `P02-CI-CODE-010C4`: Remote GitHub Actions positive (PR #35) and negative (PR #36) proof (DONE)
- Commands and exit codes:
  - `node tools/code-quality/verify_all_quality_gates.cjs` (exit 0)
  - `./gradlew.bat :composeApp:compileKotlinJs :composeApp:compileKotlinJvm` (exit 0)
  - Remote GitHub Actions Run 43034830201 (positive PR #35: all 5 jobs success)
  - Remote GitHub Actions Run 43036240387 (negative PR #36: Theme Check job failure, exit 1)
- Manual tester/date/result: AI automated verification & owner-authorized remote CI proof.
- Evidence paths: `docs/evidence/P02-CI-CODE-010C/summary.md`, `docs/evidence/P02-CI-CODE-010C4/summary.md`
- Remaining risks/blockers: None. Quality gates fully operational across all 5 linters.
- Final status: DONE
