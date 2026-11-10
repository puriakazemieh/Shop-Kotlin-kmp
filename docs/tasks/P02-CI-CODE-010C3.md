# P02-CI-CODE-010C3 — Plugin and Theme Check bounded baselines

- Status: TODO (decomposed; C3A and C3A1 done, C3B1 first READY)
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

- Started at:
- Completed at:
- Changed files:
- Commands and exit codes:
- Manual tester/date/result:
- Evidence paths:
- Remaining risks/blockers:
- Final status: TODO (waits for ordered C3B1 → C3B2 → C3B3 → C3C → C3D → C3E and any genuine fix cards; C3A and C3A1 completed with pinned shipped-file scanner reports and package parity evidence).
