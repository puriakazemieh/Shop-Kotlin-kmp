# P02-CI-CODE-010C2A — Native detekt baseline

- Status: READY
- Phase/Area/Type: P02 / CI / CODE
- Priority/Risk/Size: P1 / MEDIUM / M
- Owner: AI; completion authority: BOTH for CI evidence
- Depends on: P02-SEC-CODE-010C1A, P02-SEC-CODE-010C1B
- Blocks: P02-CI-CODE-010C2B1

## Scope and acceptance

Using the pinned detekt CLI 1.23.8 and the exact CI input set, generate a native bounded baseline for existing Kotlin findings. Pin the baseline to a source revision and tool digest, run clean and synthetic-new-violation checks, and wire CI so unseen findings fail while reports upload. Do not blanket-disable rules or edit product code. Allowed: `.github/workflows/code-quality.yml`, `tools/**`, `docs/**`. Evidence: `docs/evidence/P02-CI-CODE-010C2A/` with commands, exit codes, baseline counts and CI run. Human QA N/A.

## Completion record

- Started at:
- Completed at:
- Changed files:
- Commands and exit codes:
- Manual tester/date/result: N/A
- Evidence paths:
- Remaining risks/blockers:
- Final status: READY
