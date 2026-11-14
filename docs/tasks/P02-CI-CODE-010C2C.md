# P02-CI-CODE-010C2C — Bounded WPCS style baseline

- Status: DONE
- Phase/Area/Type: P02 / CI / CODE
- Priority/Risk/Size: P1 / HIGH / M
- Owner: AI; completion authority: BOTH for CI evidence
- Depends on: P02-CI-CODE-010C2B6 and any genuine Security/DB fix cards produced by B1–B6
- Blocks: P02-CI-CODE-010C2D

## Scope and acceptance

Build an exact-finding baseline for pre-existing nonsecurity WPCS style debt, pinned to source revision and WPCS/PHPCS versions. New findings must fail; security/DB findings are not waived and genuine issues must be fixed or explicitly blocked. Test clean and synthetic new violation. Allowed: `.github/workflows/code-quality.yml`, `tools/**`, `docs/**`. Evidence: `docs/evidence/P02-CI-CODE-010C2C/`. Human QA N/A.

## Completion record

- Final status: DONE (2026-11-09); exact style baseline implemented and clean/new-violation behavior verified. Strict Security/DB gate remains red as documented.
- Evidence: `docs/evidence/P02-CI-CODE-010C2C/verification.md`.
