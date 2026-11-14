# P03-MANIFEST-CODE-025 Evidence

## Overview
- **Task ID**: `P03-MANIFEST-CODE-025`
- **Title**: ذخیره وضعیت مؤثر واکنشی و خروج کنترل‌شده از shadow
- **Date**: 2026-11-14
- **Verification Environment**: Windows 11, JDK 17, Gradle 8.14.3

## Summary of Changes
1. **EffectiveFeatureStore Reactive Implementation**:
   - `EffectiveFeatureStore` maintains reactive state `features: StateFlow<ResolvedFeatures>` and tracking property `revision: String?`.
   - Updated `DefaultEffectiveFeatureStore` to incorporate:
     - **رد Replay Revision**: در صورتی که ریویژن ورودی با ریویژن فعال جاری برابر باشد (`state.etag == revision`)، تغییر بدون اعمال رد می‌شود.
     - **اعمال سقف کامپایل‌شده (Compiled Feature Ceiling)**: با تزریق `CompiledFeatureCeiling` در استور، تمام مقادیر ورودی از فیلتر سقف عبور داده می‌شوند و هیچ قابلیت تاییدنشده یا خارج از بیلد فعال نمی‌گردد.
     - **حالت Fail-Closed در Stale/Invalid**: در رخداد خطا یا انقضای مانیفست (`ManifestBootstrapState.Error`)، استور فوراً به وضعیت fallback امن سوئیچ کرده و ریویژن فعال ابطال (`null`) می‌شود.
     - **خروج کنترل‌شده و ابطال صریح (`revoke`)**: متد `revoke(fallback)` جهت ابطال سریع و بازگشت به حالت امن پیاده‌سازی شد.
2. **خروج کنترل‌شده از Shadow Mode**:
   - `FeatureFlagShadowMode` تایید شد که صرفاً ناظر تلمتری است (`FeatureFlagShadowReporter`) و در مسیر فعال کلاینت، سیاست معتبر مانیفست را بازمی‌گرداند و خروجی legacy را جایگزین آن نمی‌کند.
3. **یکپارچه‌سازی در DI و کلاینت**:
   - در `composeApp/src/commonMain/kotlin/com/kazemieh/shop/App.kt`، نمونه `CompiledFeatureCeiling` به‌صورت صریح به `DefaultEffectiveFeatureStore` تزریق شد.
4. **پوشش تست‌های خودکار**:
   - در `core/config/capabilities/src/jvmTest/kotlin/com/kazemieh/config/capabilities/EffectiveFeatureStoreTest.kt` آزمون‌های رفتاری برای:
     - به‌روزرسانی واکنشی وضعیت و ریویژن با مانیفست جدید معتبر
     - رد کامل Replay Revision
     - بازگشت امن و ابطال ریویژن در خطای Stale/Invalid
     - مسدودسازی هرگونه ارتقای فراتر از سقف کامپایل‌شده
     - ابطال صریح (`revoke`) و بازگشت به وضعیت Fail-Closed

## Automated Test Results
All commands executed successfully with exit code 0:
1. **Capabilities Test Suite**:
   - Command: `.\gradlew.bat :core:config:capabilities:jvmTest`
   - Result: `BUILD SUCCESSFUL in 7s` (14 actionable tasks: 5 executed, 9 up-to-date)
   - Exit code: 0
2. **Multiplatform Full Compilation & Tests**:
   - Command: `.\gradlew.bat :core:config:capabilities:jvmTest :core:navigation:jvmTest :core:network:jvmTest :composeApp:jvmTest :composeApp:compileKotlinJs :composeApp:compileKotlinJvm`
   - Result: `BUILD SUCCESSFUL in 32s` (305 actionable tasks: 49 executed, 256 up-to-date)
   - Exit code: 0
3. **Android Application Debug Assembly**:
   - Command: `.\gradlew.bat :androidApp:assembleDebug`
   - Result: `BUILD SUCCESSFUL in 47s` (729 actionable tasks: 60 executed, 669 up-to-date)
   - Exit code: 0

## Acceptance & QA Details
- **Technical QA**: PASS. Full automated test suite passes with zero regressions.
- **Manual QA Instruction**:
  - **محیط**: محیط آزمایشی (Staging) روی شبیه‌ساز یا وب با بارگذاری مانیفست راه‌دور.
  - **مراحل**:
    1. کلاینت را اجرا کرده و مانیفست راه‌دور معتبر را دریافت کنید (بررسی فعال شدن قابلیت‌های مجاز).
    2. مانیفست حاوی تکرار ریویژن قبلی (Replay) ارسال کنید؛ بررسی کنید که کلاینت درخواست تکراری را نادیده می‌گیرد.
    3. اتصال را به حالت خطا یا پاسخ ۵۰۰ ببرید؛ بررسی کنید که کلاینت فوراً به حالت امن و قابلیت‌های fallback بازمی‌گردد.
    4. تلاشی برای ارسال قابلیت‌های خارج از سقف بیلد (خارج از ceiling) انجام دهید؛ بررسی کنید که قابلیت‌های خارج از سقف روشن نمی‌شوند.
  - **معیار موفقیت**: بازتاب آنی تغییرات معتبر در UI و روت‌ها، رد ریپلی، مسدود بودن فیچرهای خارج از سقف و رفتار Fail-Closed در خطا.
- **Final Status**: DONE
