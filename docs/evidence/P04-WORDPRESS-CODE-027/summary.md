# Evidence — P04-WORDPRESS-CODE-027

## Summary
Migrated Course (`cb_course`) catalog and Lesson content (`cb_lessons`, `cb_lesson_files_{idx}`) into `Carmilla\Core\Academy\CourseCatalogService` and `CourseCatalogRestController`, keeping enrollment/progress (`027A`) and quizzes/certificates (`027B`) separate, and wired both Carmilla Theme (`ThemeHostAdapter`, `carmilla/courses-list.php`, `carmilla/course-detail.php`) and Carmilla Bridge (`CB_Academy_Controller`, `CB_Public_Renderer` `[carmilla_courses]` / `[carmilla_course]`).

## Changed Files
- `wordpress/packages/carmilla-core/inc/Academy/CourseCatalogService.php` — Canonical `cb_course` & `cb_lessons` service with synthetic lesson IDs (`courseId * 100000 + index + 1`), fail-closed protected lesson redaction (`videoUrl: null`, `resourceFiles: []` for unauthorized guests), and `academy.core` gate check.
- `wordpress/packages/carmilla-core/inc/Academy/CourseCatalogRestController.php` — Singleton REST route registration for `/api/courses`, `/api/courses/{slug}`, `/api/admin/courses*`, `/api/admin/courses/{id}/lessons`.
- `wordpress/packages/carmilla-core/Carmilla_Kernel.php` — Loaded `CourseCatalogService` and `CourseCatalogRestController`.
- `wordpress/packages/carmilla-core/inc/Rest/RestInfrastructure.php` — Registered `CourseCatalogRestController::register_routes()` on `rest_api_init`.
- `wordpress/carmilla-bridge/includes/class-cb-academy-controller.php` — Skipped duplicate `/api/courses` and `/api/courses/{slug}` registration when `CourseCatalogRestController` is active.
- `wordpress/carmilla-bridge/includes/class-cb-public-renderer.php` — Added `[carmilla_courses]` and `[carmilla_course]` shortcodes.
- `wordpress/carmilla-theme/inc/class-theme-host-adapter.php`, `wordpress/carmilla-theme/carmilla/courses-list.php`, `wordpress/carmilla-theme/carmilla/course-detail.php` — Added standalone shortcodes and theme override templates.
- `wordpress/packages/carmilla-core/tests/test-course-catalog-027.php` — 16 real-WordPress assertions.

## Verification Commands & Results
```sh
docker exec test-env-wordpress-test-1 sh -c "php -l ... && php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/test-course-catalog-027.php && php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/verify-all-phase04-real-wp.php"
```
- `php -l`: 0 syntax errors.
- `test-course-catalog-027.php`: **16 / 16 passed** (exit code `0`).
- `verify-all-phase04-real-wp.php`: **78 / 78 passed** (exit code `0`).
- `git diff --check`: exit code `0`.
