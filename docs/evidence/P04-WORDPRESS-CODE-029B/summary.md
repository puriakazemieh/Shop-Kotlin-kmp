# Evidence: P04-WORDPRESS-CODE-029B

- **Task ID**: `P04-WORDPRESS-CODE-029B`
- **Status**: `DONE`
- **Date**: `2026-11-28`
- **Repository**: `d:\Android\AndroidStudioProjects\kmp-shop` (`develop`)

## Inventory of Interactions & Scope Boundary

- **Migrated in this slice**:
  - Product Reviews (`comment_type = review`, `rating` meta, `verified` meta, `cb_helpful` meta, nested replies)
  - Product Q&A (`comment_type = cb_qna`, nested replies, `hide_qna_clauses` feed isolation)
  - User Favorites / Wishlist (`cb_wishlist` user meta, paginated product list, add/remove)
- **Out-of-slice (remain in Bridge for dedicated future cards)**:
  - `CB_Extras_Controller`: memberships, referrals, recently-viewed, return-requests, recurring-orders, stock-notifications, price-alerts, frequently-bought-together
  - `CB_Story_Controller`: `cb_story` CPT stories endpoint

## Changes Implemented

1. **Shared Interactions Service** (`wordpress/packages/carmilla-core/inc/Interactions/InteractionService.php`):
   - Reviews CRUD + helpful vote, Q&A CRUD + `hide_qna_clauses` feed isolation, and Favorites (`cb_wishlist`) CRUD.
   - Owner/moderator authorization checks (`404 NOT_FOUND` for non-owners on update/delete).
   - Gated by `commerce.core` (`InteractionService::is_enabled()`).

2. **Shared REST Controller** (`wordpress/packages/carmilla-core/inc/Interactions/InteractionRestController.php`):
   - Singleton registration in `RestInfrastructure::on_rest_api_init()` with duplicate-route guard.
   - Endpoints: `/api/reviews*`, `/api/questions*`, `/api/favorites*`.

3. **Bridge, Theme & Shortcodes**:
   - `wordpress/packages/carmilla-core/Carmilla_Kernel.php` & `RestInfrastructure.php`: loaded `InteractionService` & `InteractionRestController`, registered `hide_qna_clauses` filter, mapped `/api/favorites` to `commerce.core`.
   - `wordpress/carmilla-bridge/includes/class-cb-interaction-controller.php` & `class-cb-extras-controller.php`: guarded duplicate route registrations.
   - `wordpress/carmilla-bridge/includes/class-cb-public-renderer.php` & `wordpress/carmilla-theme/inc/class-theme-host-adapter.php`: registered `[carmilla_product_reviews]` and `[carmilla_favorites]` with `commerce.core` gate, scoped `.carmilla-public-surface`, and Theme override templates (`carmilla/product-reviews.php`, `carmilla/favorites.php`).

## Verification & Results (Real WordPress 7.1.3 / MariaDB 10.6.28 Docker)

- **PHP Lint (`php -l` in `test-env-wordpress-test-1`)**: 10/10 files passed with 0 syntax errors.
- **Task Behavioral Test (`test-interactions-029b.php`)**: `17 passed, 0 failed.` (Reviews CRUD + helpful + ownership 404, Q&A CRUD + feed isolation, Favorites add/list/remove, shortcodes, `403 FEATURE_DISABLED` & disabled shortcode).
- **Master Phase 04 Verification (`verify-all-phase04-real-wp.php`)**: `ALL 78 MASTER PHASE 04 REAL WORDPRESS CHECKS PASSED!` (Theme-only, Plugin-only TT4, Co-install).
- **`git diff --check`**: exit code `0`.
