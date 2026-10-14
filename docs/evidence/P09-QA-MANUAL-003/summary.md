# Manual QA Evidence Handoff / تحویل شواهد تست دستی: P09-QA-MANUAL-003

- **Task ID / شناسه تسک:** P09-QA-MANUAL-003
- **Date / تاریخ:** 2026-09-06
- **Title / عنوان:** نصب تمیز، ارتقا و بازگشت (Rollback) روی ماتریس WP/PHP/Woo

## Test Instructions for User / دستورالعمل تست برای کاربر
### کجا نگاه کنید
- پیشخوان وردپرس بخش افزونه‌ها (`/wp-admin/plugins.php`) یا پوسته‌ها (`/wp-admin/themes.php`)
- تنظیمات ووکامرس بخش Advanced -> Features (وضعیت HPOS)
- لاگ‌های وردپرس (`wp-content/debug.log`)

### مراحل تست
1. **ماتریس نصب تمیز:** فایل ZIP افزونه/پوسته را روی وردپرس خام (نسخه‌های ۶.۴ تا ۶.۶) با PHP 8.1 تا 8.3 نصب و فعال کنید.
2. **تست ارتقا:** نسخه قبلی را نصب کرده، چند سفارش بسازید، سپس نسخه جدید را آپلود کرده و صحت اجرای migrationها را تایید کنید.
3. **تست بازگشت:** نسخه را دانگرید کنید و عدم تخریب پایگاه داده را تایید نمایید.
4. **سازگاری HPOS و بلوک‌ها:** حالت HPOS و بلوک‌های تسویه‌حساب گوتنبرگ را روشن کرده و تست کنید.

## Status / وضعیت
- **Final Status / وضعیت نهایی:** AWAITING_MANUAL_QA (در انتظار تست دستی)

## اجرای قابل‌بازگشت در محیط QA — 2026-10-14

### محیط

- نصب مقصد پایدار: `D:\Carmilla-QA\p07-wordpress-destination`
- WordPress `7.1.3`، PHP `8.2.12`
- حالت اولیه و نهایی: قالب Carmilla فعال و افزونهٔ Carmilla Bridge غیرفعال

### نتیجهٔ اجراشده

- ZIPهای `carmilla-bridge.zip` و `carmilla-theme.zip` با شمارش 109 و 141 فایل
  به‌ترتیب، خوانده شدند و ساختار ZIP معتبر بود.
- lint فایل bootstrap افزونه و `functions.php` قالب در نصب مقصد موفق بود.
- افزونه از وضعیت غیرفعال فعال شد؛ در یک boot تازه، namespace `carmilla/v1`
  ثبت و 118 route آن قابل مشاهده بود.
- smoke واقعی migration فاز 07 با دادهٔ synthetic پاس شد: درج، mapping، restore
  parent، rewrite URL، حذف metadata حساس و idempotent re-run همگی PASS بودند.
- در پایان افزونه دوباره غیرفعال شد؛ دادهٔ واقعی یا پرداخت ایجاد نشد.

### محدودیت باقی‌مانده

این فقط یک اجرای بازگشت‌پذیر روی یک ترکیب است و معیار `minimum/current/mid +
HPOS/Blocks` را پوشش نمی‌دهد. در نصب اصلی QA، WooCommerce `11.2.1` با HPOS
روشن و بستهٔ Blocks در دسترس است؛ ولی اجرای clean-install/upgrade/rollback روی
چند نسخهٔ پشتیبانی‌شدهٔ WordPress/PHP/WooCommerce همچنان تأیید انسانی/محیط جدا
می‌خواهد. وضعیت کارت عمداً `AWAITING_MANUAL_QA` باقی می‌ماند.

## حداقل PHP با Docker — 2026-10-14

- Docker Engine `29.2.1` در دسترس بود.
- `docker compose -f tools/test-env/docker-compose.yml run --rm php-test php -v`
  روی PHP `8.1.34` با exit code `0` اجرا شد.
- تمام فایل‌های PHP در `wordpress/carmilla-bridge` و
  `wordpress/carmilla-theme` با همان image و `php -l` بررسی شدند:
  `PHP_LINT_PASS`، exit code `0`.

این نتیجه فقط baseline سازگاریِ syntax در minimum PHP است و جایگزین clean
install/upgrade/rollback واقعی WordPress/WooCommerce یا تأیید انسانی نیست.
