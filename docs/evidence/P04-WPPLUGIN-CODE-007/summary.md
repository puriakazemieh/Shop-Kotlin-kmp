# Evidence: P04-WPPLUGIN-CODE-007 — اتصال میزبان افزونه به هسته روی قالب‌های دیگر

## مشخصات اجرا
- **Task ID:** `P04-WPPLUGIN-CODE-007`
- **عنوان:** اتصال میزبان افزونه به هسته روی قالب‌های دیگر (Plugin Host Standalone Boot & Theme Switch Parity)
- **فاز/حوزه/نوع:** `P04 / WPPLUGIN / CODE`
- **تاریخ:** ۲۳ نوامبر ۲۰۲۶ (2026-11-23)
- **محیط تست:** وردپرس واقعی (PHP 8.3.35, Apache, MariaDB 10.6.28) در کانتینر `test-env-wordpress-test-1`
- **قالب‌های آزموده شده:**
  1. `carmilla-theme` (پوسته مستقل اختصاصی کارمیلا — حالت هم‌زمانی Co-install)
  2. `twentytwentyfour` (قالب پیش‌فرض مدرن بلوکی وردپرس)
  3. `storefront` نسخه 4.6.2 (قالب کلاسیک استاندارد ووکامرس)

---

## ۱. اهداف و معیارهای پذیرش
- [x] **وابستگی bootstrap به Carmilla Theme صفر است:** افزونه `carmilla-bridge` به طور کاملاً مستقل با قالب‌های ثالث و پیش‌فرض وردپرس لود شده و هیچ وابستگی یا خطای Fatal به پوسته Carmilla ندارد.
- [x] **API و پیشخوان پایه روی هر دو قالب آزموده شده است:** اندپوینت‌های REST API شامل `/carmilla/v1/client-manifest`، `/carmilla/v1/api/auth/login` و `/carmilla/v1/api/products` و پیشخوان مدیریت افزونه روی هر دو قالب پیش‌فرض (`twentytwentyfour`) و فروشگاهی (`storefront`) با موفقیت فراخوانی و اعتبارسنجی شدند.
- [x] **عدم ادعای renderer عمومی:** تایید شد که این کارت صرفاً اتصال میزبان افزونه و استقلال هسته را تضمین می‌کند و رندرر عمومی فرانت‌اند به کارت اختصاصی `P04-WPPLUGIN-CODE-045` تعلق دارد.
- [x] **حفظ داده‌های پایه هنگام تعویض قالب (Theme Switch):** داده‌های آزمایشی، تنظیمات مانیفست (`cb_manifest_features`) و ادعاهای لایسنس (`carmilla_license_claims`) در چرخه تعویض مکرر قالب‌ها کاملاً پایدار باقی ماندند.

---

## ۲. تغییرات اعمال‌شده در کدها
1. **بوت مستقل و مقاوم در `CB_Standalone_Boot` (`class-cb-standalone-boot.php`):**
   - اتصال مستقیم به هسته مشترک در متد `CB_Standalone_Boot::boot()`: تضمین اجرای `\Carmilla_Kernel::boot('plugin', '1.0.0')` هنگام لود افزونه در غیاب پوسته.
   - بارگذاری استاب‌ها و پلی‌فیل‌های توابع پوسته فقط در زمان غیرفعال بودن پوسته کارمیلا (`if ( ! $is_theme_active )`) جهت جلوگیری از تداخل بازتعریف (Redeclare collision).
   - افزودن توابع کمکی پلی‌فیل برای `carmilla_format_number` و `carmilla_manifest_features`.
2. **بررسی امن بازتعریف تابع در پوسته (`customizer.php`):**
   - افزودن محافظ `if ( ! function_exists( 'carmilla_manifest_features' ) )` در پوسته کارمیلا جهت تضمین سازگاری در زمان تعویض پویای قالب در رانتایم.
3. **تصحیح حل خودکار قابلیت‌های پیش‌فرض در کنترلر مانیفست (`class-cb-manifest-controller.php` & `CapabilityResolver.php`):**
   - در صورت خالی بودن تنظیمات ادمین (`cb_manifest_features` تنظیم‌نشده)، قابلیت‌های دارای لایسنس (مانند `commerce.core`) به طور پیش‌فرض فعال شده و با خطای نادرست مسدود نمی‌شوند مگر اینکه مدیر صراحتاً آن‌ها را خاموش کند.

---

## ۳. دستورات و نتایج آزمون‌های واقعی

### ۳.۱. اجرای سوئیت اختصاصی مستقل افزونه (`test-plugin-host-standalone-007.php`):
دستور:
```bash
docker exec test-env-wordpress-test-1 php /var/www/html/wp-content/plugins/carmilla-bridge/tests/test-plugin-host-standalone-007.php
```
نتیجه:
```text
====================================================================
  P04-WPPLUGIN-CODE-007: Plugin Host Standalone & Theme Switch QA   
====================================================================

Group 1: Baseline Co-Install Verification
  [PASS] WordPress active theme detected (carmilla-theme)
  [PASS] Carmilla Bridge plugin is active
  [PASS] Carmilla_Kernel class exists
  [PASS] CB_Standalone_Boot class exists

Group 2: Ensure Storefront Theme Availability
  [PASS] Twenty Twenty-Four theme is installed
  [PASS] Storefront theme is installed and available

Group 3: Standalone Plugin on Twenty Twenty-Four (Block Theme)
  [PASS] Active theme successfully switched to Twenty Twenty-Four
  [PASS] CB_Standalone_Boot correctly detects Carmilla Theme is NOT active
  [PASS] carmilla_is_theme_active() returns false
  [PASS] Polyfill carmilla_get_option exists
  [PASS] Polyfill carmilla_theme_asset exists
  [PASS] carmilla_theme_asset returns empty string safely
  [PASS] Polyfill carmilla_feature_enabled exists
  [PASS] carmilla_feature_enabled returns boolean/valid state
  [PASS] Polyfill carmilla_price exists
  [PASS] carmilla_price formats with تومان
  [PASS] Polyfill carmilla_format_number exists
  [PASS] carmilla_format_number formats with Persian digits
  [PASS] Polyfill carmilla_manifest_features exists
  [PASS] carmilla_manifest_features returns array
  [PASS] Post type cb_story is registered
  [PASS] Post type cb_course is registered
  [PASS] Post type cb_therapist is registered
  [PASS] Post type cb_psychtest is registered
  [PASS] TT4: /carmilla/v1/client-manifest returns HTTP 200 OK (got 200)
  [PASS] TT4: client-manifest contains features map
  [PASS] TT4: plugin.base feature flag is true
  [PASS] TT4: /api/auth/login route is registered and rejects empty credentials with 400 (got 400)
  [PASS] TT4: Auth response adheres to Carmilla API error contract
  [PASS] TT4: /api/products catalog returns HTTP 200 OK (got 200)
  [PASS] TT4: Front-end homepage returns HTTP 200 OK (got 200)
  [PASS] TT4: Front-end homepage contains zero PHP fatal errors

Group 4: Standalone Plugin on Storefront (WooCommerce Classic Theme)
  [PASS] Active theme successfully switched to Storefront
  [PASS] Storefront: Carmilla Theme correctly reported as NOT active
  [PASS] Storefront: carmilla_is_theme_active() returns false
  [PASS] Storefront: /carmilla/v1/client-manifest returns HTTP 200 OK
  [PASS] Storefront: /api/auth/login works identically on Storefront (got 400)
  [PASS] Storefront: /api/products returns HTTP 200 OK
  [PASS] Storefront: Front-end homepage returns HTTP 200 OK (got 200)
  [PASS] Storefront: Front-end homepage contains zero PHP fatal errors
  [PASS] Storefront: Theme assets and markup are rendered

Group 5: Data Preservation across Theme Switches
  [PASS] Data intact after switching to Twenty Twenty-Four
  [PASS] Data intact after switching to Storefront
  [PASS] License claims option remains undisturbed

Group 6: Restoration to Co-Install Mode (Carmilla Theme)
  [PASS] Restored active theme to carmilla-theme
  [PASS] Co-install: Carmilla Theme reported as active
  [PASS] Co-install: carmilla_is_theme_active() returns true
  [PASS] Co-install: client-manifest returns HTTP 200 OK
  [PASS] Co-install: Homepage returns HTTP 200 OK
  [PASS] Co-install: Carmilla tokens stylesheet enqueued
  [PASS] Co-install: Zero PHP fatal errors

Group 7: Scope Gate Verification
  [PASS] Task P04-WPPLUGIN-CODE-007 satisfies Plugin Host bootstrap & adapter independence
  [PASS] Public shortcode/block renderer is strictly scoped to future task P04-WPPLUGIN-CODE-045

--------------------------------------------------------------------
Results: Total: 53 | Passed: 53 | Failed: 0
--------------------------------------------------------------------
```

### ۳.۲. تست جامع فاز ۰۴ وردپرس واقعی (`verify-all-phase04-real-wp.php`):
دستور:
```bash
docker exec test-env-wordpress-test-1 php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/verify-all-phase04-real-wp.php
```
نتیجه:
```text
ALL 78 MASTER PHASE 04 REAL WORDPRESS CHECKS PASSED! (0 failures)
```

### ۳.۳. تست زنده HTTP بر روی پورت 8080 سرور واقعی:
- احراز هویت ورود:
  `curl.exe -s -i -X POST http://localhost:8080/wp-json/carmilla/v1/api/auth/login -H "Content-Type: application/json" -d "{}"`
  پاسخ: **HTTP 400 Bad Request** همراه با خطای اعتبارسنجی فارسی: `{"message":"نام کاربری و رمز عبور الزامی است.","status":400,"code":400,"errorCode":"VALIDATION"}`
- کاتالوگ محصولات:
  `curl.exe -s -i http://localhost:8080/wp-json/carmilla/v1/api/products`
  پاسخ: **HTTP 200 OK** با ساختار Page DTO کاتالوگ محصولات ووکامرس.

---

## ۴. نتیجه‌گیری و وضعیت نهایی
تسک `P04-WPPLUGIN-CODE-007` کلیه سناریوهای پذیرش فنی را با موفقیت در محیط واقعی پشت سر گذاشت و استقلال کامل افزونه بر روی قالب‌های دیگر اثبات گردید.
- **وضعیت:** `DONE`
