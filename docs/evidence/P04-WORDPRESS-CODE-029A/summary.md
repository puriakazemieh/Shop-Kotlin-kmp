# Evidence: P04-WORDPRESS-CODE-029A

- **Task ID**: `P04-WORDPRESS-CODE-029A`
- **Status**: `DONE`
- **Date**: `2026-11-28`
- **Repository**: `d:\Android\AndroidStudioProjects\kmp-shop` (`develop`)

## Changes Implemented

1. **Shared Support Service** (`wordpress/packages/carmilla-core/inc/Support/SupportService.php`):
   - Centralized support tickets & message threads stored in user meta `cb_tickets` (strictly isolated from `cb_psych_attempts` and clinical `cb_msg` comments).
   - Owner-scoped `list_tickets`, `get_ticket`, `create_ticket`, `post_message` (`404 NOT_FOUND` for non-owners).
   - Admin management `admin_list_tickets`, `admin_set_status` (`OPEN`, `ANSWERED`, `CLOSED`), and admin reply (`senderRole: ADMIN`).
   - Legacy `support.js` adapter (`legacy_thread`, `legacy_post`).
   - Gated by `content.support` (`SupportService::is_enabled()`).

2. **Shared REST Controller** (`wordpress/packages/carmilla-core/inc/Support/SupportRestController.php`):
   - Singleton registration in `RestInfrastructure::on_rest_api_init()` with duplicate-route guard.
   - Endpoints: `GET/POST /api/support/tickets`, `GET /api/support/tickets/(?P<id>\d+)`, `POST /api/support/tickets/(?P<id>\d+)/messages`, legacy `GET/POST /support`, and admin `/api/admin/support/tickets*`.

3. **Bridge, Theme & Shortcodes**:
   - `wordpress/packages/carmilla-core/Carmilla_Kernel.php` & `RestInfrastructure.php`: loaded `SupportService` & `SupportRestController`, mapped `/api/admin/support` to `content.support` in `feature_for_route()`.
   - `wordpress/carmilla-bridge/includes/class-cb-support-controller.php`: guarded duplicate route registration.
   - `wordpress/carmilla-bridge/includes/class-cb-public-renderer.php` & `wordpress/carmilla-theme/inc/class-theme-host-adapter.php`: registered `[carmilla_support_tickets]` and `[carmilla_support_thread]` with `content.support` gate, strict owner-only rendering, and Theme override templates (`carmilla/support-tickets.php`, `carmilla/support-thread.php`).

## Verification & Results (Real WordPress 7.1.3 / MariaDB 10.6.28 Docker)

- **PHP Lint (`php -l` in `test-env-wordpress-test-1`)**: 9/9 files passed with 0 syntax errors.
- **Task Behavioral Test (`test-support-029a.php`)**: `19 passed, 0 failed.` (owner CRUD, legacy `/support`, IDOR 404 & shortcode isolation, subscriber blocked from admin, data isolation from `cb_psych_attempts`, admin reply/status, `403 FEATURE_DISABLED` & disabled shortcode).
- **Master Phase 04 Verification (`verify-all-phase04-real-wp.php`)**: `ALL 78 MASTER PHASE 04 REAL WORDPRESS CHECKS PASSED!` (Theme-only, Plugin-only TT4, Co-install).
- **`git diff --check`**: exit code `0`.
