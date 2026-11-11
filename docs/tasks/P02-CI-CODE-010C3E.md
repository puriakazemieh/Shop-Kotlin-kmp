# P02-CI-CODE-010C3E — Exact Theme Check regression gate

- Status: TODO
- Phase/Area/Type: P02 / CI / CODE+QA
- Priority/Risk/Size: P1 / HIGH / M
- Owner: AI
- Depends on: P02-CORE-CODE-010C3D4 (and preceding fix cards P02-SEC-CODE-010C3D1, P02-THEME-CODE-010C3D2, P02-THEME-CODE-010C3D3)
- Blocks: P02-CI-CODE-010C3

## Scope and acceptance

Implement an exact, pinned Theme Check finding baseline from C3D's approved artifact/channel triage. New REQUIRED, security or other unreviewed findings must fail; tool/version/report drift and missing artifacts fail; no broad severity-count waiver. Prove local positive/negative checks and real isolated-PR Theme Check job/report results, then remove the synthetic probe. Do not claim WordPress.org Theme acceptance for the commercial independent Theme. Allowed: `.github/workflows/code-quality.yml`, `tools/code-quality/**`, `docs/**`, isolated synthetic CI branch. No product source fix. Evidence: `docs/evidence/P02-CI-CODE-010C3E/` with commands, revisions, run/job/artifact IDs, conclusions and rollback. If a required product/package fix is pending, remain BLOCKED.

## Completion record

- Final status: TODO.
