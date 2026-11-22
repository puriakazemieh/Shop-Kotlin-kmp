<div dir="rtl" align="right">

# گزارش شواهد پذیرش Task: P04-WPPLUGIN-CODE-004

- **شناسه Task:** `P04-WPPLUGIN-CODE-004`
- **عنوان:** bootstrap اولیه و بسته‌بندی هسته در هر دو ZIP
- **تاریخ:** 2026-11-22
- **مجری و بازبین:** Carmilla Agent (AI)
- **مخزن:** `D:/Android/AndroidStudioProjects/kmp-shop`
- **نوع تسک:** WPPLUGIN / CODE
- **سطح اولویت / ریسک / اندازه:** P0 / HIGH / M
- **مرجع نیازمندی:** Master checklist row P04-WPPLUGIN-CODE-004، [ADR-007](../../architecture/adr/ADR-007-SHARED-KERNEL-HOST-BOOTSTRAP.md) و [تعریف محصولات مستقل](../../INDEPENDENT_PRODUCTS_SPEC_FA.md).

---

## ۱. اهداف و معیارهای اختصاصی محقق‌شده

1. **بسته‌بندی هسته مشترک در هر دو بسته ZIP (Dual ZIP Shared Kernel Packaging):**
   - اسکریپت‌های `wordpress/build-theme-zip.sh` و `wordpress/build-bridge-zip.sh` به صورت ایزوله اجرا شدند.
   - هر دو فایل خروجی `carmilla-theme.zip` و `carmilla-bridge.zip` هسته یکسان `Carmilla_Kernel.php` را از منبع واحد `wordpress/packages/carmilla-core` در ساختار داخلی خود بسته‌بندی کردند.
2. **بوت کاملاً مستقل در حالت Theme-only:**
   - پوسته Carmilla بدون نیاز به حضور افزونه Carmilla Bridge و بدون نصب هیچ‌گونه Companion Plugin یا MU-Plugin، هسته داخلی خود را بارگذاری و اجرا می‌کند (`Carmilla_Kernel::boot('theme', '0.8.0')`).
   - وضعیت `is_booted_by('theme')` برابر با `true` و `is_booted_by('plugin')` برابر با `false` است.
3. **بوت کاملاً مستقل در حالت Plugin-only:**
   - افزونه Carmilla Bridge روی قالب‌های استاندارد دیگر بدون حضور پوسته Carmilla، هسته مشترک را بارگذاری و اجرا می‌کند (`Carmilla_Kernel::boot('plugin', '1.0.0')`).
   - وضعیت `is_booted_by('plugin')` برابر با `true` و `is_booted_by('theme')` برابر با `false` است.
4. **اجرای تک‌باره و بدون تداخل در حالت نصب هم‌زمان (Co-Install Single Boot with Zero Collision):**
   - در حالت نصب هم‌زمان، افزونه در `plugins_loaded` و پوسته در `after_setup_theme` فرآیند بوت را صدا می‌زنند.
   - با مکانیزم چک‌پوینت درون‌حافظه‌ای (`boot_kernel`) هسته مشترک فقط یک‌بار مقداردهی اولیه (`init_core`) می‌شود و هیچ‌گونه اکشن یا قلاب تکراری ثبت نمی‌گردد.
   - هر دو شناسه محصول و نسخه در کانتکست‌های ثبتی ذخیره می‌شوند (`['plugin' => '1.0.0', 'theme' => '0.8.0']`).
5. **مسیرهای بارگذاری امن هاست‌ها:**
   - افزودن مسیر جایگزین برای محیط توسعه در [functions.php](file:///d:/Android/AndroidStudioProjects/kmp-shop/wordpress/carmilla-theme/functions.php) که هم در حالت بسته ZIP و هم در ساختار توسعه مخزن هسته را شناسایی و بارگذاری می‌کند.

---

## ۲. دستورات اجراشده و نتایج (Commands & Exit Codes)

### الف) ساخت بسته‌های ZIP در محیط کانتینر ایزوله
```bash
docker run --rm -v "${PWD}:/app" -w /app/wordpress alpine sh -c "apk add --no-cache bash zip && bash build-theme-zip.sh && bash build-bridge-zip.sh"
```
- **Exit Code:** 0
- **نتیجه:** هر دو فایل `carmilla-theme.zip` و `carmilla-bridge.zip` با ساختار معتبر ساخته شدند.

### ب) بررسی محتوای بسته‌های ZIP
```bash
docker run --rm -v "${PWD}:/app" alpine sh -c "unzip -l /app/wordpress/carmilla-theme.zip | grep Carmilla_Kernel.php; unzip -l /app/wordpress/carmilla-bridge.zip | grep Carmilla_Kernel.php"
```
- **Exit Code:** 0
- **نتیجه:** فایل هسته `Carmilla_Kernel.php` با سایز دقیق یکسان (3233 بایت) در هر دو آرشیو تایید شد.

### ج) آزمون اختصاصی بوت هسته
```bash
docker run --rm -v "${PWD}:/app" -w /app php:8.1-cli php wordpress/packages/carmilla-core/tests/test-kernel-bootstrap-004.php
```
- **Exit Code:** 0
- **نتیجه:** تمام ۲۰ assertion با موفقیت PASS شدند:
  - وجود اسکریپت‌ها و عدم وجود companion plugin
  - راه‌اندازی تمیز در حالت Theme-only
  - راه‌اندازی تمیز در حالت Plugin-only
  - تست نصب هم‌زمان و تضمین عدم تکرار اکشن‌ها
  - سازگاری لودرها در هر دو میزبان

### د) اجرای همبسته تمام آزمون‌های هسته و دیتابیس
```bash
docker run --rm -v "${PWD}:/app" -w /app php:8.1-cli sh -c "php wordpress/packages/carmilla-core/tests/test-kernel-bootstrap-004.php && php wordpress/packages/carmilla-core/tests/test-schema-runner-003.php && php wordpress/packages/carmilla-core/tests/test-entitlement-data-039.php && php wordpress/packages/carmilla-core/tests/smoke-carmilla-core.php"
```
- **Exit Code:** 0 (ALL PASSED)

### ه) بررسی کانفیگ داکر، سینتکس PHP و تداخلات گیت
```bash
docker compose -f tools/test-env/docker-compose.yml config  # exit 0
docker run --rm -v "${PWD}:/app" -w /app php:8.1-cli sh -c "php -l ..."  # exit 0
git diff --check  # exit 0
```

---

## ۳. فایل‌های تغییریافته و ایجادشده

- `wordpress/carmilla-theme/functions.php` (ویرایش: تکمیل مسیر لودر هسته با مسیر fallback محیط توسعه)
- `wordpress/packages/carmilla-core/Carmilla_Kernel.php` (ویرایش: افزودن متدهای کمکی `is_booted`, `is_booted_by`, `reset_state`)
- `wordpress/packages/carmilla-core/tests/test-kernel-bootstrap-004.php` (جدید: سوئیت آزمون جامع بوت هسته)
- `docs/evidence/P04-WPPLUGIN-CODE-004/summary.md` (جدید: گزارش شواهد پذیرش)
- `docs/tasks/P04-WPPLUGIN-CODE-004.md` (به‌روزرسانی وضعیت و سابقه تکمیل)
- `docs/tasks.md` (به‌روزرسانی صف: 004 به DONE و 005 به READY)
- `docs/MASTER_IMPLEMENTATION_CHECKLIST_FA.md` (علامت‌گذاری ردیف P04-WPPLUGIN-CODE-004)

---

## ۴. روش بازگشت (Rollback)

با بازگرداندن diff همین کامیت، لودرها و متدهای کمکی هسته به وضعیت قبل بازمی‌گردند بدون اینکه هیچ‌گونه تغییر مخربی در دیتابیس یا فایل‌های تولیدی رخ دهد.

</div>
