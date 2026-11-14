<div dir="rtl" align="right">

# گزارش P03-MANIFEST-OPS-021: موجودی، Telemetry و راستی‌آزمایی Aliasهای Legacy روی WordPress واقعی

## ۱. جدول کامل موجودی Aliasها (Inventory)

| Legacy Alias | Canonical Endpoint / Method | مصرف‌کننده (Consumer) | زمان خروج قطعی (Sunset Cycle) |
|---|---|---|---|
| `/api/users/me` | `/wp-json/carmilla/v1/api/users/me` | کلاینت‌های قدیمی، بیلد‌های Web/Desktop | یک چرخه کلاینت (تا پایان فاز P03) |
| `/api/addresses` | `/wp-json/carmilla/v1/api/addresses` | کلاینت‌های قدیمی وب و موبایل | یک چرخه کلاینت (تا پایان فاز P03) |
| `/api/favorites` | `/wp-json/carmilla/v1/api/favorites` | کلاینت‌های قدیمی | یک چرخه کلاینت (تا پایان فاز P03) |
| `/api/recently-viewed` | `/wp-json/carmilla/v1/api/recently-viewed` | کلاینت‌های قدیمی | یک چرخه کلاینت (تا پایان فاز P03) |
| `BootstrapProfiles.fromLegacyApiRoot` | `BootstrapProfiles.spring` / `wordpress` | کدهای قدیمی توسعه/بیلد کلاینت | تا پایان بازسازی Bootstrap |

---

## ۲. سازوکار Deprecation و Telemetry
در `wordpress/carmilla-bridge/includes/class-cb-plugin.php` متد `maybe_root_alias()`:
1. استفاده از مسیر alias در گزارش خطای سیستم ثبت می‌شود:
   `error_log( 'Carmilla Bridge Legacy Alias Usage: ' . $match );`
2. هدرهای رسمی منسوخ‌سازی به کلاینت بازگردانده می‌شوند:
   - `X-Carmilla-Deprecated: root-alias`
   - `Warning: 299 - "Legacy root alias is deprecated and will be removed in the next client cycle"`
3. هدایت به مسیر استاندارد وردپرس از طریق `rest_do_request` بدون دور زدن گیت‌های امنیتی:
   `$GLOBALS['wp']->query_vars['rest_route'] = '/' . CB_REST_NAMESPACE . $uri;`

---

## ۳. نتایج اجرای پروب‌ها در محیط زنده Docker (WordPress + MariaDB)

محیط وردپرس واقعی با کانتینرهای `test-env-wordpress-test-1` و `test-env-db-1` بالا آورده شده و ۴ فایل پروب امنیتی اختصاصی HTTP مستقیماً اجرا شدند:

### الف) پروب جامع برابری و احراز هویت (Auth & Parity Probe)
- **فایل**: `wordpress/carmilla-bridge/tests/probe-root-alias-auth-http-real-wp.php`
- **فرمان**:
  ```bash
  docker exec -e CARMILLA_LOGIN_DISPOSABLE=yes test-env-wordpress-test-1 php /var/www/html/wp-content/plugins/carmilla-bridge/tests/probe-root-alias-auth-http-real-wp.php
  ```
- **خروجی**:
  - ۳۲ سناریوی مختلف شامل خواندن و نوشتن پروفایل، آدرس‌ها، علاقه‌مندی‌ها و بازدیدهای اخیر روی ۴ سطح احراز هویت (`anonymous`، `cookie_no_nonce`، `cookie_nonce`، `bearer`) اجرا شد.
  - نتایج: **۳۲ PASS از ۳۲ مورد** — تطابق کامل رفتار مسیر alias با مسیر canonical.
  - رد کامل درخواست‌های فاقد احراز هویت یا فاقد نانس (۴۰۱)، عدم تغییر داده و zero-bypass.
  - `Transport failures: 0; parity/auth findings: 0; cleanup=1` (کد خروجی ۰).

### ب) پروب رد توکن بیگانه/سایر ارائه‌دهندگان (Foreign Token Probe)
- **فایل**: `wordpress/carmilla-bridge/tests/probe-root-alias-foreign-token-http-real-wp.php`
- **فرمان**:
  ```bash
  docker exec -e CARMILLA_LOGIN_DISPOSABLE=yes test-env-wordpress-test-1 php /var/www/html/wp-content/plugins/carmilla-bridge/tests/probe-root-alias-foreign-token-http-real-wp.php
  ```
- **خروجی**:
  - `PASS root_get_foreign_issuer_denied`
  - `PASS root_post_foreign_issuer_denied`
  - `PASS canonical_get_foreign_issuer_denied`
  - `PASS canonical_post_foreign_issuer_denied`
  - `Failures: 0; cleanup=1` (کد خروجی ۰).

### ج) پروب ایزولاسیون بین کاربران (User Isolation Probe)
- **فایل**: `wordpress/carmilla-bridge/tests/probe-root-alias-isolation-http-real-wp.php`
- **فرمان**:
  ```bash
  docker exec -e CARMILLA_LOGIN_DISPOSABLE=yes test-env-wordpress-test-1 php /var/www/html/wp-content/plugins/carmilla-bridge/tests/probe-root-alias-isolation-http-real-wp.php
  ```
- **خروجی**:
  - ۱۸ بررسی دقیق شامل خواندن/نوشتن آدرس کاربر دیگر (۴۰۴)، استقلال لیست علاقه‌مندی‌ها و عدم تغییر اطلاعات یک کاربر با توکن دیگری.
  - تمامی ۱۸ مورد `PASS`.
  - `Failures: 0; cleanup=1` (کد خروجی ۰).

### د) پروب ورودی‌های نامعتبر و مخرب (Malformed Inputs Probe)
- **فایل**: `wordpress/carmilla-bridge/tests/probe-root-alias-malformed-http-real-wp.php`
- **فرمان**:
  ```bash
  docker exec -e CARMILLA_LOGIN_DISPOSABLE=yes test-env-wordpress-test-1 php /var/www/html/wp-content/plugins/carmilla-bridge/tests/probe-root-alias-malformed-http-real-wp.php
  ```
- **خروجی**:
  - ۶ سناریوی پارامترهای آرایه‌ای نامعتبر، نانس اشتباه (۴۰۳)، توکن نامعتبر (۴۰۱) و Content-Type نامعتبر.
  - تمامی ۶ مورد `PASS`.
  - هدر `X-Carmilla-Deprecated: root-alias` در همه پاسخ‌های روت با موفقیت بررسی و تایید شد.
  - `Failures: 0; cleanup=1` (کد خروجی ۰).

### هـ) پروب‌های گارد مانیفست و تم
- `smoke-manifest.php`: **ALL PASSED**
- `smoke-manifest-security.php`: **ALL PASSED**
- `smoke-manifest-theme.php`: **ALL PASSED**

---

## ۴. بررسی لاگ‌های Telemetry در آپاچی
لاگ آپاچی (`docker logs test-env-wordpress-test-1`) ثبت منظم موارد استفاده از alias را تایید کرد:
```text
[Sat Nov 14 09:32:09] [php:notice] Carmilla Bridge Legacy Alias Usage: /api/favorites
[Sat Nov 14 09:32:09] [php:notice] Carmilla Bridge Legacy Alias Usage: /api/recently-viewed
[Sat Nov 14 09:32:09] [php:notice] Carmilla Bridge Legacy Alias Usage: /api/users/me
[Sat Nov 14 09:32:10] [php:notice] Carmilla Bridge Legacy Alias Usage: /api/addresses
```

---

## ۵. نتیجه‌گیری و پذیرش
بلوکر پیشین کارت (نبود محیط Docker/PHP و نبود شواهد پروب HTTP و عدم bypass) به‌طور کامل برطرف شد. کلیه معیارهای کارت برآورده شده و تسک در وضعیت **DONE** قرار گرفت.

</div>
