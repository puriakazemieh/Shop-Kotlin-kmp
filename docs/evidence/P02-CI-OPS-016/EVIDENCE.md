# مستندات اثبات (Evidence) برای P02-CI-OPS-016

## اهداف و اقدامات
تسک P02-CI-OPS-016 با هدف «جداسازی کامل گردش کار آرتیفکت‌های انتشار (Release Artifact Workflow) از بیلد دیباگ و تولید چک‌سام، متادیتا، SBOM و سیاست نگهداری طولانی‌مدت (Retention)» انجام شد:

1. **تفکیک گردش کار انتشار و حذف رفتارهای مسامحه‌آمیز (`.github/workflows/release-artifact.yml`):**
   - فراخوانی‌های دیباگ (`assembleDebug`) و نام‌گذاری آرتیفکت به عنوان `android-debug-apks` به طور کامل حذف شدند.
   - خاموش‌سازی خطاها (`|| true`) و شرط‌های مسامحه‌آمیز دسکتاپ و وب حذف شدند؛ گردش کار در صورت فقدان هر یک از خروجی‌های محصول بلافاصله متوقف می‌شود.
   - گیت اعتبارسنجی موجودی آرتیفکت‌های محصول (`Validate Release Product Artifact Inventory`) برای جلوگیری از انتشار متادیتای خالی اضافه شد.
2. **تولید خودکار سند فهرست قطعات نرم‌افزاری (SBOM) با سد بازدارنده بسته خالی:**
   - اسکریپت `tools/ci/generate_sbom.cjs` با پشتیبانی از دو فرمت استاندارد بین‌المللی توسعه داده شد:
     - `sbom.cyclonedx.json` منطبق بر استاندارد CycloneDX 1.5
     - `sbom.spdx.json` منطبق بر استاندارد SPDX 2.3
     - استخراج خودکار ۵۲ وابستگی مستقیم از `gradle/libs.versions.toml` به همراه `purl`
   - ایجاد سد امنیتی که در صورت فقدان فایل‌های محصول در دایرکتوری خروجی، بیلد را بلافاصله با خطا متوقف کرده و اجازه ایجاد متادیتای خالی نمی‌دهد.
3. **تولید چک‌سام SHA-256 و متادیتای ریلیز:**
   - تولید فایل `SHA256SUMS.txt` برای تمامی آرتیفکت‌های ایجادشده
   - تولید فایل `release-metadata.json` شامل شماره نسخه، تاریخ ساخت، مدت نگهداری و مشخصات کامل آرتیفکت‌ها
4. **سیاست نگهداری (Retention):**
   - تعیین مدت زمان نگهداری ۹۰ روز (`retention-days: 90`) به جای مدت زمان ۷ روزه بیلدهای دیباگ

## نتایج ارزیابی خودکار
- اسکریپت تایید خودکار `tools/ci/verify_release_workflow.cjs` اجرا شد و ۱۰۰٪ آزمون‌ها (شامل آزمون منفی دایرکتوری خالی و اعتبارسنجی Actionlint) با موفقیت پاس شدند:
  - `Release Workflow Decoupling`: PASS
  - `Mandatory Artifact & Inventory Gate`: PASS
  - `Retention Policy Verification (90 days)`: PASS
  - `Negative Probe: Empty Output Rejection`: PASS
  - `SBOM & Metadata Verification`: PASS
  - `Baseline KMP Compilation Check`: PASS

## وضعیت تسک
DONE
