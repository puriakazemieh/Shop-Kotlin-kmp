# Evidence: P04-WPTHEME-CODE-005 — اتصال میزبان پوسته به هسته داخلی بدون Bridge

## ۱. مشخصات Task و هدف
- **Task ID:** P04-WPTHEME-CODE-005
- **عنوان:** اتصال میزبان پوسته به هسته داخلی بدون Bridge
- **مسئول اجرا / تأیید:** AI / BOTH
- **وضعیت نهایی:** DONE
- **هدف:** اتصال لودر، دسترسی تنظیمات و آداپتر پایه پوسته به `Carmilla_Kernel` بسته‌بندی‌شده داخلی؛ حذف وابستگی به کلاس‌ها و مسیر نصب Carmilla Bridge در این مرز؛ جداسازی کامل تنظیمات ظاهر (Theme-owned) از وضعیت قابلیت‌ها (Site-owned)؛ و تحقق برابری (Parity) فیچر پایه در نصب مستقل و هم‌زمان.

---

## ۲. تغییرات پیاده‌سازی‌شده (Implementation Summary)

### ۲.۱ کلاس آداپتر میزبان پوسته (`ThemeHostAdapter`)
- ایجاد فایل `wordpress/carmilla-theme/inc/class-theme-host-adapter.php`:
  - متد `is_standalone()`: اعلام اجرای مستقل پوسته بدون حضور افزونه Carmilla Bridge.
  - متد `has_bridge()`: بررسی وضعیت حضور افزونه Bridge در حالت Co-install از طریق کانتکست‌های Kernel.
  - متد `is_feature_enabled(string $slug)`: استعلام قابلیت‌ها بر اساس مپ اسلاگ‌های پوسته (`shop`, `blog`, `courses`, `clinic`, `psychtests`, `stories`, `banners`) به شناسه‌های کانونیکال کاتالوگ استحقاق و ارجاع مستقیم به `Carmilla_Kernel::is_feature_active()`.
  - متد `get_appearance_settings()`: استخراج انحصاری تنظیمات ظاهر (رنگ‌های برند، متون هیرو، لوگو، جهت RTL) از Theme Mods (`get_theme_mod`). این متد اکیداً فاقد هرگونه کلید کنترلی قابلیت یا لایسنس است.
  - متد `get_capability_status()`: استخراج وضعیت قابلیت‌ها و لایسنس در سطح سایت از `Carmilla_Kernel::get_active_features()`؛ Theme Mods به هیچ عنوان خوانده یا تغییر داده نمی‌شوند.
  - متد `is_vertical_available(string $vertical)`: بررسی هم‌زمان فعال‌بودن قابلیت و نیازمندی‌های محیطی (مانند شرط کلاس `WooCommerce` برای vertical فروشگاه).
  - متد `get_navigation_destinations()`: ساخت مقاصد ناوبری سربرگ/نوار پایین موبایل هماهنگ با عمودی‌های در دسترس.
  - متد `get_cpt_inventory()`: فهرست انواع پست‌های محتوایی مدیریت‌شده در حالت مستقل (`cb_course`, `cb_therapist`, `cb_psychtest`, `cb_course_request`, `cb_story`, `cb_banner`).

### ۲.۲ اتصال بوت‌پواسته به هسته داخلی
- در `wordpress/carmilla-theme/functions.php`:
  - لود زودهنگام آداپتر پوسته `class-theme-host-adapter.php` قبل از سایر اجزا.
  - مقداردهی اولیه `ThemeHostAdapter::init()` پس از فراخوانی `Carmilla_Kernel::boot('theme', CARMILLA_THEME_VERSION)`.
- در `wordpress/carmilla-theme/inc/customizer.php`:
  - هدایت تابع `carmilla_feature_enabled()` به `ThemeHostAdapter::is_feature_enabled()` و در صورت لزوم `Carmilla_Kernel::is_feature_active()`.
  - هدایت تابع `carmilla_manifest_features()` به `Carmilla_Kernel::get_active_features()`.
- در `wordpress/carmilla-theme/inc/messaging-admin.php`:
  - حذف وابستگی انحصاری به کلاس `CB_Integrations_Controller` و استفاده از `Carmilla_Kernel::is_booted_by('plugin')` جهت جلوگیری از تداخل منو در نصب هم‌زمان.
- در `wordpress/packages/carmilla-core/Carmilla_Kernel.php`:
  - افزودن متدهای عمومی `get_active_features()` و `is_feature_active(string $feature_id)`.
  - پاک‌سازی متغیر سراسری `$carmilla_active_features` در متد `reset_state()`.
- در `wordpress/packages/carmilla-core/inc/Catalog/EntitlementSchema.php`:
  - افزودن نام‌های قدیمی مانیفست (`content.blog`, `commerce.physical`, `commerce.digital`, `academy.quiz`, `academy.certificate`, `clinic.messaging`, `wallet`) به `FEATURE_ALIASES` برای سازگاری معکوس بی‌نقص.
- ساخت مجدد پکیج `wordpress/carmilla-theme.zip` شامل `inc/class-theme-host-adapter.php` و `packages/carmilla-core/Carmilla_Kernel.php`.

---

## ۳. نتایج اعتبارسنجی خودکار (Automated Verification)

### ۳.۱ سوئیت آزمون اختصاصی P04-WPTHEME-CODE-005
- **دستور:**
  ```bash
  docker run --rm -v "${PWD}:/app" -w /app php:8.1-cli php wordpress/packages/carmilla-core/tests/test-theme-host-kernel-005.php
  ```
- **نتیجه:** ۵۳ تست با موفقیت پاس شدند (Exit Code: 0).
  - گروه ۱: بوت مستقل پوسته و آداپتر بدون کلاس‌ها و مسیرهای افزونه Bridge.
  - گروه ۲: تفکیک کامل تنظیمات ظاهر (Theme Mods) از وضعیت قابلیت‌ها (Kernel) و عدم دسترسی به Theme Mods برای قابلیت‌ها.
  - گروه ۳: برابری ۱۰۰ درصدی (Parity) فعال‌بودن قابلیت‌های پایه میان حالت Standalone Theme و حالت Co-install با Bridge.
  - گروه ۴: اعتبارسنجی غیرفعال شدن فروشگاه در صورت نبود WooCommerce و فعال بودن محتوا/آکادمی/کلینیک بدون نیاز به Bridge.
  - گروه ۵: صحت کارکرد توابع قالب و ناوبری بدون حضور Bridge.

### ۳.۲ تست‌های رگرسیون
- `test-kernel-bootstrap-004.php`: ۲۰ تست پاس شدند (Exit Code: 0).
- `test-schema-runner-003.php`: ۲۵ تست پاس شدند (Exit Code: 0).
- `test-entitlement-data-039.php`: ۳۵ تست پاس شدند (Exit Code: 0).
- `smoke-manifest-theme.php`: ۵ تست پاس شدند (Exit Code: 0).

---

## ۴. بررسی معیارهای پذیرش (Acceptance Criteria)

- [x] **لودر و آداپتر به مسیر Bridge وابسته نیستند:** پوسته با هسته `packages/carmilla-core` مستقل بوت می‌شود و هیچ مسیر یا کلاسی از Bridge لود نمی‌شود.
- [x] **تنظیمات ظاهر با تنظیمات قابلیت مخلوط نشده است:** ظاهر در Theme Mods و قابلیت‌ها در Kernel/Entitlement ذخیره می‌شوند.
- [x] **نصب مستقل و هم‌زمان فیچر پایه parity دارند:** در هر دو حالت، فیچرهای پایه بدون هیچ مغایرتی فعال هستند.
- [x] **بسته‌بندی ZIP پوسته به‌روز است:** فایل `carmilla-theme.zip` شامل آداپتر و کرنل کامل ساخته شد.

---

## ۵. راهنمای تست دستی در وردپرس (Manual QA Guide)

### ۵.۱ محیط مورد نیاز
- وردپرس ۶.۳+ با PHP 7.4+ در محیط تستی محلی یا داکر.
- بدون فعال‌بودن افزونه Carmilla Bridge.

### ۵.۲ مراحل تست (Steps to Reproduce)
1. فایل `wordpress/carmilla-theme.zip` را در مسیر «نمایش ← پوسته‌ها ← بارگذاری پوسته» نصب و فعال کنید.
2. به بخش «نمایش ← سفارشی‌سازی ← تنظیمات کارمیلا ← رنگ‌های برند» بروید و رنگ‌ها یا متن هیرو را تغییر دهید و ذخیره کنید.
3. صفحه اول سایت (`front-page.php`) را باز کنید و بررسی کنید که استوری‌ها، بنرها و مقالات بدون خطا لود شوند.
4. منوی ادمین «کارمیلا ← پیشخوان» را بررسی کنید و اطمینان حاصل کنید که بدون خطای افزونه Bridge نمایش داده می‌شود.
5. حال افزونه `carmilla-bridge.zip` را نیز نصب و فعال کنید (Co-install)؛ بررسی کنید که هیچ خطای fatal یا تکرار منوها و هوک‌ها رخ ندهد و تنظیمات ظاهری پوسته دست‌نخورده باقی بماند.

### ۵.۳ معیار موفقیت
- پوسته به تنهایی فعال شده و بدون هشدار یا خطای PHP کار می‌کند.
- نبود افزونه Bridge پیامی مبنی بر اجبار به نصب companion plugin تولید نمی‌کند.
- تغییر تنظیمات ظاهری در Customizer روی ظاهر سایت اعمال شده و هیچ تداخلی با فیچرهای فعال ندارد.
