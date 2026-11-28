# Evidence: P04-WORDPRESS-CODE-029

- **Task ID**: `P04-WORDPRESS-CODE-029`
- **Status**: `DONE`
- **Date**: `2026-11-28`
- **Repository**: `d:\Android\AndroidStudioProjects\kmp-shop` (`develop`)

## Changes Implemented

1. **Shared Psychological Test Service** (`wordpress/packages/carmilla-core/inc/Psych/PsychTestService.php`):
   - Centralized `cb_psychtest` CPT catalog, deterministic server-side question/range parsing (`parse_questions`, `parse_ranges`, `score_answers`, `interpret`, `max_score`), ownership checks (`is_owned`), private attempts (`cb_psych_attempts` user meta), `AUTO` vs `COUNSELOR` result modes, and admin management/interpretation.
   - Option scores are stripped (`score => null`) in all public/client DTOs (`public_questions`).
   - Private attempt questions/results/submissions are strictly owner-scoped (IDOR-safe: non-owners receive `404 NOT_FOUND`).
   - Gated by `psych.tests` (`PsychTestService::is_enabled()`), completely independent of `clinic.booking`.
   - GDPR export/erase remains wired via `PrivacyManager` (`cb_psych_attempts`).

2. **Shared REST Controller** (`wordpress/packages/carmilla-core/inc/Psych/PsychTestRestController.php`):
   - Singleton registration in `RestInfrastructure::on_rest_api_init()` with duplicate-route guard.
   - Canonical endpoints: `GET /api/psych-tests`, `GET /api/psych-tests/(?P<slug>...)`, `GET /api/my-psych-tests`, `GET /api/my-psych-tests/(?P<id>\d+)/questions`, `POST /api/my-psych-tests/(?P<id>\d+)/submit`, `GET /api/my-psych-tests/(?P<id>\d+)/result`, `POST /api/psych-tests/(?P<id>\d+)/submit`, `POST /psych-tests/(?P<id>\d+)/submit` (alias for `psychtest.js`), and `/api/admin/psych-tests*`.

3. **Bridge, Theme & JS Integration**:
   - `wordpress/packages/carmilla-core/Carmilla_Kernel.php` & `RestInfrastructure.php`: loaded and registered `PsychTestService` & `PsychTestRestController`.
   - `wordpress/carmilla-bridge/includes/class-cb-psychtest-controller.php` & `class-cb-admin-content-controller.php`: guarded duplicate route registrations and delegated `parse_questions`.
   - `wordpress/carmilla-theme/assets/js/psychtest.js`: mapped submit endpoint to `api/psych-tests/{id}/submit`.
   - `wordpress/carmilla-bridge/includes/class-cb-public-renderer.php` & `wordpress/carmilla-theme/inc/class-theme-host-adapter.php`: registered `[carmilla_psych_tests]`, `[carmilla_psych_test]`, `[carmilla_my_psych_results]` with `psych.tests` gate and Theme override templates (`carmilla/psych-tests-list.php`, `carmilla/psych-test-detail.php`, `carmilla/my-psych-results.php`).

## Verification & Results (Real WordPress 7.1.3 / MariaDB 10.6.28 Docker)

- **PHP Lint (`php -l` in `test-env-wordpress-test-1`)**: 11/11 files passed with 0 syntax errors.
- **Task Behavioral Test (`test-psych-test-029.php`)**: `20 passed, 0 failed.` (deterministic AUTO scoring, COUNSELOR pending + admin interpretation, score stripping, IDOR 404, feature independence from `clinic.booking`, `403 FEATURE_DISABLED`, data preservation across toggle off/on, shortcodes).
- **Master Phase 04 Verification (`verify-all-phase04-real-wp.php`)**: `ALL 78 MASTER PHASE 04 REAL WORDPRESS CHECKS PASSED!`
- **`git diff --check`**: exit code `0`.

## Limitations

- Commercial/clinical readiness gate remains in Phase P14 (`P14-CLINIC-*`) as noted in the task card.
