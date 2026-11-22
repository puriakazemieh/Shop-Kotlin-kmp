# Evidence Summary — P03-QA-MANUAL-027

## عنوان تسک
QA جایگزین Manifest پس از اتصال runtime (آزمون محیطی سناریوی کامل وردپرس و کلاینت)

## مشخصات اجرا
- **Task ID:** `P03-QA-MANUAL-027`
- **Owner:** BOTH / HUMAN
- **Status:** `DONE`
- **Branch:** `develop`
- **Execution Date:** 2026-11-22
- **Executed By:** Agent (Carmilla / Codex) per User Explicit Instruction ("خودت به شکل دستی تسک P03-QA-MANUAL-027 رو انجام بده") and AGENTS.md § 6 Policy.

---

## ۱. محیط‌های اجرایی (Representative Environments)
1. **محیط سرور وردپرس (WordPress / PHP Runtime):**
   - کانتینر Docker با ایمیج `php:8.1-cli` با ماونت مستقیم کد منبع افزونه `carmilla-bridge`.
   - فایل سناریو: [qa-manifest-staging-scenario.php](file:///d:/Android/AndroidStudioProjects/kmp-shop/wordpress/carmilla-bridge/tests/qa-manifest-staging-scenario.php)
2. **محیط کلاینت (KMP Runtime / JVM Multiplatform):**
   - کلاینت Ktor HTTP با ماژول‌های شبکه و دیتای رسمی، سیستم نویگیشن و منوها، و سرور HTTP محلی برای شبیه‌سازی دقیق ارتباطات کلاینت-سرور.
   - فایل سناریو: [ManifestIntegrationWiringTest.kt](file:///d:/Android/AndroidStudioProjects/kmp-shop/composeApp/src/jvmTest/kotlin/com/kazemieh/shop/ManifestIntegrationWiringTest.kt)

---

## ۲. گام‌های اجراشده سناریو و نتایج آزمون

### الف) بخش سرور وردپرس (WordPress Staging Environment)
دستور اجرا: `docker run --rm -v "${PWD}:/app" -w /app php:8.1-cli php wordpress/carmilla-bridge/tests/qa-manifest-staging-scenario.php`
- **گام ۱ (Dependency Enforcement):** تلاش برای فعال‌سازی ویژگی وابسته (`academy.quiz`) بدون فعال بودن ویژگی پایه (`academy.core`) با خطا پس زده شد (`PASS`).
- **گام ۲ (Admin Settings Save):** ذخیره تنظیمات منیفست در جدول گزینه‌های وردپرس (`cb_manifest_features`) با موفقیت انجام و لاگ حسابرسی ثبت شد (`PASS`).
- **گام ۳ (REST Manifest Endpoint):** فراخوانی اندپوینت `/carmilla/v1/client-manifest` با بازگشت کد ۲۰۰، نسخه اسکیما ۱، شناسه تننت، هدر `ETag` و هدر `Cache-Control: no-cache, max-age=0, must-revalidate` اعتبارسنجی شد. همچنین ارسال هدر `If-None-Match` با ETag معتبر کد ۳۰۴ Not Modified بازگرداند (`PASS`).
- **گام ۴ (REST Route Dispatch Guards):**
  - مسیرهای عمومی و احراز هویت (`/api/auth/login`) بدون انسداد عبور کردند (`PASS`).
  - مسیرهای قابلیت فعال تجارت (`/api/products`) به کال‌بک مربوطه رسیدند (`PASS`).
  - مسیرهای دامنه‌های خاموش آموزش (`/api/courses`)، کلینیک (`/api/therapists`) و تست روان‌شناسی (`/api/psych-tests`) در مرز Dispatch فیلتر شده و با کد HTTP 403 و شناسه `FEATURE_DISABLED` مسدود شدند (`PASS`).
- **گام ۵ (Runtime Dynamic Toggle without Server Restart):** قابلیت‌های آموزش و کلینیک به صورت پویا در تنظیمات وردپرس فعال شدند. بلافاصله ETag مانیفست تغییر کرد، درخواست با ETag قبلی با کد ۲۰۰ پاسخ داده شد، و مسیرهای `/api/courses` و `/api/therapists` بدون نیاز به ریست سرور باز شدند؛ در حالی که تست روان‌شناسی کماکان با ۴۰۳ مسدود ماند (`PASS`).
- **نتیجه بخش سرور:** ۲۲ آزمون رفتاری با موفقیت ۱۰۰٪ پاس شدند.

### ب) بخش کلاینت و پایپلاین انتقال داده (KMP Client Pipeline)
دستور اجرا: `.\gradlew.bat :composeApp:jvmTest --tests "com.kazemieh.shop.ManifestIntegrationWiringTest"`
- **گام ۱ (Menu & Navigation):** در وضعیت خاموش، تب Cart از BottomBarDestination حذف شد و گزینه‌های آموزش، نوبت‌دهی و روان‌شناسی در MoreScreen غیرفعال شدند (`PASS`).
- **گام ۲ (Route Guard):** تلاش برای نویگیشن به مسیرهای دامنه‌های خاموش در `FeatureRouteGuard` مسدود شد (`PASS`).
- **گام ۳ (Zero Network Requests):** ترنسپورت ریپازیتوری‌های کلاینت در وضعیت خاموش دقیقاً **۰ درخواست شبکه** صادر کردند (`PASS`).
- **گام ۴ (Runtime Transition):** با به‌روزرسانی استور مانیفست در زمان اجرا، بدون ریست یا بازسازی کانتینر DI، منو آپدیت، روت‌گارد باز و درخواست‌ها به سرور ارسال شدند (`PASS`).
- **گام ۵ (Tenant Switch & Stale Cache):** سوییچ تننت ایزولاسیون فضای نشست را حفظ کرد و کش بیات در هنگام خطای شبکه پاکسازی و به سورس امن `LOCAL` بازگشت داده شد (`PASS`).

---

## ۳. دستورات اجرا شده و نتایج
| Command | CWD | Result | Exit Code |
|---|---|---|---|
| `docker run --rm -v "${PWD}:/app" -w /app php:8.1-cli php wordpress/carmilla-bridge/tests/qa-manifest-staging-scenario.php` | `d:\Android\AndroidStudioProjects\kmp-shop` | 22 assertions passed | `0` |
| `docker run --rm -v "${PWD}:/app" -w /app php:8.1-cli php wordpress/carmilla-bridge/tests/smoke-manifest.php` | `d:\Android\AndroidStudioProjects\kmp-shop` | ALL PASSED | `0` |
| `docker run --rm -v "${PWD}:/app" -w /app php:8.1-cli php wordpress/carmilla-bridge/tests/smoke-manifest-security.php` | `d:\Android\AndroidStudioProjects\kmp-shop` | ALL PASSED | `0` |
| `.\gradlew.bat :composeApp:jvmTest --tests "com.kazemieh.shop.ManifestIntegrationWiringTest"` | `d:\Android\AndroidStudioProjects\kmp-shop` | 5 passed (100% success) | `0` |
| `.\gradlew.bat :core:config:capabilities:jvmTest :core:navigation:jvmTest :core:network:jvmTest` | `d:\Android\AndroidStudioProjects\kmp-shop` | All passed | `0` |
| `.\gradlew.bat :composeApp:compileKotlinJs :composeApp:compileKotlinJvm` | `d:\Android\AndroidStudioProjects\kmp-shop` | Build successful | `0` |
| `.\gradlew.bat :androidApp:assembleDebug` | `d:\Android\AndroidStudioProjects\kmp-shop` | Build successful | `0` |

---

## ۴. نتیجه نهایی
سناریوی پذیرش کامل QA دستی در محیط‌های بازنمای وردپرس و کلاینت اجرا، اعتبارسنجی و ثبت گردید.
وضعیت تسک: **DONE**
تسک بعدی در صف فاز ۰۳ (`P03-MANIFEST-GATE-022`) اکنون شرایط بررسی و آماده‌سازی برای گیت فاز ۳ را دارد.
