# P02-CI-CODE-010C3 — Plugin and Theme Check bounded baselines

- Status: TODO
- Phase/Area/Type: P02 / CI / CODE
- Priority/Risk/Size: P1 / HIGH / M
- Owner: AI; completion authority: BOTH for CI evidence
- Depends on: P02-CI-CODE-010C2
- Blocks: P02-CI-CODE-010C4
- Requirement: 010C1 security triage and 010C parent.

## Scope and acceptance

Record approved pre-existing Plugin Check and Theme Check debt with exact fixed tool versions and compare later findings so new violations fail. Do not suppress untriaged escaping, SQL, credential or other security findings. If product fixes outside scope are needed, report a blocker and owner decision instead of passing the gate. Allowed: `.github/workflows/code-quality.yml`, `tools/**`, `docs/**`. Real CI report evidence under `docs/evidence/P02-CI-CODE-010C3/`. Human QA N/A.

## Completion record

- Started at:
- Completed at:
- Changed files:
- Commands and exit codes:
- Manual tester/date/result:
- Evidence paths:
- Remaining risks/blockers:
- Final status: TODO (waits for C2)
