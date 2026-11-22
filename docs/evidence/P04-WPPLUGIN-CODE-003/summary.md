<div dir="rtl" align="right">

# گزارش شواهد پذیرش Task: P04-WPPLUGIN-CODE-003

- **شناسه Task:** `P04-WPPLUGIN-CODE-003`
- **عنوان:** نسخه schema و runner مهاجرت مشترک و قابل ادامه
- **تاریخ:** 2026-11-22
- **مجری و بازبین:** Carmilla Agent (AI)
- **مخزن:** `D:/Android/AndroidStudioProjects/kmp-shop`
- **نوع تسک:** WPPLUGIN / CODE
- **سطح اولویت / ریسک / اندازه:** P0 / HIGH / M
- **مرجع نیازمندی:** Master checklist row P04-WPPLUGIN-CODE-003، [ADR-007](../../architecture/adr/ADR-007-SHARED-KERNEL-HOST-BOOTSTRAP.md) و [تعریف محصولات مستقل](../../INDEPENDENT_PRODUCTS_SPEC_FA.md).

---

## ۱. اهداف و معیارهای اختصاصی محقق‌شده

1. **پیاده‌سازی یکپارچه رانر مهاجرت در هسته مشترک (Single Kernel Implementation):**
   - پیاده‌سازی کلاس `Carmilla\Core\Migration\SchemaRunner` در مسیر `wordpress/packages/carmilla-core/inc/Migration/SchemaRunner.php`.
   - رانر تنها یک پیاده‌سازی واحد در کل سیستم دارد و هر دو میزبان Theme و Plugin از همان نمونه استفاده می‌کنند.
2. **قفل توزیع‌شده و محافظت در برابر هم‌زمانی (Distributed Locking & Concurrency Guard):**
   - متدهای `acquire_lock` و `release_lock` با شناسه یکتای اجرا (`lock_token`) برای جلوگیری از تداخل اجرای دو پروسس هم‌زمان.
   - بازیابی خودکار قفل‌های مرده (Stale Lock Recovery) در صورت سپری شدن بیش از ۶۰ ثانیه از ایجاد قفل پیشین.
3. **چک‌پوینت‌های پایدار و مهاجرت قابل ادامه (Resumable Step Checkpoints):**
   - ثبت چک‌پوینت هر مرحله در آپشن `carmilla_schema_checkpoints`.
   - متد `run_resumable_step` مراحل از پیش انجام‌شده را رد می‌کند و صرفاً مراحل باقی‌مانده یا شکست‌خورده را اجرا می‌کند.
4. **اثبات بازیابی پس از شکست با مهاجرت ساختگی (Synthetic Migration Recovery with Zero Duplication):**
   - مهاجرت نسخه `1.0.3` شامل سه مرحله ساخت جدول، درج ردیف Alpha و درج ردیف Beta است.
   - با تزریق خطای مصنوعی در مرحله سوم، اجرای اول با خطا متوقف شد؛ آپشن نسخه در `1.0.2` باقی ماند و قفل آزاد شد.
   - در اجرای مجدد بدون خطا، مراحل ۱ و ۲ رد شدند و مرحله ۳ با موفقیت پایان یافت؛ نسخه اسکیما به `1.0.3` رسید و تعداد رکوردهای جدول دقیقاً ۲ رکورد باقی ماند (عدم تولید رکورد تکراری).
5. **ممنوعیت قطعی بازگشت ناسازگار به عقب (Incompatible Downgrade Prevention):**
   - در صورت بالاتر بودن نسخه موجود پایگاه‌داده نسبت به رانر (مثلاً دیتابیس `2.0.0` و رانر `1.0.3`)، درخواست مهاجرت با وضعیت `rejected` و خطای `incompatible_downgrade_rejected` رد می‌شود و هیچ تغییری در دیتابیس اعمال نمی‌گردد (Fail-Closed).
6. **عدم اجرای مجدد در نصب هم‌زمان (Co-Install Idempotency):**
   - ثبت چک‌پوینت در حافظه برای چرخه جاری درخواست؛ لود دومین میزبان (پوسته پس از افزونه یا برعکس) مهاجرت را نادیده گرفته و با وضعیت `skipped` خارج می‌شود.

---

## ۲. دستورات اجراشده و نتایج (Commands & Exit Codes)

### الف) آزمون اختصاصی رانر مهاجرت
```bash
docker run --rm -v "${PWD}:/app" -w /app php:8.1-cli php wordpress/packages/carmilla-core/tests/test-schema-runner-003.php
```
- **Exit Code:** 0
- **نتیجه:** ۲۵ assertion موفق، ۰ خطا:
  - نصب تمیز از نسخه 0.0.0
  - اجرای بدون عملیات مجدد (noop) روی نسخه به‌روز
  - عدم اجرای مجدد در یک چرخه درخواست (co-install)
  - رد قطعی downgrade ناسازگار
  - عملکرد قفل توزیع‌شده و بازیافت قفل منقضی‌شده
  - ادامه موفقیت‌آمیز پس از خطای مصنوعی مرحله ۳ و عدم تکرار رکوردهای Alpha

### ب) اجرای تمام آزمون‌های همبسته هسته مشترک
```bash
docker run --rm -v "${PWD}:/app" -w /app php:8.1-cli sh -c "php wordpress/packages/carmilla-core/tests/test-schema-runner-003.php && php wordpress/packages/carmilla-core/tests/test-entitlement-data-039.php && php wordpress/packages/carmilla-core/tests/smoke-carmilla-core.php"
```
- **Exit Code:** 0
- **نتیجه:** تمام سوئیت‌های تست کاتالوگ 039، اسموک هسته و رانر مهاجرت 003 پاس شدند.

### ج) آزمون سلامت ترکیب داکر (Docker Compose Config)
```bash
docker compose -f tools/test-env/docker-compose.yml config
```
- **Exit Code:** 0

### د) بررسی سینتکس PHP (Lint)
```bash
docker run --rm -v "${PWD}:/app" -w /app php:8.1-cli sh -c "php -l wordpress/packages/carmilla-core/inc/Migration/SchemaRunner.php && php -l wordpress/packages/carmilla-core/tests/test-schema-runner-003.php"
```
- **Exit Code:** 0 (No syntax errors detected)

### ه) بررسی تداخلات گیت (Git Diff Check)
```bash
git diff --check
```
- **Exit Code:** 0

---

## ۳. فایل‌های تغییریافته و ایجادشده

- `wordpress/packages/carmilla-core/inc/Migration/SchemaRunner.php` (ویرایش: پیاده‌سازی قفل، چک‌پوینت‌های مرحله‌ای، محافظت از downgrade، مهاجرت مصنوعی 1.0.3)
- `wordpress/packages/carmilla-core/tests/test-schema-runner-003.php` (جدید: سوئیت آزمون جامع ۲۵ مرحله‌ای)
- `docs/evidence/P04-WPPLUGIN-CODE-003/summary.md` (جدید: گزارش شواهد پذیرش)
- `docs/tasks/P04-WPPLUGIN-CODE-003.md` (به‌روزرسانی وضعیت و سابقه تکمیل)
- `docs/tasks.md` (به‌روزرسانی صف: 003 به DONE و 004 به READY)
- `docs/MASTER_IMPLEMENTATION_CHECKLIST_FA.md` (علامت‌گذاری ردیف P04-WPPLUGIN-CODE-003)

---

## ۴. روش بازگشت (Rollback)

با بازگرداندن diff همین کامیت، رانر به نسخه قبلی بازمی‌گردد. از آنجا که مهاجرت‌ها فاقد تغییرات تخریبی schema هستند و صرفاً جدول مصنوعی برای تست اضافه شده، هیچ آسیبی به داده‌های پایگاه‌داده وارد نمی‌شود.

</div>
