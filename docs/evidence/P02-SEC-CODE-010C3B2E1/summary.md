# P02-SEC-CODE-010C3B2E1 — wallet registration closed

Date: 2026-11-10. `WooCompat::register_gateways` now returns the existing WooCommerce gateway list unchanged and no longer loads or registers the unsafe wallet class. The rest of the gateway list is preserved. The wallet implementation itself is not claimed repaired.

Impact: GitNexus `impact register_gateways --direction upstream` returned UNKNOWN with zero resolved callers on an index two commits behind. Text search confirmed `WooCompat::init` attaches this method to `woocommerce_payment_gateways`, `Carmilla_Kernel` calls `WooCompat::init`, and the wallet gateway used `WalletService::deduct_funds`. The change is restricted to gateway registration. [WooCommerce's gateway API](https://developer.woocommerce.com/docs/features/payments/payment-gateway-api) documents that this filter tells WooCommerce which gateway classes exist.

Checks performed: PHP lint of `WooCompat.php` passed; `smoke-wallet-gateway-disabled.php` printed `PASS wallet gateway absent; other gateways preserved`. Disposable WordPress 6.4.3/PHP 8.1.27/MariaDB 10.6.28 ran the real `woocommerce_payment_gateways` filter with another gateway. With both wallet class files present, pre-change code made the test fail (`FAIL filter changed gateways`, exit 1), while updated code passed. WordPress was freshly installed with synthetic credentials and no production data.

The full WooCommerce checkout path was unavailable: latest WooCommerce required newer WordPress, and both container and host attempts to fetch a compatible plugin ZIP from WordPress.org failed with connection refusal. Therefore the task remains `AWAITING_MANUAL_QA`; no checkout/UI PASS is inferred from the filter test. The disposable containers and network must be removed after evidence capture.

Manual QA on a disposable or staging WordPress site with compatible WooCommerce and the current Carmilla Bridge/Core package:

1. Enable one ordinary test payment gateway (for example, cash on delivery), and confirm Carmilla Wallet is absent under WooCommerce > Settings > Payments.
2. Create a synthetic customer and a low-value test product. Open checkout as that customer; confirm the ordinary test gateway appears and Carmilla Wallet does not.
3. Submit the test checkout with the ordinary gateway; confirm it completes under that gateway.
4. In a separate checkout request, attempt to submit `payment_method=carmilla_wallet` directly. Confirm WooCommerce rejects or declines it, no order becomes paid through the wallet, and the customer's wallet balance and wallet ledger do not change.
5. Record WooCommerce/WordPress versions, the package commit, screenshots or response/status, test order IDs, and before/after synthetic balance and ledger counts. Do not use a production payment or customer.
