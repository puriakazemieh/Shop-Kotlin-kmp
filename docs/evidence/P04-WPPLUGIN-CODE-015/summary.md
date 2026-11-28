# Evidence Summary: P04-WPPLUGIN-CODE-015

- **Task ID:** `P04-WPPLUGIN-CODE-015`
- **Title:** سازگاری Woo HPOS و Cart/Checkout Blocks در دو محصول
- **Date:** 2026-11-28
- **Base Commit:** `f142f332`
- **Status:** `DONE`

## Summary of Changes

1. **WooCommerce HPOS & Cart/Checkout Blocks Compatibility (`wordpress/packages/carmilla-core/inc/Adapter/WooCompat.php`)**:
   - Fixed `declare_hpos_compatibility()` to declare `custom_order_tables` and `cart_checkout_blocks` against the canonical `carmilla-bridge/carmilla-bridge.php` plugin entrypoint (`resolve_bridge_plugin_file()`) instead of `WooCompat.php`'s own `__FILE__`.
   - Added idempotency guard (`$initialized`) in `init()` so co-install never registers duplicate `before_woocommerce_init`, `wp_enqueue_scripts`, or `woocommerce_payment_gateways` hooks.
   - Restricted `dequeue_woo_blocks_styles()` via `should_dequeue_blocks_styles()` so third-party themes in Plugin-only mode (e.g. Twenty Twenty-Four, Storefront) retain their default WooCommerce Blocks styles.
   - Added `TESTED_WOO_VERSIONS` (`8.0.0` – `9.9.9`) and `get_compatibility_matrix()`.
2. **Real WooCommerce HPOS & Blocks Test Suite (`wordpress/packages/carmilla-core/tests/test-woo-hpos-blocks-015.php`)**:
   - Verified `Automattic\WooCommerce\Utilities\FeaturesUtil` recognizes `carmilla-bridge/carmilla-bridge.php` as compatible with both `custom_order_tables` (HPOS) and `cart_checkout_blocks`.
   - Verified `woocommerce_payment_gateways` filter is registered exactly once in co-install.
   - Verified `WooCommerceAdapter::is_hpos_enabled()` under HPOS OFF and HPOS ON, real `wc_create_order()` fixture parity (`ID`, `total=280000`, `status=processing`) across `Theme-Only`, `Plugin-Only` (TT4), and `Co-Install`, and checkout block style isolation.

## Verification Commands & Results

- **Environment:** Docker `test-env-wordpress-test-1` (WordPress 7.1.3, MariaDB 10.6.28, real WooCommerce installed)
- **Command:**
  `docker exec test-env-wordpress-test-1 sh -c "php -l ...WooCompat.php && php ...smoke-wallet-gateway-disabled.php && php ...test-woo-hpos-blocks-015.php && php ...verify-all-phase04-real-wp.php"`
- **Results:**
  - PHP syntax lint (`WooCompat.php`): **0 errors (exit code 0)**
  - `smoke-wallet-gateway-disabled.php`: **PASS (exit code 0)**
  - `test-woo-hpos-blocks-015.php`: **14 / 14 passed (exit code 0)**
  - `verify-all-phase04-real-wp.php`: **78 / 78 passed (exit code 0)**
