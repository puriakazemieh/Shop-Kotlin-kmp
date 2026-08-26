# شواهد P03-ANDROID-CODE-017

## تغییرات

- `BackendKind` اکنون تنها دو مقدار رسمی `WORDPRESS` و `SPRING` دارد و
  `BootstrapProfiles.forBackend` profile را با manifest path متناظر می‌سازد.
- `TenantConfig` هویت tenant را از branding و backend جدا می‌کند.
- `initKoin` بر اساس profile backend را انتخاب می‌کند؛ شناسه‌ی tenant یک وابستگی
  مستقل است و همان مقدار برای namespace cache، local manifest و remote manifest
  استفاده می‌شود.
- flavorهای قدیمی برند برای حفظ package/applicationId تا تسک migration نگه
  داشته شده‌اند و دیگر dimension backend جدیدی ایجاد نمی‌کنند.

## اعتبارسنجی خودکار

```text
.\gradlew.bat --no-daemon :core:config:capabilities:jvmTest :composeApp:compileKotlinJvm :composeApp:compileKotlinJs
BUILD SUCCESSFUL (exit code 0)
```

تست پیکربندی هر دو profile، مسیر manifest وردپرس/اسپرینگ و مدل مستقل tenant را
اثبات می‌کند. تست نصب و اجرای واقعی profileها روی دستگاه تا
`P03-QA-MANUAL-020` نگه داشته شده است.
