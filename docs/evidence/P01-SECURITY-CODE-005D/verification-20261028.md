# P01-SECURITY-CODE-005D — JVM token persistence policy

- تاریخ: ۲۰۲۶-۱۰-۲۸
- محدوده: `core:data`، بدون دادهٔ واقعی یا token واقعی.
- baseline: `.\gradlew.bat :core:data:compileKotlinJvm --console=plain` — exit 0.
- characterization: تست `TokenSettingsSecurityTest` پیش از اصلاح fail شد، زیرا نمونهٔ جدید JVM همان token مصنوعی را از `PreferencesSettings` می‌خواند.
- اصلاح: `createTokenSettings` در JVM یک `InMemoryTokenSettings` تازه می‌سازد؛ بنابراین token با پایان process از بین می‌رود و در preferences پایدار نوشته نمی‌شود.
- verification: `.\gradlew.bat :core:data:jvmTest --tests "com.kazemieh.data.local.TokenSettingsSecurityTest" :core:data:compileKotlinJs :core:data:compileKotlinJvm --console=plain` — exit 0.
- نتیجه: نمونهٔ دوم JVM access/refresh token را ندارد؛ logout با بسته‌شدن برنامه تحقق می‌یابد.
- تست دستی: لازم نیست؛ آزمون واحد behavior را مستقیم بررسی می‌کند.
- rollback: بازگرداندن تغییر کارخانه JVM نشست را دوباره پایدار می‌کند و نباید بدون جایگزین امن انجام شود.
