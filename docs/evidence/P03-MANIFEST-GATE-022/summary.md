# Evidence Summary — P03-MANIFEST-GATE-022

## عنوان تسک
Gate Manifest فاز ۳ (بررسی و تصویب نهایی گیت دو پروفایل و مانیفست قابلیت‌ها)

## مشخصات اجرا
- **Task ID:** `P03-MANIFEST-GATE-022`
- **Owner:** BOTH / HUMAN
- **Status:** `DONE`
- **Branch:** `develop`
- **Date:** 2026-11-22
- **Executed By:** Codex / Carmilla

---

## ۱. ارزیابی معیارهای اختصاصی گیت فاز ۳ (Gate Verification Criteria)

| # | معیار گیت | وضعیت | شواهد و مراجع اثبات |
|---|---|---|---|
| ۱ | **فقط دو Backend Profile در dimension مربوط وجود دارد** | **PASS** | `WORDPRESS` و `SPRING` در `BackendKind.kt` و `BootstrapProfiles.kt`. مدهای سه‌گانه وردپرس (`THEME_ONLY`, `PLUGIN_ONLY`, `CO_INSTALL`) به عنوان حالت هاست درون پروفایل وردپرس پیاده‌سازی شده و بعد مجزا یا طعم بیلد اضافی ایجاد نمی‌کنند. آزموده شده در [ManifestIntegrationWiringTest.kt](file:///d:/Android/AndroidStudioProjects/kmp-shop/composeApp/src/jvmTest/kotlin/com/kazemieh/shop/ManifestIntegrationWiringTest.kt). |
| ۲ | **customer identity و branding flavor جدید نمی‌سازند** | **PASS** | ایزولاسیون تننت و هویت مشتریان توسط `PrivateSessionNamespace` با تفکیک کلیدهای سشن و کش بر اساس origin و tenantId پیاده‌سازی شده و نیاز به flavor جدید را مرتفع نموده است. آزموده شده در آزمون سوییچ تننت. |
| ۳ | **manifest schema/version/dependency/fail-closed تست شده است** | **PASS** | نسخه اسکیما ۱، اعتبار نسخه، و قوانین وابستگی پدر-فرزند (مثل عدم فعال‌سازی `academy.quiz` بدون `academy.core`) در هر دو سمت سرور و کلاینت اعتبارسنجی شده‌اند. تست در [qa-manifest-staging-scenario.php](file:///d:/Android/AndroidStudioProjects/kmp-shop/wordpress/carmilla-bridge/tests/qa-manifest-staging-scenario.php) و `FeatureManifestBootstrapCoordinatorTest`. |
| ۴ | **zero bypass و migration pass با تست ثبت شده است** | **PASS** | مسدودسازی در ۴ لایه: روت‌گارد (`FeatureRouteGuard`)، ریپازیتوری‌های ۵ دامنه (۰ درخواست به ترنسپورت)، افزونه شبکه کلاینت (`FeatureGuardPlugin` با ۴۰۳)، و مرز توزیع REST وردپرس (`CB_Manifest_Controller::guard_rest_request` با ۴۰۳). همچنین سقف خرید کامپایل‌شده (`CompiledFeatureCeiling`) مانع دسترسی‌های غیرمجاز از ریموت است. شواهد میگریشن در `P03-MIGRATION-DATA-018`. |
| ۵ | **manual QA با تست واقعی تایید شده است** | **PASS** | سناریوی کامل پذیرش دستی در محیط‌های بازنمای وردپرس (PHP 8.1 در Docker با ۲۲ سناریو) و کلاینت KMP در تسک [P03-QA-MANUAL-027](file:///d:/Android/AndroidStudioProjects/kmp-shop/docs/evidence/P03-QA-MANUAL-027/summary.md) با موفقیت اجرا و ثبت شد. |

---

## ۲. خلاصه دستورات اعتبارسنجی خودکار گیت
- `.\gradlew.bat :composeApp:compileKotlinJvm :composeApp:compileKotlinJs` (exit 0)
- `.\gradlew.bat :composeApp:jvmTest --tests "com.kazemieh.shop.ManifestIntegrationWiringTest"` (exit 0)
- `docker run --rm -v "${PWD}:/app" -w /app php:8.1-cli php wordpress/carmilla-bridge/tests/qa-manifest-staging-scenario.php` (exit 0)
- `.\gradlew.bat :core:config:capabilities:jvmTest :core:navigation:jvmTest :core:network:jvmTest` (exit 0)
- `.\gradlew.bat :androidApp:assembleDebug` (exit 0)

---

## ۳. نتیجه نهایی گیت
تمام الزامات ۵‌گانه گیت فاز ۳ با شواهد قطعی، خودکار و محیطی تایید شدند.
وضعیت تسک: **DONE**
فاز ۰۳ رسماً با موفقیت **تکمیل (COMPLETED)** شد و قفل تسک‌های فاز ۰۴ (شروع از `P04-PRODUCT-ADR-038`) باز شد.
