# Evidence Summary — P03-QA-MANUAL-027

## عنوان تسک
QA انسانی جایگزین Manifest پس از اتصال runtime

## مشخصات وضعیت
- **Task ID:** `P03-QA-MANUAL-027`
- **Owner:** `HUMAN`
- **Current Status:** `AWAITING_MANUAL_QA`
- **Branch:** `develop`
- **Base Commit Fingerprint:** `603e015b`
- **Depends On:** `P03-QA-AUTO-026` (DONE)
- **Blocks:** `P03-MANIFEST-GATE-022`

---

## ۱. اعتبارسنجی خودکار پیش‌نیازها (Pre-QA Automated Baseline)
پیش از اجرای تست دستی در محیط Staging، تمامی آزمون‌های خودکار و کامپایل تارگت‌ها با موفقیت تأیید شدند:

| Command | Result | Exit Code |
|---|---|---|
| `.\gradlew.bat :composeApp:jvmTest --tests "com.kazemieh.shop.ManifestIntegrationWiringTest"` | ۵ آزمون یکپارچگی پایپلاین پاس شدند | `0` |
| `.\gradlew.bat :core:config:capabilities:jvmTest :core:navigation:jvmTest :core:network:jvmTest` | آزمون‌های قابلیت‌ها، نویگیشن و شبکه پاس شدند | `0` |
| `.\gradlew.bat :composeApp:compileKotlinJs :composeApp:compileKotlinJvm` | کامپایل کامل کلاینت JS و JVM | `0` |
| `.\gradlew.bat :androidApp:assembleDebug` | بیلد پکیج دیباگ اندروید | `0` |

---

## ۲. سناریو و چک‌لیست آزمون دستی در محیط Staging (Manual QA Protocol)

### محیط تست
- **سرور:** وردپرس Staging (پنل مدیریت `wp-admin` با افزونه Carmilla Bridge و تم مربوطه).
- **کلاینت:** بیلد Staging اپلیکیشن (اندروید یا وب کلاینت).

### مراحل آزمون (Steps)
1. **تغییر قابلیت‌ها (Feature Toggle):**
   - وارد پنل وردپرس شوید و یکی از قابلیت‌ها (مثلاً آزمون‌های روان‌شناسی `psych.tests` یا دوره‌های آموزشی `academy.core`) را غیرفعال کنید.
2. **رفرش بلادرنگ کلاینت (Runtime Refresh without Rebuild):**
   - بدون نیاز به بیلد یا راه‌اندازی مجدد اپ، منوی کلاینت را رفرش کنید.
   - **انتظار:** گزینه مربوطه باید بلافاصله از منوی پایین یا صفحه More پنهان شود.
3. **لینک مستقیم و دیپ‌لینک (Deep Link Guard):**
   - تلاش کنید از طریق دیپ‌لینک مستقیم (مثلاً مسیر `/psych-tests` یا `/courses`) وارد صفحه شوید.
   - **انتظار:** روت‌گارد (`FeatureRouteGuard`) مانع ورود شود و صفحه عدم دسترسی یا هدایت امن نمایش یابد.
4. **بررسی ترافیک شبکه (Network Request Isolation):**
   - لاگ شبکه سرور یا مانیتورینگ شبکه کلاینت را بررسی کنید.
   - **انتظار:** برای قابلیت خاموش، هیچ درخواستی به اندپوینت‌های مربوطه ارسال نشود (Zero Network Requests).
5. **مقاومت در برابر قطعی شبکه و کش بیات:**
   - ارتباط اینترنت را قطع یا اندپوینت مانیفست را از دسترس خارج کنید.
   - **انتظار:** کلاینت نباید کرش کند؛ وضعیت کش معتبر LKG یا پیکربندی امن محلی به درستی بارگذاری شود.

---

## ۳. فرم ثبت نتیجه توسط آزمایش‌کننده انسانی (Tester Completion Form)
- **نام آزمایش‌کننده (Tester):** [نام و نقش]
- **تاریخ آزمون (Date):** [YYYY-MM-DD]
- **شناسه بیلد (Build Fingerprint):** [Commit SHA / Build Number]
- **نتیجه گام ۱ (Toggle):** [PASS / FAIL]
- **نتیجه گام ۲ (Runtime Menu):** [PASS / FAIL]
- **نتیجه گام ۳ (Deep Link):** [PASS / FAIL]
- **نتیجه گام ۴ (Zero Network Requests):** [PASS / FAIL]
- **نتیجه گام ۵ (Offline/Stale Cache):** [PASS / FAIL]
- **تأیید نهایی:** [PENDING / APPROVED]
