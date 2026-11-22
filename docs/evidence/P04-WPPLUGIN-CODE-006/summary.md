# Evidence Summary: P04-WPPLUGIN-CODE-006

- **Task ID:** P04-WPPLUGIN-CODE-006
- **Title:** resolver مشترک قابلیت، پیش‌نیاز و آمادگی انتشار (Shared capability resolver, prerequisites, and release readiness)
- **Status:** DONE
- **Date:** 2026-11-22
- **Environment:**
  - Real WordPress 7.1.3 on Apache/2.4.68, PHP 8.3.35, MariaDB 10.6 (`tools/test-env/docker-compose.yml`)
  - Container: `test-env-wordpress-test-1` (port 8080:80)
  - PHP CLI 8.1 container: `test-env-php-test-run`

---

## 1. Scope & Objective

Unified capability, prerequisite, and release readiness resolver for both Carmilla hosts (Theme and Plugin/Bridge):
1. **5 Dimensions of Evaluation:**
   - Package code presence (`package_code`)
   - Entitlement license purchase (`entitlements`)
   - Admin toggle settings (`admin_settings`)
   - Environmental prerequisites (`environment`, e.g. WooCommerce)
   - Release readiness gate (`releaseReady !== false`)
2. **6 Distinct Status Codes with Explanatory Persian Reasons:**
   - `ACTIVE`: `'قابلیت فعال و آماده استفاده است.'`
   - `NOT_ENTITLED`: `'این قابلیت در مجوز یا بسته خریداری‌شده سایت قرار ندارد.'`
   - `MISSING_CODE`: `'کد این قابلیت در بسته تحویلی وجود ندارد (نیازمند ارتقای بسته).'`
   - `DISABLED_BY_ADMIN`: `'این قابلیت توسط مدیر در تنظیمات خاموش شده است.'`
   - `PREREQUISITE_MISSING`: `'پیش‌نیاز خارجی ووکامرس (WooCommerce) فعال نیست.'` / `'پیش‌نیاز «...» فعال نیست.'`
   - `RELEASE_GATE_RESTRICTED`: `'این قابلیت در این نسخه هنوز آماده انتشار نیست (محدودیت گیت انتشار).'`
3. **Architectural Guardrails (ADR-006, INDEPENDENT_PRODUCTS_SPEC_FA):**
   - Admin toggle cannot grant unpurchased entitlement (`NOT_ENTITLED`) or invent missing code (`MISSING_CODE`).
   - Missing WooCommerce restricts ONLY commerce.* (`commerce.core`, `commerce.cart`, `commerce.payment`, `commerce.wallet`) and leaves base UI and all other verticals (`academy.*`, `clinic.*`, `psych.*`, `builder.*`) active.
   - Neither Carmilla host requires the other as a prerequisite (Standalone Theme requires NO Bridge; Standalone Plugin requires NO Theme).
   - Release gate (`releaseReady = false`) cannot be bypassed by admin toggle.
   - Base UI (`theme.base`, `plugin.base`, `content.core`) remains preserved and executable.

---

## 2. Implemented Components

1. **`wordpress/packages/carmilla-core/inc/Catalog/CapabilityResolver.php`:**
   - Canonical resolver class implementing `resolve_matrix()`, `resolve_effective()`, `is_feature_active()`, `get_feature_status()`, `get_dependency_violations()`, `get_canonical_dependencies()`, `get_feature_defaults()`, and `get_test_fixtures()`.
2. **`wordpress/packages/carmilla-core/Carmilla_Kernel.php`:**
   - Loads `CapabilityResolver.php`.
   - Updated `resolve_features()` to resolve matrix against active claims and stored admin option (`cb_manifest_features`).
   - Added `get_feature_matrix()` for structured status reporting and diagnostics.
3. **`wordpress/packages/carmilla-core/inc/Entitlement/EntitlementResolver.php`:**
   - Updated `resolve()` to delegate to unified `CapabilityResolver::resolve_effective()`.
4. **`wordpress/carmilla-bridge/includes/class-cb-manifest-controller.php`:**
   - Replaced duplicate legacy arrays with calls to `CapabilityResolver::resolve_effective()`, `get_dependency_violations()`, and `get_feature_defaults()`.
5. **`wordpress/carmilla-theme/inc/class-theme-host-adapter.php` & `template-functions.php`:**
   - Updated `ThemeHostAdapter::is_feature_enabled()` to query `Carmilla_Kernel` / `CapabilityResolver`.
   - Added global `carmilla_feature_enabled()` template helper delegating directly to `ThemeHostAdapter` / `Carmilla_Kernel`.

---

## 3. Automated Verification & Test Results

### Suite 1: Canonical Unit Acceptance Suite
- **Command:** `docker exec test-env-wordpress-test-1 php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/test-capability-resolver-006.php`
- **Result:** `68 Passed, 0 Failed` (Exit Code: 0)
- **Covered Scenarios:**
  - Group 1: Status codes & fixtures matrix loaded.
  - Group 2: Base UI only mode (`theme.base` and `content.core` active, verticals `NOT_ENTITLED`).
  - Group 3: Admin toggle invariance gate (toggling unpurchased or missing-code features leaves them inactive).
  - Group 4: Disabled by admin (`clinic.booking` disabled -> child `clinic.therapists` becomes `PREREQUISITE_MISSING`).
  - Group 5: External prerequisite isolation (missing WooCommerce restricts only `commerce.*`, leaves `academy.*`, `clinic.*`, `psych.*`, `builder.*`, and base UI active).
  - Group 6: Host standalone independence (Theme-only and Plugin-only execute without the other host).
  - Group 7: Release readiness gate (`releaseReady = false` restricts activation regardless of admin toggle).
  - Group 8: Summary statistics calculation.
  - Group 9: Host integrations (`ThemeHostAdapter`, `carmilla_feature_enabled`, `CB_Manifest_Controller`, `Carmilla_Kernel`).

### Suite 2: Real WordPress Environment Verification
- **Command:** `docker exec test-env-wordpress-test-1 php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/verify-phase04-real-wp.php`
- **Result:** `ALL 57 REAL WORDPRESS ACCEPTANCE CHECKS PASSED!` (Exit Code: 0)
- **Covered Checks:**
  - Real MariaDB schema migration and table validation.
  - Distributed lock via real WordPress transients.
  - CapabilityRegistry roles assigned to real WordPress administrator.
  - Live ThemeHostAdapter appearance settings in real Theme Mods.
  - CapabilityResolver executing against live WordPress options and MariaDB.
  - Real HTTP 200 front-end delivery on `http://localhost:8080/`.

### Suite 3: Regression Test Suites
- `test-theme-host-kernel-005.php`: `53 Passed, 0 Failed` (Exit Code: 0)
- `test-kernel-bootstrap-004.php`: `19 Passed, 0 Failed` (Exit Code: 0)
- `test-entitlement-data-039.php`: `48 Passed, 0 Failed` (Exit Code: 0)
- `test-schema-runner-003.php`: `33 Passed, 0 Failed` (Exit Code: 0)
- `smoke-manifest.php`: `ALL PASSED` (Exit Code: 0)

---

## 4. Acceptance Criteria Verification

- [x] یک resolver canonical و تست جدول وضعیت وجود دارد.
- [x] انتخاب مدیر حق خرید یا کد غایب را ایجاد نمی‌کند.
- [x] غیرفعال‌بودن prerequisite فقط قابلیت وابسته را می‌بندد.
- [x] release gate مستقل از toggle و UI-only پایه محفوظ است.
