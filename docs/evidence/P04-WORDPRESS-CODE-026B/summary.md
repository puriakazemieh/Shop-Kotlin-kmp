# Evidence — P04-WORDPRESS-CODE-026B

## Summary
Migrated per-user Cart (`cb_cart` user meta) and HPOS-compatible Order CRUD (`WooCommerceAdapter::get_orders`, `get_order`, `wc_create_order`) into `Carmilla\Core\Commerce\CartOrderService` and `CartOrderRestController`, and wired both Carmilla Theme (`ThemeHostAdapter`, `carmilla/cart-summary.php`, `carmilla/orders-list.php`) and Carmilla Bridge (`CB_Cart_Controller`, `CB_Order_Controller`, `CB_Public_Renderer` `[carmilla_cart]` / `[carmilla_orders]`).

## Changed Files
- `wordpress/packages/carmilla-core/inc/Commerce/CartOrderService.php` — Canonical cart & HPOS-compatible order service with `commerce.core` gate and `WOO_INACTIVE` guard without fatals.
- `wordpress/packages/carmilla-core/inc/Commerce/CartOrderRestController.php` — Singleton REST route registration for `/api/cart*` and `/api/orders*` (zero duplicate write/callback handlers in co-install).
- `wordpress/packages/carmilla-core/Carmilla_Kernel.php` — Loaded `CartOrderService` and `CartOrderRestController`.
- `wordpress/packages/carmilla-core/inc/Rest/RestInfrastructure.php` — Registered `CartOrderRestController::register_routes()` on `rest_api_init`.
- `wordpress/carmilla-bridge/includes/class-cb-cart-controller.php`, `class-cb-order-controller.php` — Delegated `register_routes()` to `CartOrderRestController`.
- `wordpress/carmilla-bridge/includes/class-cb-public-renderer.php` — Added `[carmilla_cart]` and `[carmilla_orders]` shortcodes.
- `wordpress/carmilla-theme/inc/class-theme-host-adapter.php`, `wordpress/carmilla-theme/carmilla/cart-summary.php`, `wordpress/carmilla-theme/carmilla/orders-list.php` — Added standalone shortcodes and theme override templates.
- `wordpress/packages/carmilla-core/tests/test-cart-order-026b.php` — 12 real-WordPress assertions.

## Verification Commands & Results
```sh
docker exec test-env-wordpress-test-1 sh -c "php -l ... && php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/test-cart-order-026b.php && php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/verify-all-phase04-real-wp.php"
```
- `php -l`: 0 syntax errors.
- `test-cart-order-026b.php`: **12 / 12 passed** (exit code `0`).
- `verify-all-phase04-real-wp.php`: **78 / 78 passed** (exit code `0`).
- `git diff --check`: exit code `0`.

## Environment Note
In `test-env-wordpress-test-1`, WooCommerce plugin is not installed/activated, so the `WOO_INACTIVE` prerequisite path, singular REST route registration, unauthenticated 401/403 block, `commerce.core` feature-gate enforcement (`403 FEATURE_DISABLED`), and `CartOrderService::get_cart` / `[carmilla_cart]` were verified directly; live `wc_create_order` HPOS writes will be exercised when WooCommerce is present in staging.
