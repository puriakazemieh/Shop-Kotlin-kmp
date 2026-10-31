# P02-QA-CODE-007C4 checkpoint — 2026-10-31

Baseline `e2c426ac`. Four new PHP 8.1 / WP 6.4.3 / Woo 8.5.2 matrix cells each run one shared-root login/route smoke: managed-user-deletion, web-auth-route-activation, web-login or web-refresh-logout. Each cell uses a fresh `carmilla_login_qa` MariaDB service, `synthetic_qa` fixture-only credentials, an independently generated `qa_<12 hex>_` table prefix, `/usr/src/wordpress` and `/work`/`/bridge` Bridge paths. The shared fixture intercepts `wp_mail`; no customer endpoint or retained database is used. The existing 16 C1–C3 cells and synthetic negative gate remain in place.

All four scripts exited 2 without the disposable flag in network-disabled Docker PHP 8.1. Docker PHP 8.1 lint of all four scripts exited 0; `js-yaml` parsed 20 matrix cells (exit 0). GitNexus impact could not resolve the workflow YAML path (`UNKNOWN`), so the explicit dispatch and fixture path were text-reviewed. Actual CI has not run for this card yet; no login/route PASS is claimed.

## Actual CI result

The local workflow commit `3b1ddc4d` reached remote `develop`. [WordPress Integration push run 41198134020](https://github.com/puriakazemieh/Shop-Kotlin-kmp/actions/runs/41198134020) executed on that exact SHA and passed all 20 jobs, including the four new shared-root login/route tests. Each job used a separate disposable MariaDB service and random prefix; GitHub Actions cleanup stopped the service. The earlier 16 tests and synthetic-negative gate configuration remained intact. No product auth/network code changed here, so no human product-behavior QA action is required. Other workflows are not inferred green from this run.
