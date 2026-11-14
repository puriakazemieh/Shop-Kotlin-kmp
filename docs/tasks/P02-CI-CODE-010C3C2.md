# P02-CI-CODE-010C3C2 — Wire exact Plugin Check CI gate

- Status: DONE
- Phase/Area/Type: P02 / CI / CODE
- Priority/Risk/Size: P1 / HIGH / M
- Owner: AI
- Depends on: P02-CI-CODE-010C3C1
- Blocks: P02-CI-CODE-010C3C3

## Scope and acceptance

Run C3C1's exact checker on the ZIP-shaped Plugin Check report in `.github/workflows/code-quality.yml`, preserving the pinned action/scanner revisions. Upload full report and checker diagnostics even on failure, and ensure oversized PR comment output cannot hide findings or create false green. Validate YAML and script behavior locally, then inspect a relevant CI run. No product source fix. Evidence: `docs/evidence/P02-CI-CODE-010C3C2/`.

## Completion record

- Final status: DONE. Pinned action plus independent exact-baseline gate, full report/diagnostic artifacts, local actionlint and checker scenarios, actual disposable WordPress scan, and relevant prior CI run recorded in `docs/evidence/P02-CI-CODE-010C3C2/summary.md`. Current package correctly fails the gate; the changed workflow's remote PR run belongs to C3C3.
