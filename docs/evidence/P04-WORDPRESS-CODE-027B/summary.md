# Evidence — P04-WORDPRESS-CODE-027B

## Summary
Migrated Academy course/lesson quizzes, server-side scoring, duplicate-attempt/certificate control, and certificate verification into `Carmilla\Core\Academy\CourseQuizService` and `CourseQuizRestController`, preserving existing meta/option schemas (`cb_quiz`, `cb_pass_score`, `cb_lesson_quiz_{idx}`, `cb_quiz_passed_{id}`, `cb_lquiz_passed_{cid}_{idx}`, `cb_certs`, `cb_cert_index`) and wiring `[carmilla_certificates]` / `carmilla/certificates.php` across both Theme and Plugin.

## Changed Files
- `wordpress/packages/carmilla-core/inc/Academy/CourseQuizService.php` — Canonical quiz & certificate service (no answer-flag leakage, duplicate attempt control, fail-closed paid-course check).
- `wordpress/packages/carmilla-core/inc/Academy/CourseQuizRestController.php` — Singleton REST route registration for quiz, certificate, verify, and admin quiz upsert endpoints.
- `wordpress/packages/carmilla-core/Carmilla_Kernel.php`, `wordpress/packages/carmilla-core/inc/Rest/RestInfrastructure.php` — Loaded and registered `CourseQuizRestController`.
- `wordpress/carmilla-bridge/includes/class-cb-academy-controller.php` — Skipped duplicate quiz/certificate routes when `CourseQuizRestController` is active.
- `wordpress/carmilla-bridge/includes/class-cb-public-renderer.php` — Added `[carmilla_certificates]` shortcode.
- `wordpress/carmilla-theme/inc/class-theme-host-adapter.php`, `wordpress/carmilla-theme/carmilla/certificates.php` — Added standalone `[carmilla_certificates]` shortcode and theme override template.
- `wordpress/packages/carmilla-core/tests/test-course-quiz-027b.php` — 11 real-WordPress assertions.

## Verification Commands & Results
```sh
docker exec test-env-wordpress-test-1 sh -c "php -l ... && php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/test-course-quiz-027b.php && php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/verify-all-phase04-real-wp.php"
```
- `php -l`: 0 syntax errors.
- `test-course-quiz-027b.php`: **11 / 11 passed** (exit code `0`).
- `verify-all-phase04-real-wp.php`: **78 / 78 passed** (exit code `0`).
- `git diff --check`: exit code `0`.
