# P02-CI-CODE-010C3B2E — Triage bundled core importer and wallet DB findings

- Status: TODO
- Phase/Area/Type: P02 / CI / DB-REVIEW
- Priority/Risk/Size: P1 / HIGH / S
- Owner: AI
- Depends on: P02-CI-CODE-010C3B2D
- Blocks: P02-CI-CODE-010C3B2F

## Scope and acceptance

Review all pinned shipped-file `WordPress.DB.*` findings in `packages/carmilla-core/inc/Import/Carmilla_Importer.php` and `Payment/WalletService.php` (12 preliminary instances). Distinguish scanner-only bundled-core findings from prior WPCS root, trace SQL and mysqli calls, record exact identity, risk, disposition and fix/test task. No product fix. Evidence: `docs/evidence/P02-CI-CODE-010C3B2E/`. Allowed: read-only product source, `tools/code-quality/**`, `docs/**`.

## Completion record

- Final status: TODO.
