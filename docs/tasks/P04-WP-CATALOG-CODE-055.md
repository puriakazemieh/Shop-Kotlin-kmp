# P04-WP-CATALOG-CODE-055 — Apply catalog price range correctly

- Status: TODO
- Phase/Area/Type: P04 / WordPress Catalog / CODE
- Priority/Risk/Size: P1 / HIGH / M
- Owner: AI
- Depends on: P04-WP-ADMIN-CODE-054
- Blocks: P04-WP-CONTENT-PERF-CODE-056

## Scope and acceptance

Fix `CB_Catalog_Controller::products` so `minPrice`/`maxPrice` actually restrict WooCommerce products using a supported WooCommerce query/data-store path; the current `meta_query` argument is ignored in the measured environment. Preserve pagination, sort, stock/category filters and response shape. Characterize 30/300 synthetic WC products with prices inside/outside bounds, including inclusive edges and empty result. Record generated SQL, EXPLAIN/ANALYZE, output and cross-filter behavior. Do not directly SQL-write WooCommerce orders. Allowed: catalog controller, focused tests, docs. Evidence: `docs/evidence/P04-WP-CATALOG-CODE-055/`; baseline: `docs/evidence/P02-PERF-REVIEW-010C2B4D/verification.md`.

## Completion record

- Final status: TODO
