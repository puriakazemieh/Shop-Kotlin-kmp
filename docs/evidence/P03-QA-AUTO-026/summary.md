# Evidence Summary — P03-QA-AUTO-026

## عنوان تسک
آزمون wiring واقعی Manifest و دو پروفایل (WordPress با ۳ مد و Spring در ۵ دامنه)

## مشخصات اجرا
- **Task ID:** `P03-QA-AUTO-026`
- **Branch:** `develop`
- **Date:** 2026-11-22
- **Executed By:** Codex / Carmilla

## شرح آزمون و تغییرات مهندسی
در راستای پوشش معیار اختصاصی تسک ("روشن/خاموش، tenant switch، manifest stale و سقف خرید شبیه‌سازی‌شده با شمار request بررسی؛ صرف lambda fake به‌عنوان تست DI پذیرفته نیست"):
1. **پیاده‌سازی آزمون یکپارچگی جامع در سطح کلاینت:**
   - [ManifestIntegrationWiringTest.kt](file:///d:/Android/AndroidStudioProjects/kmp-shop/composeApp/src/jvmTest/kotlin/com/kazemieh/shop/ManifestIntegrationWiringTest.kt):
   - استفاده از سرور محلی شبیه‌سازی‌شده با برچسب مشخص (`[FIXTURE] Contract-level Simulated Endpoint Fixture: not actual server implementation`).
   - ۵ سناریوی یکپارچگی مستقل طراحی و اجرا شدند:
     1. **ماتریس پروفایل‌ها و مدهای هاست وردپرس (`matrix of two backend profiles and three wordpress host modes boots from simulated fixture`):**
        - تست هر دو پروفایل `WORDPRESS` (با مدهای `THEME_ONLY`, `PLUGIN_ONLY`, `CO_INSTALL`) و `SPRING`.
        - اعتبارسنجی کلاینت ریموت با کلاینت HTTP واقعی Ktor و بررسی شمارنده ریکوئست‌های مانیفست.
     2. **پایپلاین کامل روشن/خاموش در ۵ دامنه محصول (`full pipeline wiring on and off across all five domains from bootstrap to menu to route guard to transport`):**
        - حالت خاموش: بررسی فیلتر شدن تب سبد خرید در منوی پایین (`BottomBarDestination.Cart`) و غیرفعال شدن گزینه‌های آکادمی، کلینیک و تست روان‌شناسی در MoreScreen.
        - بررسی مسدودسازی در `FeatureRouteGuard` برای تمام مسیرهای مربوطه.
        - بررسی ترنسپورت واقعی در ریپازیتوری‌های `CartRepository`, `AcademyRepository`, `ClinicRepository`, `PsychTestRepository` و اثبات عدم ارسال حتی ۱ درخواست به شبکه (requestCount = 0).
        - تغییر پویا در زمان اجرا (Dynamic runtime update) بدون نیاز به ریست DI کلاینت: فعال شدن گزینه‌ها در منو، باز شدن مسیرها در روت‌گارد، و عبور ریکوئست‌های معتبر در ترنسپورت و افزایش متناظر شمارنده سرور.
     3. **سوییچ تننت و ایزولاسیون فضای نشست (`tenant switch isolates private namespaces and refreshes manifest with verified request count`):**
        - بررسی سوییچ بین `tenant-alpha` (تجارت روشن، روان‌شناسی خاموش) و `tenant-beta` (تجارت خاموش، روان‌شناسی روشن).
        - بررسی ایزولاسیون کلیدهای `PrivateSessionNamespace` و تفکیک فضای توکن‌ها و کش بین تننت‌ها.
        - بررسی افزایش دقیق شمارنده درخواست مانیفست سرور به ازای هر تننت.
     4. **رد کش بیات و بازگشت به پیکربندی محلی امن (`stale manifest cache is rejected on network failure and falls back to safe local configuration`):**
        - تست خطای شبکه (HTTP 503) در حضور کش تازه (قبل از TTL): بازگشت امن با سورس `LAST_KNOWN_GOOD`.
        - تست خطای شبکه در زمان انقضای کش (بعد از TTL): رد کش بیات (eviction) و بازگشت قطعی با سورس `LOCAL`.
     5. **اعمال سقف کامپایل‌شده خرید (`simulated purchase ceiling prevents unauthorized feature activation from remote manifest`):**
        - تعریف سقف کامپایل‌شده خرید فقط برای حوزه فیزیکی تجارت.
        - دریافت مانیفست ریموت سرور که مدعی دسترسی به آکادمی، کلینیک و روان‌شناسی است.
        - اثبات بلاک شدن قابلیت‌های خارج از سقف توسط `CompiledFeatureCeiling` و مسدود ماندن مسیرها در `FeatureRouteGuard`.

## دستورات اجرا شده و نتایج
| Command | CWD | Result | Exit Code |
|---|---|---|---|
| `.\gradlew.bat :composeApp:jvmTest --tests "com.kazemieh.shop.ManifestIntegrationWiringTest"` | `d:\Android\AndroidStudioProjects\kmp-shop` | 5 passed (100% success) | 0 |
| `.\gradlew.bat :core:config:capabilities:jvmTest :core:navigation:jvmTest :core:network:jvmTest` | `d:\Android\AndroidStudioProjects\kmp-shop` | All passed | 0 |
| `.\gradlew.bat :composeApp:compileKotlinJs :composeApp:compileKotlinJvm` | `d:\Android\AndroidStudioProjects\kmp-shop` | Build successful | 0 |
| `.\gradlew.bat :androidApp:assembleDebug` | `d:\Android\AndroidStudioProjects\kmp-shop` | Build successful | 0 |

## نتیجه نهایی
تمام معیارهای اختصاصی تسک `P03-QA-AUTO-026` به‌صورت فنی و خودکار با شواهد بازتولیدپذیر محقق شدند.
وضعیت تسک: **DONE**
تسک مسدودشده بعدی: `P03-QA-MANUAL-027` به وضعیت **READY** ارتقا می‌یابد.
