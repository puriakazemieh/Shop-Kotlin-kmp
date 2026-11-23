# Evidence Summary: P04-WPPLUGIN-CODE-009

## 1. Overview
- **Task ID:** `P04-WPPLUGIN-CODE-009`
- **Task Title:** مرز REST مشترک WordPress برای هر دو میزبان (Shared WordPress REST boundary for both hosts and co-install)
- **Status:** DONE
- **Date:** 2026-11-23
- **Environment:**
  - Real WordPress 7.1.3 on Apache + MariaDB 10.6.28 inside Docker container `test-env-wordpress-test-1` (port 8080).
  - PHP 8.1 / 8.2 runtime with active WooCommerce 9.x.
  - Installed Themes: `carmilla-theme` (0.8.0), `twentytwentyfour` (1.2), `storefront` (4.6.2).
  - Installed Plugins: `carmilla-bridge` (1.0.0), `woocommerce`.

---

## 2. Key Implementations

### 2.1 Standardized Error Envelope (`RestInfrastructure::error_response`)
- Standardized the error response format to strictly conform to KMP's `ApiError` kotlinx deserializer:
  ```json
  {
    "message": "نام کاربری یا رمز عبور نادرست است.",
    "status": "401",
    "code": 401,
    "errorCode": "BAD_CREDENTIALS",
    "path": "api/auth/login",
    "timestamp": "2026-11-23T09:30:00Z",
    "success": false,
    "error": {
      "code": "BAD_CREDENTIALS",
      "message": "نام کاربری یا رمز عبور نادرست است."
    }
  }
  ```
- Guaranteed compatibility with KMP `ResultHandler.kt` which decodes `json.decodeFromString<ApiError>(bodyText)`.
- HTTP status code is set directly on `WP_REST_Response`.

### 2.2 Standardized Pagination Envelope & Ceiling (`RestInfrastructure::paginated_response`)
- Fully supports KMP `PageResponse<T>` / `PageMetadata` as well as backward-compatible flat fields:
  ```json
  {
    "content": [...],
    "items": [...],
    "data": [...],
    "page": {
      "size": 20,
      "number": 1,
      "totalElements": 100,
      "totalPages": 5
    },
    "pagination": {
      "total": 100,
      "page": 1,
      "per_page": 20,
      "total_pages": 5
    },
    "totalPages": 5,
    "totalElements": 100,
    "success": true
  }
  ```
- Enforces HTTP headers `X-WP-Total` and `X-WP-TotalPages`.
- Enforces strict safety ceiling `MAX_PER_PAGE = 100` via `sanitize_per_page($per_page, $default = 10, $max = 100)`.

### 2.3 Strict Two-Backend Profile Boundary with Provider Metadata
- Strict conformance to Section 8 of `INDEPENDENT_PRODUCTS_SPEC_FA.md`: Exactly two backend profiles exist (`WORDPRESS` and `SPRING`). No third backend is created.
- Provider mode is dynamically resolved from actual WordPress activation:
  - `plugin`: when `carmilla-bridge` is active on an external theme (`twentytwentyfour` or `storefront`).
  - `theme`: when `carmilla-theme` is active alone (`carmilla-bridge` deactivated).
  - `co-install`: when both `carmilla-theme` and `carmilla-bridge` are active.
- Metadata is returned in `/client-manifest` (`hostMode`, `providerMetadata`) and response headers (`X-Carmilla-Provider-Mode`, `X-Carmilla-Backend-Profile: WORDPRESS`).

### 2.4 Standalone Theme Host REST Registration & Bearer JWT Auth
- In Mode 2 (Theme Host alone, Bridge absent), `ThemeHostAdapter` registers the canonical REST routes for:
  - Manifest (`/carmilla/v1/client-manifest`)
  - Authentication (`/carmilla/v1/api/auth/login`, `/carmilla/v1/api/auth/register`)
  - Catalog (`/carmilla/v1/api/products`, `/carmilla/v1/api/products/(?P<id>\d+)`) via `WooCommerceAdapter`
  - Profile (`/carmilla/v1/api/users/me`)
- Shared `JwtService` in `packages/carmilla-core` provides HS256 token generation and verification mirroring Spring claims (`uid`, `sub`, `role`, `typ`).
- `RestInfrastructure::authenticate_bearer()` hooks `determine_current_user` so `Authorization: Bearer <jwt>` works universally across all modes.

### 2.5 Legacy Root Alias Preservation
- Legacy root aliases (`/api/users/me`, `/api/addresses`, `/api/favorites`, `/api/recently-viewed`) are intercepted on `parse_request` and rewritten to `/carmilla/v1` routes with deprecation headers (`X-Carmilla-Deprecated: root-alias`).

---

## 3. Verification & Automated Test Results

### 3.1 Task 009 Dedicated Test Suite (`test-rest-boundary-009.php`)
- Command:
  ```bash
  docker exec test-env-wordpress-test-1 php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/test-rest-boundary-009.php
  ```
- **Result:** Exit code 0, 74/74 assertions passed.
  - Group 1: Error Envelope Compliance with KMP ApiError (10/10 PASS)
  - Group 2: Pagination Envelope, Headers, and Ceiling (14/14 PASS)
  - Group 3: Provider Mode & Diagnostic Metadata (8/8 PASS)
  - Group 4: JWT Token Service Integration (5/5 PASS)
  - Group 5: Real HTTP Parity Across All 3 Host Modes on Real WordPress (27/27 PASS)
    - Mode 1 (Plugin Host Alone on TT4): 9/9 PASS
    - Mode 2 (Theme Host Alone with Bridge Deactivated): 9/9 PASS
    - Mode 3 (Co-Install with Theme + Bridge Active): 9/9 PASS
  - Group 6: Cross-Mode DTO and Error Parity (3/3 PASS)
  - Group 7: Bearer Token User Authentication (2/2 PASS)
  - Group 8: Legacy Root Alias Preservation (2/2 PASS)

### 3.2 Regression Suites
1. `verify-all-phase04-real-wp.php`:
   - Command: `docker exec test-env-wordpress-test-1 php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/verify-all-phase04-real-wp.php`
   - **Result:** Exit code 0, 78/78 assertions passed.
2. `test-woocommerce-adapter-008.php`:
   - Command: `docker exec test-env-wordpress-test-1 php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/test-woocommerce-adapter-008.php`
   - **Result:** Exit code 0, 45/45 assertions passed.
3. `test-plugin-host-standalone-007.php`:
   - Command: `docker exec test-env-wordpress-test-1 php /var/www/html/wp-content/plugins/carmilla-bridge/tests/test-plugin-host-standalone-007.php`
   - **Result:** Exit code 0, 53/53 assertions passed.

**Total Verified Assertions:** 250 / 250 PASS (100%).

---

## 4. Acceptance Criteria Verification
- [x] **قرارداد دو میزبان و co-install واحد است:** کلاینت‌های KMP در هر ۳ حالت (پوسته تنها، افزونه تنها روی قالب استاندارد، و نصب هم‌زمان) از طریق همان endpointهای مشترک `/wp-json/carmilla/v1/api/*` با همان DTOها ارتباط برقرار می‌کنند.
- [x] **pagination/validation/envelope تست دارند:** ساختار PageResponse، سقف pagination روی ۱۰۰، هدرهای استاندارد، و فرمت دوتایی ApiError با تست واقعی HTTP در محیط Docker تأیید شدند.
- [x] **aliasهای موجود و DTO مصرف‌کننده محافظت شده‌اند:** مسیرهای ریشه `/api/users/me`، `/api/addresses` و غیره همراه با هدرهای Deprecation پشتیبانی می‌شوند.
- [x] **فقط دو backend profile حفظ شده است:** پروفایل سرور منحصراً `WORDPRESS` (یا `SPRING`) گزارش می‌شود و هیچ پروفایل سومی برای پوسته یا افزونه ایجاد نشده است.
