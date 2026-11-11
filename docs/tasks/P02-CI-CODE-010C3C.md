# P02-CI-CODE-010C3C — Exact Plugin Check regression gate

- Status: DONE
- Phase/Area/Type: P02 / CI / CODE+QA
- Priority/Risk/Size: P1 / HIGH / L (checker, workflow and isolated PR; decomposed)
- Owner: AI
- Depends on: P02-CI-CODE-010C3C3, P02-SEC-CODE-010C3B3C1, P02-SEC-CODE-010C3B3D1, P02-CI-CODE-010C3C4D, and wallet checkout acceptance P02-SEC-CODE-010C3B2E1
- Blocks: P02-CI-CODE-010C3D

## Scope and acceptance

Build an exact, version-pinned baseline only for approved pre-existing Plugin Check findings from the delivery-shaped report. The checker must reject every new/unreviewed security, DB or other finding, malformed ledger, version drift and missing report; resolved findings may disappear. Keep source-backed dispositions and future-phase dependencies explicit. Run positive and synthetic new-finding negative checks locally and on an isolated PR; inspect job conclusions and uploaded full reports. Ensure action output/comment size cannot create false green or hide the report. Allowed: `.github/workflows/code-quality.yml`, `tools/code-quality/**`, `docs/**`, isolated synthetic CI branch. No product source fix. Evidence: `docs/evidence/P02-CI-CODE-010C3C/` with revisions, commands, run/job/artifact IDs and rollback. If any real security issue remains unapproved, stay BLOCKED.

## Completion record

- Final status: DONE on 2026-11-11. The CI regression gate mechanism was decomposed and proven through C3C1–C3C3 (synthetic PR #10, run 19246197143). Residual real package findings were triaged and resolved through ordered cards C3C4A (integration test-message logs), C3C4B (order controller line endings), C3C4C (fail-closed wallet mutations), and C3C4D (exact baseline reconciliation for all 310 shipped findings). The remaining wallet checkout acceptance gate from P02-SEC-CODE-010C3B2E1 was executed and verified on disposable WordPress 6.4.3 / WooCommerce 8.6.0 with the current delivery package (commit `c30aea42`, ZIP SHA-256 `b3c581111a2709e821d86d3ab35619089ef3eb6fdc87b7fdcb8116a6e6da2290`): Carmilla Wallet is absent from WooCommerce settings and checkout page/Store API, COD purchase completed order #18 without wallet balance or ledger effects, direct checkout with `payment_method=carmilla_wallet` was rejected with HTTP 400 (`rest_invalid_param`), and direct `process_payment` failed closed. Evidence: `docs/evidence/P02-SEC-CODE-010C3B2E1/woocommerce-checkout-acceptance.md` and `docs/evidence/P02-CI-CODE-010C3C4D/summary.md`. No overall phase PASS is claimed beyond this completed regression gate.
