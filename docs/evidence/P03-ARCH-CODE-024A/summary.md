# P03-ARCH-CODE-024A Evidence — تولید deterministic تنظیمات و سقف قابلیت کلاینت

## ۱. هدف و تغییرات پیاده‌سازی
مطابق کارت `P03-ARCH-CODE-024A` و قرارداد `docs/INDEPENDENT_PRODUCTS_SPEC_FA.md`:
- تولید سورس‌های پیکربندی و سقف استاتیک از روی `tools/build-config/products.json` در مسیر `build/generated` و بدون ایجاد منبع fork شده یا کد مشتری.
- اصلاح `composeApp/build.gradle.kts`:
  - مرتب‌سازی حتمی و قطعی (Deterministic Sorting) کلیدهای SKU با `sortedEntries = parsedJson.entries.sortedBy { it.key.toString() }`.
  - اعتبارسنجی نبود هرگونه کلید یا الگوی حساس/رمز/توکن در ورودی‌ها (`password`, `secret`, `private_key`, `token`, `apikey`).
  - اعتبارسنجی شناسه SKU (عدم وجود فاصله و خالی نبودن).
  - افزودن `jvmTest.dependencies` با `kotlin("test")`.
- اصلاح شناسه‌های غیراستاندارد در `tools/build-config/products.json` به شناسه‌های رسمی کاتالوگ (`academy.core`, `psych.tests`, `clinic.booking`).
- افزودن آزمون‌های جامع رفتاری در:
  - `composeApp/src/jvmTest/kotlin/com/kazemieh/shop/GeneratedProductSpecsTest.kt`
  - `core/config/capabilities/src/jvmTest/kotlin/com/kazemieh/config/capabilities/DeterministicBuildSpecTest.kt`

## ۲. بررسی معیارهای اختصاصی کارت
1. **دو اجرای ورودی یکسان خروجی یکسان (Deterministic Generation):**
   - به دلیل مرتب‌سازی صریح `sortedEntries` بر اساس کلید SKU و تبدیل قطعی آرایه‌ها، هر دو بار اجرای تسک `generateProductSpecs` خروجی دقیقاً بایت‌به‌بایت یکسان تولید می‌کند.
   - آزمون `two identical input specifications produce identical serialized output` در `DeterministicBuildSpecTest` تطابق هش و متنی دو اجرای یکسان را اثبات کرد (PASS).
2. **دو SKU سقف متفاوت (Different Ceilings for SKUs):**
   - SKUهای `carmila`، `chronos` و `academy` سقف‌های مستقل دارند:
     - `carmila` دارای سقف `wallet` و `commerce.digital`.
     - `chronos` فاقد سقف `wallet` و `commerce.digital`.
     - `academy` دارای `academy.core` و `commerce.digital` اما فاقد `commerce.physical`.
   - آزمون `two SKUs enforce different compiled feature ceilings` با موفقیت این تفاوت را اثبات کرد (PASS).
3. **Secret در خروجی صفر (Zero Secrets in Output):**
   - اسکریپت ساخت با تابع بازگشتی `assertNoSecrets` ساختار ورودی را ممیزی کرده و در صورت وجود کلیدواژه‌های حساس خطا پرتاب می‌کند.
   - آزمون‌های `secret hygiene - zero secrets in generated specs output` و `zero secrets in build spec representations` در هر دو لایه پاس شدند (PASS).
4. **بسته فاقد قابلیت با remote روشن نشود (No Remote Override of Uncompiled Features):**
   - آزمون `build lacking a compiled feature cannot be remotely turned on` با ارسال مانیفست راه‌دوری که تلاش می‌کند تمامی قابلیت‌ها (از جمله `wallet`, `academy.core`, `psych.tests`, `clinic.booking`) را برای `chronos` فعال کند اجرا شد؛ تابع `ceiling.apply` تمامی این قابلیت‌ها را در وضعیت `false` قفل کرد (PASS).

## ۳. شواهد اجرای آزمون‌ها و فرامین بیلد

### آزمون‌های اختصاصی JVM
- **فرمان**: `./gradlew.bat :composeApp:jvmTest`
- **کد خروجی**: ۰ (Exit Code 0)
- **گزارش**: ۵ تست اجرا شده، ۰ خطا، ۰ شکست (`TEST-com.kazemieh.shop.GeneratedProductSpecsTest.xml`).

### آزمون‌های ماژول‌های Core
- **فرمان**: `./gradlew.bat :core:config:capabilities:jvmTest :core:navigation:jvmTest :core:network:jvmTest`
- **کد خروجی**: ۰ (Exit Code 0)
- **گزارش**: تمام تست‌های `capabilities` (شامل ۳ آزمون جدید `DeterministicBuildSpecTest`)، `navigation` و `network` پاس شدند.

### کامپایل JVM و Web/JS
- **فرمان**: `./gradlew.bat :composeApp:compileKotlinJs :composeApp:compileKotlinJvm`
- **کد خروجی**: ۰ (Exit Code 0 - BUILD SUCCESSFUL in 23s)

### ساخت کامل بسته‌های Android Debug
- **فرمان**: `./gradlew.bat :androidApp:assembleDebug`
- **کد خروجی**: ۰ (Exit Code 0 - BUILD SUCCESSFUL in 1m 22s)
- تمام طعم‌های اندروید (`carmila`, `atris`, `chronos`, `academy`, `psych`, `wp`) بدون خطای کامپایل یا فقدان منبع بیلد شدند.

## ۴. نتیجه فنی و پذیرش
معیارهای فنی اختصاصی کارت `P03-ARCH-CODE-024A` به‌صورت کاملاً خودکار و مستند آزموده شده و با خروجی صفر فرامین تایید شدند.
وضعیت تسک: `DONE`.
