# مستندات اثبات (Evidence) برای P02-ARCH-CODE-013

## اهداف و اقدامات
مطابق با تاییدیه ADR (جداسازی Android Application Shell برای AGP 9)، اقدامات زیر انجام شد:
۱. ماژول اختصاصی `androidApp` به عنوان پوسته نازک اجرایی اندروید ایجاد شد و به صورت مستقیم به `:composeApp` وابستگی دارد.
۲. کدهای نقطه ورود اندروید (`MainActivity.kt` و `ShopApplication.kt`) و `AndroidManifest.xml` در ماژول `androidApp` مستقر شدند.
۳. پلاگین ماژول `composeApp` از نوع `com.android.kotlin.multiplatform.library` تنظیم شده و ماهیت کتابخانه‌ای مشترک KMP حفظ گردید.
۴. مقادیر `applicationId`ها، `versionCode`، `versionName` و ۶ فلِیوِر برند (`carmila`, `atris`, `chronos`, `academy`, `psych`, `wp`) در `androidApp/build.gradle.kts` تعریف و بیلد شدند.
۵. مقدار `targetSdk = 36` منطبق بر استانداردهای `RTK.md` تنظیم گردید.
۶. تست ارتقا (Upgrade Test) با بیلد نسخه جدید (`versionCode = 2`, `versionName = 1.1`) با شناسه اپلیکیشن و امضای یکسان تایید شد.
۷. اسکریپت اعتبارسنجی خودکار در `tools/android/verify_android_app_shell.cjs` ساخته و تمام تست‌ها با موفقیت پاس شدند.

## گزارش تفصیلی
گزارش کامل شامل جدول فلِیوِرها، سایز APKها و لاگ‌های بیلد در [summary.md](summary.md) ثبت گردیده است.

## وضعیت تسک
DONE
