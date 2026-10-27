# P01-SECURITY-CODE-006 — Evidence Map

## خلاصه هدف و آسیب‌پذیری
- **شناسه کارت:** `P01-SECURITY-CODE-006`
- **عنوان:** `?api=` و override origin آزاد از production حذف یا allowlist شود
- **سطح تهدید:** امکان هدایت اپلیکیشن وب / PWA به سرور مخرب از طریق تزریق پارامتر `?api=` یا `?wpApi=` در URL و سرقت Session Cookie یا توکن‌های دسترسی کاربر.
- **خروجی الزامی:** رد شدن آدرس‌های localhost، شبکه‌های خصوصی (RFC 1918) و دامنه‌های غیرمجاز (foreign URL) در حالت release/production.

---

## تغییرات اعمال‌شده (Smallest Safe Diff)

1. **`core/network/src/commonMain/kotlin/com/kazemieh/network/common/ApiOriginValidator.kt`:**
   - موتور اعتبارسنجی مستقل چندسکویی برای تفکیک:
     - `isLocalhost`: تشخیص `localhost`، `127.0.0.1`، `::1`، `*.localhost`.
     - `isPrivateAddress`: تشخیص بازه‌های IPv4 خصوصی (10.0.0.0/8، 172.16.0.0/12، 192.168.0.0/16، 169.254.0.0/16، 127.0.0.0/8) و دامنه‌های محلی (`.local`, `.internal`, `.lan`).
     - `isForeignHost`: بررسی انطباق هاست با لیست سفید `allowedAuthHosts` در `BackendProfile`.
     - `validateOriginOverride`: در محیط release هرگونه پروتکل غیر HTTPS، هرگونه localhost، IP خصوصی یا هاست ناشناخته را رد می‌کند و فقط آدرس‌های HTTPS موجود در allowlist را می‌پذیرد. در محیط debug/dev، آدرس‌های محلی را مجاز می‌شمارد.

2. **`composeApp/src/commonMain/kotlin/com/kazemieh/shop/App.kt` (`initKoin`):**
   - پیش از مقداردهی `ApiConfig.baseUrlOverride`، مقدار `apiBaseUrlOverride` از فیلتر `ApiOriginValidator.validateOriginOverride` عبور داده می‌شود. در صورتی که آدرس نامعتبر باشد، نادیده گرفته شده و امن به `spec.backendProfile.apiRoot` بازمی‌گردد.

3. **`composeApp/src/webMain/kotlin/com/kazemieh/shop/main.kt` (`resolveSkuAndApi`):**
   - در محیط release وب، هرگونه پارامتر تزریقی در URL (`?api=` یا `?wpApi=`) از override کردن API root مسدود شده و تنها پیکربندی رسمی بارگذاری‌شده مجاز است.

4. **`composeApp/src/jsMain/resources/pwa-bootstrap.js`:**
   - تابع `validateUrlOverride` پیاده‌سازی شد.
   - پارامترهای `?wpApi=` یا `?api=<URL>` قبل از اعمال در `config.backend.apiRoot` اعتبارسنجی می‌شوند و در صورت اجرا در دامنه عمومی (release) دامنه‌های لوکال، IPهای خصوصی و دامنه‌های foreign حذف می‌شوند.

5. **`composeApp/src/jsMain/resources/index.html`:**
   - نسخه کش اسکریپت بوت‌استرپ به `v=13` به‌روزرسانی شد.

---

## نتایج تست‌های خودکار (Automated Verification)

| دستور | خروجی / نتیجه | وضعیت |
|---|---|---|
| `.\gradlew.bat :core:network:jvmTest --tests "com.kazemieh.network.common.ApiOriginValidatorTest"` | 7 تست اختصاصی رد localhost، رد دامنه‌های محلی و خصوصی، رد foreign origin، رد HTTP در release و پذیرش دامنه‌های مجاز | ✅ Exit Code 0 |
| `.\gradlew.bat :core:network:jsBrowserTest --tests "com.kazemieh.network.common.ApiOriginValidatorJsTest"` | تست‌های رفتار محیط مرورگر JS در رد لوکال‌هاست، IP خصوصی و foreign URL | ✅ Exit Code 0 |
| `.\gradlew.bat :composeApp:compileKotlinJs` | کامپایل موفق تارگت Web/JS | ✅ Exit Code 0 |
| `.\gradlew.bat :composeApp:compileKotlinJvm` | کامپایل موفق تارگت Desktop/JVM | ✅ Exit Code 0 |

---

## دستورالعمل تست دستی (Manual QA Instructions)

### محیط تست
- مرورگر وب (Chrome / Firefox / Edge)
- فیکسچر وردپرس در حال اجرا روی پورت 8091 (`http://localhost:8091/pwa/`)

### سناریوهای آزمون
1. **تزریق آدرس مخرب خارجی در URL:**
   - آدرس زیر را در مرورگر باز کنید:
     `http://localhost:8091/pwa/?wpApi=https://evil-attacker.com/api/`
   - بررسی تب Console و Network در Developer Tools مرورگر:
   - **نتیجه مورد انتظار:** هیچ درخواستی به `evil-attacker.com` ارسال نمی‌شود و اپلیکیشن به مبدأ معتبر محلی یا پیش‌فرض خود متصل باقی می‌ماند.

2. **تزریق IP خصوصی شبکه محلی:**
   - آدرس زیر را باز کنید:
     `http://localhost:8091/pwa/?wpApi=https://192.168.1.99/api/`
   - **نتیجه مورد انتظار:** آدرس نامعتبر نادیده گرفته شده و روت اصلی تغییری نمی‌کند.

3. **حالت عملکرد عادی PWA وردپرس محلی:**
   - آدرس `http://localhost:8091/pwa/#/login` را باز کنید.
   - **نتیجه مورد انتظار:** فرم ورود بدون خطای شبکه لود شده و به `http://localhost:8091/wp-json/carmilla/v1/` متصل می‌شود.
