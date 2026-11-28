# Evidence — P04-WORDPRESS-CODE-027A

## Summary
Migrated Academy enrollment (`my_courses`, `enroll`) and lesson progress (`lesson_progress`) into `Carmilla\Core\Academy\CourseEnrollmentService` and `CourseEnrollmentRestController` preserving `cb_course_prog_{$course_id}` user meta, enforcing fail-closed `403 ENROLLMENT_REQUIRED` on paid courses without purchase/grant, and wiring `[carmilla_my_courses]` / `carmilla/my-courses.php` in both Theme and Plugin.

## Changed Files
- `wordpress/packages/carmilla-core/inc/Academy/CourseEnrollmentService.php` — Canonical enrollment & progress service with paid-course fail-closed guard.
- `wordpress/packages/carmilla-core/inc/Academy/CourseEnrollmentRestController.php` — Singleton REST route registration for `/api/academy/my-courses`, `/api/academy/courses/{id}/enroll`, `/api/academy/lessons/{lid}/progress`.
- `wordpress/packages/carmilla-core/Carmilla_Kernel.php`, `wordpress/packages/carmilla-core/inc/Rest/RestInfrastructure.php` — Loaded and registered `CourseEnrollmentRestController`.
- `wordpress/carmilla-bridge/includes/class-cb-academy-controller.php` — Skipped duplicate enrollment/progress routes when `CourseEnrollmentRestController` is active.
- `wordpress/carmilla-bridge/includes/class-cb-public-renderer.php` — Added `[carmilla_my_courses]` shortcode.
- `wordpress/carmilla-theme/inc/class-theme-host-adapter.php`, `wordpress/carmilla-theme/carmilla/my-courses.php` — Added standalone `[carmilla_my_courses]` shortcode and theme override template.
- `wordpress/packages/carmilla-core/tests/test-course-enrollment-027a.php` — 10 real-WordPress assertions.

## Verification Commands & Results
```sh
docker exec test-env-wordpress-test-1 sh -c "php -l ... && php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/test-course-enrollment-027a.php && php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/verify-all-phase04-real-wp.php"
```
- `php -l`: 0 syntax errors.
- `test-course-enrollment-027a.php`: **10 / 10 passed** (exit code `0`).
- `verify-all-phase04-real-wp.php`: **78 / 78 passed** (exit code `0`).
- `git diff --check`: exit code `0`.
