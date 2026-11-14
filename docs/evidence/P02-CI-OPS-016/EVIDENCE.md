# مستندات اثبات (Evidence) برای P02-CI-OPS-016

## اهداف و اقدامات
تسک P02-CI-OPS-016 با هدف «جداسازی کامل گردش کار آرتیفکت‌های انتشار (Release Artifact Workflow) از بیلد دیباگ و تولید چک‌سام، متادیتا، SBOM و سیاست نگهداری طولانی‌مدت (Retention)» انجام شد:

1. **تفکیک گردش کار انتشار (`.github/workflows/release-artifact.yml`):**
   - فراخوانی‌های دیباگ (`assembleDebug`) و نام‌گذاری آرتیفکت به عنوان `android-debug-apks` به طور کامل حذف شدند.
   - گردش کار اکنون منحصراً بر روی اهداف Release اجرا می‌شود:
     - بیلد APKهای ریلیز اندروید برای برندهای مختلف (`assemble*Release`)
     - بیلد UberJar دسکتاپ ریلیز (`packageUberJarForCurrentOS`)
     - بیلد توزیع وب پروداکشن (`jsBrowserDistribution`)
     - پکیج فایل‌های zip ریلیز تم و پلاگین وردپرس
2. **تولید خودکار سند فهرست قطعات نرم‌افزاری (SBOM):**
   - اسکریپت `tools/ci/generate_sbom.cjs` با پشتیبانی از دو فرمت استاندارد بین‌المللی توسعه داده شد:
     - `sbom.cyclonedx.json` منطبق بر استاندارد CycloneDX 1.5
     - `sbom.spdx.json` منطبق بر استاندارد SPDX 2.3
     - استخراج خودکار ۵۲ وابستگی مستقیم از `gradle/libs.versions.toml` به همراه `purl`
3. **تولید چک‌سام SHA-256 و متادیتای ریلیز:**
   - تولید فایل `SHA256SUMS.txt` برای تمامی آرتیفکت‌های ایجادشده
   - تولید فایل `release-metadata.json` شامل شماره نسخه، تاریخ ساخت، مدت نگهداری و مشخصات کامل آرتیفکت‌ها
4. **سیاست نگهداری (Retention):**
   - تعیین مدت زمان نگهداری ۹۰ روز (`retention-days: 90`) به جای مدت زمان ۷ روزه بیلدهای دیباگ

## نتایج ارزیابی خودکار
- اسکریپت تایید خودکار `tools/ci/verify_release_workflow.cjs` اجرا شد و ۱۰۰٪ آزمون‌ها با موفقیت پاس شدند:
  - `Release Workflow Decoupling`: PASS
  - `Retention Policy Verification (90 days)`: PASS
  - `SBOM & Metadata Verification`: PASS
  - `Baseline KMP Compilation Check`: PASS

## وضعیت تسک
DONE
