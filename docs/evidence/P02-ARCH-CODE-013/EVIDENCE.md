# مستندات اثبات (Evidence) برای P02-ARCH-CODE-013

## اهداف و اقدامات
مطابق با تاییدیه ADR (جداسازی Android Application Shell برای AGP 9)، اقدامات زیر انجام شد:
۱. یک ماژول جدید به نام `androidApp` ایجاد شد.
۲. کدهای مخصوص اندروید که نقطه ورود برنامه هستند (`MainActivity.kt` و `ShopApplication.kt`) و فایل `AndroidManifest.xml` از ماژول `composeApp` به ماژول `androidApp` منتقل شدند.
۳. پلاگینِ تنظیم‌شده روی `composeApp` (در فایل `carmilla.compose.application.gradle.kts`) از نوع `com.android.application` به `com.android.library` تغییر پیدا کرد تا ماژول KMP یک کتابخانه باقی بماند.
۴. تمامی تنظیماتِ مربوط به اپلیکیشن شامل `applicationId`ها، `versionCode`، `versionName` و `productFlavors` (برندهای white-label) با دقت کامل به `androidApp/build.gradle.kts` انتقال یافتند تا رفتار بیلد کاملاً حفظ شود.

## وضعیت بیلد و اعتبارسنجی
- ماژول `androidApp` با موفقیت sync شده و بیلد‌های اجرایی (همانند بیلد قبلی `composeApp`) تولید می‌شوند.
- بیلد‌های مشترک KMP در سایر پلتفرم‌ها (Desktop و iOS) دچار مشکل نشده‌اند.

## وضعیت تسک
DONE
