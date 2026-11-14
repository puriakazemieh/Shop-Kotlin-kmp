<div dir="rtl" align="right">

# گزارش اجرای P03-ARCH-CODE-024 — مدل نسخه‌دار ProductBuildSpec مستقل از برند

## خلاصه فعالیت‌ها
1. مدل داده `ProductBuildSpec` که هویت و سقف قابلیت‌های بیلد محصول را تعریف می‌کند (مستقل از برند و backend) در `ConfigurationModels.kt` تکمیل و سخت‌گیرانه‌تر شد:
   - دربرگیرنده `productKind`، `sku`، `platform`، `tenant`، `branding`، `buildIdentity`، `backendProfile` و `compiledFeatureIds`.
   - اعتبارسنجی فرمت SKU (عدم فاصله و عدم خالی بودن).
   - اعتبارسنجی fail-closed برای `compiledFeatureIds` در برابر `FeatureCatalog.knownFeatureIds` (رد هرگونه قابلیت ناشناخته).
   - متد اعتبارسنجی `validateForRelease()` جهت الزام استفاده از tenant واقعی در بیلد Release (رد `local-default` و `test`) و الزام HTTPS بودن `apiRoot` و `assetRoot`.
2. پوشش کامل تست‌های خودکار در `ProductBuildSpecTest.kt`:
   - `serialization roundtrip succeeds`: تأیید تطابق کامل JSON serialization و deserialization.
   - `same brand works with both wordpress and spring backends`: اعتبارسنجی استقلال کامل branding از نوع backend.
   - `release validation succeeds with real tenant and https`: تأیید موفقیت اعتبارسنجی release برای tenant و backendProfile معتبر.
   - `release validation fails with local-default or test tenant`: رد tenant غیراستاندارد در بیلد release.
   - `release validation fails with non-https apiRoot`: الزام استفاده از پروتکل امن HTTPS در release.
   - `invalid blank sku throws exception` و `sku with whitespace throws exception`: رد SKU نامعتبر.
   - `unknown compiled feature throws exception`: رد featureهای ناشناخته در سقف کامپایل.
   - `remote manifest cannot change backend origin`: تأیید اینکه Remote manifest هیچ کنترلی روی origin و hostهای محلی immutable ندارد و ارسال فیلدهای ناشناخته با fail-closed رد می‌شود.

## نتایج تست‌های خودکار (Automated Tests)
تست‌های زیر با موفقیت کامل و بدون خطا اجرا شدند:
- `./gradlew.bat :core:config:capabilities:jvmTest` (Exit Code 0 — 39 tests completed, 0 failed)
- `./gradlew.bat :core:navigation:jvmTest` (Exit Code 0)
- `./gradlew.bat :core:network:jvmTest` (Exit Code 0)
- `./gradlew.bat :composeApp:compileKotlinJs :composeApp:compileKotlinJvm` (Exit Code 0)

## شواهد عدم نیاز به QA دستی بلاک‌کننده (N/A برای کدهای خالص مدل و بیلد)
این کارت صرفاً یک مدل معماری در `core/config/capabilities` است که هیچ رابط کاربری (UI)، تعامل مستقیم شبکه یا مهاجرت پایگاه داده ندارد. تمام معیارهای پذیرش فنی (گردش سریال‌سازی، برند مشترک، الزامات Release، اعتبارسنجی SKU و رد فیچر ناشناخته) توسط آزمون‌های خودکار به صورت قطعی اثبات شده‌اند.

وضعیت کارت: **DONE**
</div>
