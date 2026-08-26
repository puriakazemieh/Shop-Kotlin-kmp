# شواهد P03-MANIFEST-CODE-012

`FeatureFlagShadowMode` اختلاف legacy و manifest را فقط به‌صورت شمارش redacted
(تعداد تغییرات، legacy-only و manifest-only) گزارش می‌کند و خروجی legacy را
بدون تغییر برمی‌گرداند. بنابراین فعال‌سازی shadow mode رفتار فعلی را عوض
نمی‌کند و هیچ tenant، origin، token یا مقدار خام flag در event نیست. coordinator
این mode را برای نتیجهٔ remote معتبر استفاده می‌کند.

```text
.\gradlew.bat --no-daemon :core:config:capabilities:jvmTest :composeApp:compileKotlinJvm
BUILD SUCCESSFUL (exit code 0)
.\gradlew.bat --no-daemon :composeApp:compileKotlinJs
BUILD SUCCESSFUL (exit code 0)
```

تست واحد حفظ رفتار legacy، شمارش اختلاف و رد نسخهٔ خالی را پوشش می‌دهد؛ چون
تغییر shadow-only است، تست دستی لازم نیست (N/A).
