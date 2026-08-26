# شواهد P03-MANIFEST-CODE-011E

در `composeApp`، coordinator در Koin ثبت و در `App()` قبل از نمایش `AppNavHost`
اجرا می‌شود. حالت loading فقط progress نمایش می‌دهد؛ حالت ready ناوبری را
باز می‌کند؛ حالت error با fallback امن، پیام فارسی و دکمهٔ «تلاش دوباره» قابل
بازیابی است. retry مستقیماً `coordinator.retry()` را صدا می‌زند.

```text
.\gradlew.bat --no-daemon :composeApp:compileKotlinJvm
BUILD SUCCESSFUL (exit code 0)
.\gradlew.bat --no-daemon :composeApp:compileKotlinJs
BUILD SUCCESSFUL (exit code 0)
```

سناریوی واقعی WordPress/PWA عمداً اجرا نشده و برای `P03-QA-MANUAL-020` در صف
تأیید انسانی باقی می‌ماند.
