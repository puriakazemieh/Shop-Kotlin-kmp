# P04-WP-CONTENT-PERF-CODE-056 — Bound banner listing

- Status: TODO
- Phase/Area/Type: P04 / WordPress Content / CODE
- Priority/Risk/Size: P2 / MEDIUM / M
- Owner: AI
- Depends on: P04-WP-CATALOG-CODE-055
- Blocks: P04-CI-CODE-019

## Scope and acceptance

Replace the unbounded `CB_Catalog_Controller::banners` read/sort with stable bounded pagination or an explicitly capped contract coordinated with consumers. Do not silently hide banners that older clients expect. At 30/3,000 synthetic banners, verify complete traversal without duplicates, active filtering, sort order, SQL plan/scanned rows, latency and memory. Preserve existing response shape through a compatible route/version if needed. Allowed: WordPress catalog code, focused tests, client contract docs. Evidence: `docs/evidence/P04-WP-CONTENT-PERF-CODE-056/`; baseline: `docs/evidence/P02-PERF-REVIEW-010C2B4D/verification.md`.

## Completion record

- Final status: TODO
