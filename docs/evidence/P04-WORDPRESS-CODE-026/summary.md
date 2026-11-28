# Evidence — P04-WORDPRESS-CODE-026

## Summary
Migrated Posts (`post`), Pages (`page`), Categories (`category`), and Media (`attachment`) to the shared `Carmilla\Core\Content` kernel layer (`BlockMapper`, `ContentService`, `ContentRestController`) and wired both Carmilla Theme (`ThemeHostAdapter` + `carmilla/content-posts.php`, `carmilla/content-page.php`) and Carmilla Bridge (`CB_Blocks`, `CB_Blog_Controller`, `CB_Media_Controller`, `CB_Public_Renderer`) so all three host modes (Theme-only, Plugin-only, Co-install) share canonical CRUD, IDs, permissions, media validation, and duplicate prevention.

## Inventory & Changed Files
- `wordpress/packages/carmilla-core/inc/Content/BlockMapper.php` — Canonical Gutenberg <-> `BlogBlockDto` mapper.
- `wordpress/packages/carmilla-core/inc/Content/ContentService.php` — Canonical post, page, category, and media CRUD/upload service with MIME/size validation (`INVALID_MEDIA_TYPE`, `INVALID_MEDIA_SIZE`), capability checks, and `preventDuplicate` idempotency.
- `wordpress/packages/carmilla-core/inc/Content/ContentRestController.php` — Singleton REST route registration for `/api/blogs*`, `/api/admin/blogs*`, `/api/pages*`, `/api/admin/pages*`, `/api/admin/media*`, and `/api/admin/blogs/media/upload`.
- `wordpress/packages/carmilla-core/Carmilla_Kernel.php` — Loaded Content classes in kernel bootstrap.
- `wordpress/packages/carmilla-core/inc/Rest/RestInfrastructure.php` — Registered `ContentRestController::register_routes()` on `rest_api_init` and mapped `/api/pages`, `/api/admin/pages`, `/api/admin/media` to `content.core`.
- `wordpress/carmilla-bridge/includes/class-cb-blocks.php`, `class-cb-blog-controller.php`, `class-cb-media-controller.php` — Delegated to `BlockMapper` and `ContentRestController` (preventing duplicate route registration).
- `wordpress/carmilla-bridge/includes/class-cb-public-renderer.php` — Added `[carmilla_posts]`, `[carmilla_page]`, `[carmilla_content_admin]` shortcodes with `.carmilla-public-surface` scoped CSS and theme override support.
- `wordpress/carmilla-theme/inc/class-theme-host-adapter.php`, `wordpress/carmilla-theme/carmilla/content-posts.php`, `wordpress/carmilla-theme/carmilla/content-page.php` — Theme standalone shortcodes and override templates reading from `ContentService`.
- `wordpress/packages/carmilla-core/tests/test-content-core-026.php` — 25 real-WordPress assertions.

## Verification Commands & Results
```sh
docker exec test-env-wordpress-test-1 sh -c "php -l ... && php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/test-content-core-026.php && php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/verify-all-phase04-real-wp.php"
```
- `php -l`: 0 syntax errors across all modified/created PHP files.
- `test-content-core-026.php`: **25 / 25 passed** (exit code `0`).
- `verify-all-phase04-real-wp.php`: **78 / 78 passed** (exit code `0`).
- `git diff --check`: exit code `0`.
