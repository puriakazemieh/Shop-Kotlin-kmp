# Evidence: P04-ENTITLEMENT-CODE-040 — resolver مجوز معتبر و قابلیت مؤثر در هسته

## Overview
- **Task ID:** P04-ENTITLEMENT-CODE-040
- **Scope:** Kernel entitlement validation, site-binding, tampering detection, fail-closed feature resolution, direct POST rejection on unpurchased features, builder independence, and co-install single-counted quotas.
- **Date:** 2026-11-23
- **Environment:**
  - Real WordPress Test Container: `test-env-wordpress-test-1` (WordPress 7.1.3 / Apache / PHP 8.3.35 on `http://localhost:8080`)
  - Database: `test-env-db-1` (MariaDB 10.6.28 on port 3306)
  - PHP CLI Harness: `php-test` (PHP 8.1-cli via Docker)

---

## Key Technical Implementations
1. **License Validation in `CapabilityResolver`:**
   - In [`CapabilityResolver::resolve_matrix`](file:///d:/Android/AndroidStudioProjects/kmp-shop/wordpress/packages/carmilla-core/inc/Catalog/CapabilityResolver.php), integrated full license claims validation via `EntitlementResolver::validate_license($claims, true)`.
   - If license claims are unsigned, tampered (`tampered=true` / `is_valid=false`), expired beyond grace period, revoked (`status=revoked`), or bound to an unauthorized domain (`site_url`), all features resolve to `is_active=false` and `status=NOT_ENTITLED` (fail-closed).
2. **Dynamic Site Binding for Fixtures:**
   - In [`EntitlementClaims::get_default_site_url()`](file:///d:/Android/AndroidStudioProjects/kmp-shop/wordpress/packages/carmilla-core/inc/Catalog/EntitlementClaims.php), dynamically binds developer fallback fixtures to the current WordPress site URL (`get_site_url()`), while foreign domain test cases strictly fail validation.
3. **Single-Counted Site Quota Resolution:**
   - In [`Carmilla_Kernel::get_quota()`](file:///d:/Android/AndroidStudioProjects/kmp-shop/wordpress/packages/carmilla-core/Carmilla_Kernel.php), integrated single-counted quota retrieval from license claims options with cache in `EntitlementResolver`, guaranteeing zero doubling across Theme and Plugin in co-install mode.
4. **Comprehensive REST Dispatch Boundary Guard (Direct POST Rejection):**
   - In [`CB_Manifest_Controller::feature_for_route()`](file:///d:/Android/AndroidStudioProjects/kmp-shop/wordpress/carmilla-bridge/includes/class-cb-manifest-controller.php), expanded route mapping to cover `/api/my-psych-tests`, `/api/my-courses`, `/api/reviews`, `/api/questions`, `/api/stories`, `/api/support`.
   - In `CB_Manifest_Controller::guard_rest_request()`, direct POST/GET requests targeting unpurchased, disabled, or tampered features are halted immediately at `rest_pre_dispatch` with HTTP 403 Forbidden and `FEATURE_DISABLED` error.
5. **App Builder & Data Client API Independence:**
   - Disabling all platform builders (`android`, `ios`, `desktop`, `pwa`, `web`) sets builder targets to false without affecting domain data APIs (`commerce.core`, `academy.core`, `clinic.booking`, `content.core`), keeping client mobile apps and web clients fully functional.
6. **Minimal Public Client Manifest:**
   - `/carmilla/v1/client-manifest` strictly returns boolean feature flags and metadata, with zero exposure of credentials, secrets, private options, or license keys.

---

## Verification Results

### 1. Dedicated Acceptance Suite (64 Tests)
Command:
```bash
docker compose -f tools/test-env/docker-compose.yml run --rm php-test php wordpress/packages/carmilla-core/tests/test-entitlement-resolver-040.php
```
Result:
```
=================================================================
 Acceptance Suite: P04-ENTITLEMENT-CODE-040
=================================================================
-- Group 1: Signature & Tampering Enforcement -- (10 PASS)
-- Group 2: Site Binding Enforcement -- (4 PASS)
-- Group 3: Expiration & Grace Period Lifecycle -- (8 PASS)
-- Group 4: Revocation Handling -- (5 PASS)
-- Group 5: Direct POST Rejection on Unpurchased Features -- (9 PASS)
-- Group 6: App Builder vs Data Client API Independence -- (8 PASS)
-- Group 7: Co-Install Single-Counted Quota Resolution -- (4 PASS)
-- Group 8: External Prerequisite Isolation -- (8 PASS)
-- Group 9: Minimal Public Manifest -- (8 PASS)
=================================================================
ALL 64 P04-ENTITLEMENT-CODE-040 ACCEPTANCE TESTS PASSED!
=================================================================
```

### 2. Live HTTP Verification against Real WordPress Apache Server
Command:
```bash
curl.exe -s -i http://localhost:8080/wp-json/carmilla/v1/client-manifest
```
Result:
- HTTP 200 OK
- Clean minimal payload containing boolean features map
- Zero secrets, license keys, or private audit logs exposed

Command (Direct POST to unpurchased feature):
```bash
curl.exe -s -i -X POST http://localhost:8080/wp-json/carmilla/v1/api/courses
```
Result:
```
HTTP/1.1 403 Forbidden
{"code":"FEATURE_DISABLED","message":"این قابلیت در حال حاضر فعال نیست.","data":{"status":403,"feature":"academy.core"}}
```

### 3. Master Real WordPress Suite (78 Tests)
Command:
```bash
docker exec test-env-wordpress-test-1 php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/verify-all-phase04-real-wp.php
```
Result:
```
=================================================================
ALL 78 MASTER PHASE 04 REAL WORDPRESS CHECKS PASSED!
=================================================================
```

### 4. Regression Test Suites
- `test-capability-resolver-006.php`: 68 Passed, 0 Failed
- `test-theme-host-kernel-005.php`: 53 Passed, 0 Failed
- `test-entitlement-data-039.php`: 53 Passed, 0 Failed
- `test-schema-runner-003.php`: 36 Passed, 0 Failed
- `test-kernel-bootstrap-004.php`: 23 Passed, 0 Failed
- `smoke-carmilla-core.php`: 33 Passed, 0 Failed

---

## Acceptance Criteria Checklist
- [x] هدف و معیار اختصاصی همین کارت با شواهد قابل بازبینی محقق شده‌اند.
- [x] scope و مرز محصول رعایت و کار خارج کارت انجام نشده است.
- [x] خطا/مجوز نامعتبر/قابلیت خاموش طبق مورد آزموده شده است.
- [x] QA فنی و تست زمان اجرای واقعی روی محیط زنده کامل گردیده است.
