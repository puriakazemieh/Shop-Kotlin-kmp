# شواهد P03-MANIFEST-CODE-011B

## نتیجه

کلاینت امن manifest راه‌دور در `core/config/capabilities` اضافه شد. URL فقط از
`BackendProfile` trusted ساخته می‌شود؛ timeout با `withTimeout` اعمال می‌شود و
ETag در درخواست (`If-None-Match`) و پاسخ حفظ می‌گردد. پاسخ 304 به‌صورت
`NotModified` و هر خطای شبکه، HTTP، schema، backend، tenant یا feature ناشناخته
به `Failure` تبدیل می‌شود. decode با JSON strict انجام می‌شود و سقف compiled
همچنان روی نتیجه اعمال می‌گردد.

## آزمون

```text
.\gradlew.bat --no-daemon :core:config:capabilities:jvmTest
BUILD SUCCESSFUL (exit code 0)
```

آزمون‌ها ارسال URL trusted، timeout و ETag، پاسخ 304، و fail-closed برای schema
نامعتبر، backend/tenant ناهماهنگ، feature ناشناخته و timeout را پوشش می‌دهند.
