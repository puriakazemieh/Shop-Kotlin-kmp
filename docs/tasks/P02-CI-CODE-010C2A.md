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

- Started at: 2026-11-01
- Completed at:
- Changed files: `.github/workflows/code-quality.yml`, `tools/code-quality/detekt-baseline.xml`, `docs/evidence/P02-CI-CODE-010C2A/detekt-baseline-20261101.md`, this card
- Commands and exit codes: pinned detekt baseline generation 0; positive run 0; synthetic negative run 1 (expected); see evidence.
- Manual tester/date/result: N/A
- Evidence paths: `docs/evidence/P02-CI-CODE-010C2A/detekt-baseline-20261101.md`
- Remaining risks/blockers: GitHub Actions run on the committed revision still required for CI evidence.
- Final status: READY
