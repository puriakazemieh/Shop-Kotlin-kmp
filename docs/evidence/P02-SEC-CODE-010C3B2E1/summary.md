# P02-SEC-CODE-010C3B2E1 — wallet registration closed

Date: 2026-11-10. `WooCompat::register_gateways` now returns the existing WooCommerce gateway list unchanged and no longer loads or registers the unsafe wallet class. The rest of the gateway list is preserved. The wallet implementation itself is not claimed repaired.

Impact: GitNexus `impact register_gateways --direction upstream` returned UNKNOWN with zero resolved callers on an index two commits behind. Text search confirmed `WooCompat::init` attaches this method to `woocommerce_payment_gateways`, `Carmilla_Kernel` calls `WooCompat::init`, and the wallet gateway used `WalletService::deduct_funds`. The change is restricted to gateway registration. [WooCommerce's gateway API](https://developer.woocommerce.com/docs/features/payments/payment-gateway-api) documents that this filter tells WooCommerce which gateway classes exist.

Checks performed: PHP lint of `WooCompat.php` passed; `smoke-wallet-gateway-disabled.php` printed `PASS wallet gateway absent; other gateways preserved`. Disposable WordPress 6.4.3/PHP 8.1.27/MariaDB 10.6.28 ran the real `woocommerce_payment_gateways` filter with another gateway. With both wallet class files present, pre-change code made the test fail (`FAIL filter changed gateways`, exit 1), while updated code passed. WordPress was freshly installed with synthetic credentials and no production data.

The full WooCommerce checkout path was originally unavailable during the initial filter test. On 2026-11-11, this full scenario was executed and verified in disposable WordPress 6.4.3 / WooCommerce 8.6.0; see [woocommerce-checkout-acceptance.md](woocommerce-checkout-acceptance.md).

## Checkout Acceptance Verification (2026-11-11)

The complete WooCommerce checkout QA scenario was executed in disposable WordPress 6.4.3 / WooCommerce 8.6.0 / PHP 8.1.27 / MariaDB 10.6.28 with the current delivery-shaped package (commit `c30aea42`, ZIP SHA-256 `b3c581111a2709e821d86d3ab35619089ef3eb6fdc87b7fdcb8116a6e6da2290`).

All five acceptance gates passed:
1. `carmilla_wallet` is absent from WooCommerce registered and available gateways; Cash on delivery (`cod`) is enabled and available.
2. `carmilla_wallet` is absent from checkout page HTML and Store API (`payment_methods: ["cod"]`).
3. Synthetic customer placed order #18 via COD (`processing`, $35.00); wallet balance (500.00) and ledger records (0) remained unchanged.
4. Direct checkout request with `payment_method=carmilla_wallet` was rejected with HTTP 400 (`rest_invalid_param: Invalid parameter(s): payment_method`); zero orders created; wallet balance and ledger records remained 500.00 and 0.
5. Direct `process_payment(18)` invocation failed closed (returned `null` with notice `خطا در کسر موجودی`); balance remained 500.00.

Detailed report: [woocommerce-checkout-acceptance.md](woocommerce-checkout-acceptance.md). Containers and network were removed after verification.
