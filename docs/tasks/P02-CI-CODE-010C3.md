# P02-CI-CODE-010C3 — Plugin and Theme Check bounded baselines

- Status: DONE
- Phase/Area/Type: P02 / CI / CODE
- Priority/Risk/Size: P1 / HIGH / L (real 3,381-finding Plugin Check plus Theme Check; decomposed into ordered M-or-smaller C3A–C3E with package correction C3A1)
- Owner: AI; completion authority: BOTH for CI evidence
- Depends on: P02-CI-CODE-010C3E
- Blocks: P02-CI-CODE-010C4
- Requirement: 010C1 security triage and 010C parent.

## Scope and acceptance

Record approved pre-existing Plugin Check and Theme Check debt with exact fixed tool versions and compare later findings so new violations fail. Do not suppress untriaged escaping, SQL, credential or other security findings. If product fixes outside scope are needed, report a blocker and owner decision instead of passing the gate. Allowed: `.github/workflows/code-quality.yml`, `tools/**`, `docs/**`. Real CI report evidence under `docs/evidence/P02-CI-CODE-010C3/`. Human QA N/A.

## Completion record

- Size gate 2026-11-09: real Plugin Check report has 3,381 file findings (3,115 under tests; 266 outside), including untriaged security and DB codes. Theme Check also has REQUIRED findings and actual Theme package scope ambiguity. Evidence and ordered child scopes: `docs/evidence/P02-CI-CODE-010C3/size-gate-20261109.md`. No baseline or product code was changed in this decomposition turn.

- Completed on: 2026-11-11
- Changed files: `.github/workflows/code-quality.yml`, `tools/code-quality/**`, `docs/**`
- Verification commands & exit codes:
  - `node tools/code-quality/test_plugin_check_baseline.cjs` (exit code: 0)
  - `node tools/code-quality/test_theme_check_baseline.cjs` (exit code: 0)
  - Full suite of 157 automated PHP, theme, core, and baseline checks (all passed, exit code: 0)
- Evidence: `docs/evidence/P02-CI-CODE-010C3/summary.md`
- Remaining risks/blockers: None for baseline enforcement. Follow-up PR proof executes in `P02-CI-CODE-010C4`.
- Final status: DONE (all decomposed child tasks C3A, C3A1, C3B1-B3, C3C, C3D, 010C3D1-D4, and C3E completed with exact cryptographic baselines in CI).
