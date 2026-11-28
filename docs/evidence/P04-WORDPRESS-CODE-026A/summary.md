# Evidence — P04-WORDPRESS-CODE-026A

## Summary
Migrated category tree, product list/search/detail, media mapping, campaign/banner read, and admin product CRUD into `Carmilla\Core\Catalog\CatalogService` and `CatalogRestController` backed by `WooCommerceAdapter`, and wired both Carmilla Theme (`ThemeHostAdapter`, `carmilla/catalog-list.php`, `carmilla/catalog-product.php`) and Carmilla Bridge (`CB_Catalog_Controller`, `CB_Public_Renderer` `[carmilla_catalog]` / `[carmilla_product]`).

## Changed Files
- `wordpress/packages/carmilla-core/inc/Catalog/CatalogService.php` — Canonical WooCommerce catalog service (categories, products, search, detail, media mapping, CRUD, graceful `503 WOOCOMMERCE_INACTIVE` without fatal when Woo is absent).
- `wordpress/packages/carmilla-core/inc/Catalog/CatalogRestController.php` — Singleton REST route registration for `/api/categories`, `/api/products*`, `/api/campaigns/active`, `/api/banners`, `/api/admin/products*`.
- `wordpress/packages/carmilla-core/Carmilla_Kernel.php` — Loaded `CatalogService` and `CatalogRestController`.
- `wordpress/packages/carmilla-core/inc/Rest/RestInfrastructure.php` — Registered `CatalogRestController::register_routes()` on `rest_api_init`.
- `wordpress/carmilla-bridge/includes/class-cb-catalog-controller.php` — Delegated `register_routes()` to `CatalogRestController`.
- `wordpress/carmilla-bridge/includes/class-cb-public-renderer.php` — Added `[carmilla_catalog]` and `[carmilla_product]` shortcodes.
- `wordpress/carmilla-theme/inc/class-theme-host-adapter.php` — Removed duplicate `/api/products*` route registration and added `[carmilla_catalog]` / `[carmilla_product]` standalone shortcodes.
- `wordpress/carmilla-theme/carmilla/catalog-list.php`, `wordpress/carmilla-theme/carmilla/catalog-product.php` — Theme override templates reading from `CatalogService`.
- `wordpress/packages/carmilla-core/tests/test-catalog-adapter-026a.php` — 12 real-WordPress assertions.

## Verification Commands & Results
```sh
docker exec test-env-wordpress-test-1 sh -c "php -l ... && php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/test-catalog-adapter-026a.php && php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/verify-all-phase04-real-wp.php"
```
- `php -l`: 0 syntax errors.
- `test-catalog-adapter-026a.php`: **12 / 12 passed** (exit code `0`).
- `verify-all-phase04-real-wp.php`: **78 / 78 passed** (exit code `0`).
- `git diff --check`: exit code `0`.

## Environment Note
In `test-env-wordpress-test-1`, WooCommerce plugin is not installed/activated, so the Woo-inactive branch (`503 WOOCOMMERCE_INACTIVE` + prerequisite notice without fatal) was exercised alongside singular REST registration, 403 unauthorized admin check, and `commerce.core` feature-gate enforcement (`403 FEATURE_DISABLED`).
