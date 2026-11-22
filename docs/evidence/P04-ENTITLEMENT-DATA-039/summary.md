<div dir="rtl" align="right">

# گزارش شواهد پذیرش Task: P04-ENTITLEMENT-DATA-039

- **شناسه Task:** `P04-ENTITLEMENT-DATA-039`
- **عنوان:** کاتالوگ قابلیت و SKU با تفکیک حق خرید و toggle
- **تاریخ اجرا:** 2026-11-22
- **مجری و بازبین:** Carmilla Agent (Codex)
- **مخزن:** `D:/Android/AndroidStudioProjects/kmp-shop`
- **نوع تسک:** DATA / SPEC / CODE
- **سطح ریسک / اندازه:** P0 / HIGH / M

---

## ۱. اهداف محقق‌شده

1. **قرارداد نسخه‌دار کاتالوگ قابلیت (Schema Version 1):**
   - ایجاد سند قرارداد کامل `docs/contracts/ENTITLEMENT_CATALOG_SPEC_V1.md`.
   - ایجاد اسکیما `docs/contracts/carmilla-entitlement-catalog-v1.schema.json`.
   - ایجاد داده کانونیکال `docs/contracts/carmilla-entitlement-catalog-v1.json`.
2. **پوشش قابلیت‌ها، کنترلرها و ماژول‌ها:**
   - ثبت کلیه شناسه‌های کانونیکال برای دامنه‌های پایه (`theme.base`, `plugin.base`, `content.core`), فروشگاه (`commerce.*`), آموزش (`academy.*`), کلینیک و مشاوره (`clinic.*`), روان‌شناسی (`psych.*`) و اپ‌ساز (`builder.*`).
   - ایجاد شناسه‌های مجزا برای نوبت‌دهی (`clinic.booking`), درمانگران (`clinic.therapists`), مشاوره آنلاین (`clinic.consultation`), و پرونده محرمانه بالینی (`clinic.records`).
3. **تفکیک قطعی نوبت‌دهی از آزمون روان‌شناسی:**
   - اثبات اینکه بسته `sku_clinic_booking` هیچ‌گونه پیش‌نیاز یا وابستگی به `psych.tests` ندارد.
   - بسته روان‌شناسی `sku_psych` مستقل از نوبت‌دهی است.
4. **ایزوله‌سازی بسته پایه (Base Package):**
   - بسته‌های `sku_theme_base` و `sku_plugin_base` فاقد هرگونه vertical تجاری هستند و تمام builderها در آن `false` می‌باشند.
5. **جداسازی کامل اهداف اپ‌ساز (App Builder Targets):**
   - چهار هدف مستقل: `android`, `ios`, `desktop`, `pwa` (به همراه `web`).
   - مقادیر غایب (missing) به طور پیش‌فرض `false` می‌شوند.
   - داشتن مجوز یک هدف (مانند Android) هرگز حق دسترسی به سایر اهداف (مانند iOS یا Desktop) را ایجاد نمی‌کند.
   - فعال بودن حداقل یک هدف به صورت خودکار `builder.core` را الزامی می‌کند.
6. **پشتیبانی کامل از شناسه‌های قدیمی (Legacy Aliases):**
   - نگاشت خودکار شناسه‌های `builder.ui`, `builder.config`, `consultation.core`, `clinic.sessions`, `clinic.psych`, `clinic.private_records` به شناسه‌های رسمی.
7. **سیاست‌های چرخه عمر لایسنس و حفظ داده:**
   - کارکرد آفلاین بدون نیاز به اتصال دائم یا پینگ اجباری.
   - مهلت ۱۴ روزه (`grace_period`) برای لایسنس‌های منقضی‌شده با حفظ کارکرد و اعلان به مدیر.
   - ابطال لایسنس (`revoked`) عملیات تجاری را متوقف می‌کند، اما هرگز داده‌های سایت را پاک نمی‌کند.
   - انتقال دامنه (`siteTransfer`) از طریق توکن انتقال.

---

## ۲. دستورات اجراشده و نتایج (Commands & Exit Codes)

### الف) اجرای آزمون‌های پذیرش اختصاصی (Acceptance Test Suite)
```bash
docker run --rm -v "${PWD}:/app" -w /app php:8.1-cli php wordpress/packages/carmilla-core/tests/test-entitlement-data-039.php
```
- **Exit Code:** 0
- **نتیجه:** تمام ۳۵ تست اختصاصی PASS شدند:
  - آزمون اسکیمای JSON و productKinds
  - آزمون ایزوله‌سازی بسته Base بدون vertical
  - آزمون استقلال نوبت‌دهی بدون روان‌شناسی
  - آزمون اهداف ۴گانه اپ‌ساز و عدم دسترسی متقابل
  - آزمون نگاشت Aliasهای قدیمی
  - آزمون محاسبه وابستگی‌های ترانزیتی
  - آزمون وضعیت‌های Grace (14 روز)، Revoked، Site Transfer و نگاشت Artifact Inventory

### ب) اجرای آزمون Smoke برای تضمین سازگاری گذشته‌نگر (Backward Compatibility)
```bash
docker run --rm -v "${PWD}:/app" -w /app php:8.1-cli php wordpress/packages/carmilla-core/tests/smoke-carmilla-core.php
```
- **Exit Code:** 0
- **نتیجه:** ALL SMOKE TESTS PASSED (0 failures)

### ج) آزمون سلامت ترکیب داکر (Docker Compose Config)
```bash
docker compose -f tools/test-env/docker-compose.yml config
```
- **Exit Code:** 0

### د) بررسی سینتکس PHP (Lint)
```bash
docker run --rm -v "${PWD}:/app" -w /app php:8.1-cli sh -c "php -l wordpress/packages/carmilla-core/inc/Catalog/EntitlementSchema.php && php -l wordpress/packages/carmilla-core/inc/Catalog/EntitlementClaims.php && php -l wordpress/packages/carmilla-core/inc/Catalog/DependencyResolver.php && php -l wordpress/packages/carmilla-core/inc/Entitlement/EntitlementResolver.php && php -l wordpress/packages/carmilla-core/tests/test-entitlement-data-039.php"
```
- **Exit Code:** 0 (No syntax errors detected)

### ه) بررسی خطوط سفید و تداخل گیت (Git Diff Check)
```bash
git diff --check
```
- **Exit Code:** 0

---

## ۳. فایل‌های تغییریافته و ایجادشده

- `docs/contracts/carmilla-entitlement-catalog-v1.schema.json` (جدید)
- `docs/contracts/carmilla-entitlement-catalog-v1.json` (جدید)
- `docs/contracts/ENTITLEMENT_CATALOG_SPEC_V1.md` (جدید)
- `wordpress/packages/carmilla-core/inc/Catalog/EntitlementSchema.php` (ویرایش)
- `wordpress/packages/carmilla-core/inc/Catalog/EntitlementClaims.php` (ویرایش)
- `wordpress/packages/carmilla-core/inc/Catalog/DependencyResolver.php` (ویرایش)
- `wordpress/packages/carmilla-core/inc/Entitlement/EntitlementResolver.php` (ویرایش)
- `wordpress/packages/carmilla-core/tests/test-entitlement-data-039.php` (جدید)
- `docs/evidence/P04-ENTITLEMENT-DATA-039/summary.md` (جدید)

---

## ۴. روش بازگشت (Rollback)

با بازگرداندن diff کامیت این تسک یا فایل‌های اضافه شده به وضعیت قبل، سیستم به حالت اولیه بازمی‌گردد بدون اینکه ساختار پایگاه‌داده یا اطلاعات کاربری دستخوش تغییر شود.

</div>
