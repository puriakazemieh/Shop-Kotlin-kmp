# P02-SEC-CODE-010C3B2E1 — Disable unsafe wallet checkout

- Status: DONE
- Phase/Area/Type: P02 / SECURITY / CODE
- Priority/Risk/Size: P0 / CRITICAL / S
- Owner: AI
- Depends on: P02-CI-CODE-010C3B2E
- Blocks: P02-CI-CODE-010C3B2F

## Scope and acceptance

`WalletService::deduct_funds` updates user meta before an unchecked wallet-log insert and uses a non-atomic read/modify/write. `WC_Gateway_Carmilla_Wallet` is registered by `WooCompat` and defaults to enabled. Prevent the wallet gateway from being available at checkout until a separately reviewed atomic balance/ledger and idempotent order implementation exists. Preserve other WooCommerce gateways. Verify registration/checkout behavior in a representative WordPress/WooCommerce environment and record evidence that wallet cannot process payment while other gateways remain. Allowed: bundled core Woo compatibility/wallet gateway code, focused tests, docs/evidence. Do not claim the wallet ledger fixed by merely hiding it.

## Completion record

- Final status: DONE on 2026-11-11. Code change originally landed on 2026-11-10 with PHP lint and real-WordPress filter tests. On 2026-11-11, the full WooCommerce checkout acceptance scenario was executed and verified in disposable WordPress 6.4.3 / WooCommerce 8.6.0 with the delivery-shaped package (commit `c30aea42`, ZIP SHA-256 `b3c581111a2709e821d86d3ab35619089ef3eb6fdc87b7fdcb8116a6e6da2290`): Carmilla Wallet is absent from registered and available gateways and checkout page/Store API, ordinary COD checkout completed order #18 without balance/ledger effects, direct checkout with `payment_method=carmilla_wallet` was rejected with HTTP 400 (`rest_invalid_param`), and direct `process_payment` failed closed. Evidence: `docs/evidence/P02-SEC-CODE-010C3B2E1/woocommerce-checkout-acceptance.md` and `summary.md`. Do not claim wallet transactions repaired or phase gate passed.
