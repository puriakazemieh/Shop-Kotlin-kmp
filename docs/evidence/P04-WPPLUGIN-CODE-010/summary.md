# Evidence Summary: P04-WPPLUGIN-CODE-010

- **Task ID:** `P04-WPPLUGIN-CODE-010`
- **Title:** registry نقش‌ها و مجوز عملیات در هسته مشترک
- **Date:** 2026-11-28
- **Base Commit:** `f2eb1f74`
- **Status:** `DONE`

## Summary of Changes

1. **Shared Capability & Role Registry (`wordpress/packages/carmilla-core/inc/Capabilities/CapabilityRegistry.php`)**:
   - Added `DOMAIN_PRIMARY_FEATURES` mapping business domains (`content`, `shop`, `education`, `clinic`, `support`, `admin`) to canonical feature identifiers.
   - Added canonical domain role definitions (`ROLES`) with least-privilege capabilities:
     - `carmilla_instructor` (`carmilla_view_courses`, `carmilla_edit_courses`, `carmilla_read_content`; explicitly denies `manage_options`, `carmilla_manage_settings`, `carmilla_manage_licenses`, `carmilla_manage_courses`, and cross-domain admin caps).
     - `carmilla_therapist` (`carmilla_view_appointments`, `carmilla_edit_appointments`, `carmilla_read_content`; explicitly denies admin and cross-domain caps).
     - `carmilla_support_agent` (`carmilla_view_tickets`, `carmilla_reply_tickets`, `carmilla_read_content`; explicitly denies admin, license, and shop/education caps).
     - `carmilla_shop_manager` (`carmilla_manage_shop`, `carmilla_view_orders`, `carmilla_edit_products`, `carmilla_delete_products`; explicitly denies `manage_options`, `carmilla_manage_settings`, and `carmilla_manage_licenses`).
   - Implemented `register_roles()`, `unregister_roles()`, `get_role_capabilities()`, and WooCommerce `shop_manager` capability alignment in `register_capabilities()`.
   - Implemented `check_access()` enforcing the 4-gate decoupled security model:
     1. **Layer 1 (Site Entitlement):** Blocks unpurchased features (`FEATURE_NOT_ENTITLED`, 403) even for administrators.
     2. **Layer 2 (Admin Feature Toggle & Prerequisites):** Blocks admin-disabled features (`FEATURE_DISABLED`, 403), missing prerequisites (`PREREQUISITE_MISSING`, 403), and unreleased gates (`RELEASE_GATE_RESTRICTED`, 403).
     3. **Layer 3 (User Capability / RBAC):** Rejects unauthenticated (`UNAUTHENTICATED`, 401) and unauthorized users (`FORBIDDEN`, 403). Purchasing or enabling a feature never grants a Subscriber/Customer administrative capabilities.
     4. **Layer 4 (Private Data / Ownership):** Prevents non-managers from viewing or modifying another user's private resources (`FORBIDDEN`, 403).
   - Implemented `get_permission_callback()` factory for WordPress REST routes.

2. **Kernel Boot Hook (`wordpress/packages/carmilla-core/Carmilla_Kernel.php`)**:
   - Updated `Carmilla_Kernel::init_core()` to invoke `CapabilityRegistry::register_roles()` alongside `CapabilityRegistry::register_capabilities()`.

3. **Real WordPress Test Suite (`wordpress/packages/carmilla-core/tests/test-capability-roles-010.php`)**:
   - Verified role registration, least-privilege boundaries, negative fixtures across all 4 gates, REST permission callbacks, and parity across all 3 host modes (`Co-Install`, `Theme-Only`, `Plugin-Only` on Twenty Twenty-Four).

## Verification Commands & Results

- **Environment:** Docker `test-env-wordpress-test-1` (WordPress 7.1.3, MariaDB 10.6.28, Apache)
- **Commands:**
  - `docker exec test-env-wordpress-test-1 sh -c "php -l /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/inc/Capabilities/CapabilityRegistry.php && php -l /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/Carmilla_Kernel.php && php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/test-capability-roles-010.php && php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/verify-all-phase04-real-wp.php"`
- **Results:**
  - PHP syntax lint (`CapabilityRegistry.php`, `Carmilla_Kernel.php`): **0 errors (exit code 0)**
  - `test-capability-roles-010.php`: **70 / 70 passed (0 failed, exit code 0)**
  - `verify-all-phase04-real-wp.php`: **78 / 78 passed (exit code 0)**
