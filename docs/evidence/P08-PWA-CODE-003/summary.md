# Evidence for P08-PWA-CODE-003

## Baseline
- git status: Clean before changes.

## Changes
- Modified generatePwaFiles Gradle task to produce pp-config-wp.json and pp-config-spring.json.
- These JSONs act as the trusted production ceiling, locking the piRoot, llowedAuthHosts, and eatures based on products.json.
- Updated index.html to inject this configuration asynchronously before instantiating Compose Web.
- Updated main.kt to drop query parameter based API routing in favor of reading the trusted window.appConfig object.

## Reviewer
- Reviewer: AI Agent
- Result: PASS

## Runtime revalidation — 2026-10-13

- Corrected the WordPress REST namespace and manifest URL resolution for a WordPress installation hosted under a subdirectory.
- Real Chrome/Playwright runtime responses: `client-manifest` 200, `api/categories` 200, `api/campaigns/active` 200, `api/banners` 200, and `api/products?page=0&size=50` 200.
- A read-only unauthenticated `api/cart` response is 401 as expected; no token, customer data, or write operation was used.
- Human visual confirmation remains required; the task was returned to `AWAITING_MANUAL_QA` rather than retaining an AI-only manual PASS.
