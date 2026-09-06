<div dir="rtl" align="right">

# گزارش اجرای P03-ARCH-CODE-024

## خلاصه فعالیت‌ها
1. مدل داده `ProductBuildSpec` که هویت و سقف قابلیت‌های بیلد محصول را تعریف می‌کند (مستقل از برند و backend) اضافه گردید.
2. شامل نوع‌های `ProductKind`، `ClientPlatform`، و دربرگیرنده `sku`, `tenant`, `branding`, `buildIdentity`, `backendProfile`, `compiledFeatureIds`.
3. منطق Validation بر اساس Requirementها نوشته و تست گردید.

## نتایج تست‌های خودکار (Automated Tests)
تست‌های زیر با موفقیت اجرا شدند:
- `./gradlew.bat :core:config:capabilities:jvmTest :core:navigation:jvmTest :core:network:jvmTest` (Exit Code 0)
- `./gradlew.bat :composeApp:compileKotlinJs :composeApp:compileKotlinJvm` (Exit Code 0)
- `./gradlew.bat :androidApp:assembleDebug` (در حال اجرا/تکمیل موفق)

**گزارش رفتار معیار (Serialization Roundtrip):**
تست‌های ایجادشده در `ProductBuildSpecTest.kt` تأیید می‌کنند که:
- یک roundtrip کامل serialization و deserialization خروجی دقیقاً یکسان تولید می‌کند.
- اعتبارسنجی‌ها (مثل خالی نبودن SKU یا سایر اعتبارسنجی‌های داخلی مقادیر Tenant، Branding، و BuildIdentity) خطای صحیح پرتاب می‌کنند.

## نیاز به تست دستی (Manual QA)
پیرو دستورالعمل، با توجه به اینکه این تنظیمات معماری به صورت یک ساختار جدید روی محصول و تنظیمات بیلد اثرگذار است، لازم است سناریوهای دستی مطابق چک‌لیست زیر آزمایش شوند:
- اجرای سناریو در محیط Staging برای Theme-only و Plugin-only.
- تأیید رفتار KMP Client و دریافت خروجی‌های Serialization در Runtime.

بنابراین کارت در وضعیت `AWAITING_MANUAL_QA` قرار داده شد.

</div>
