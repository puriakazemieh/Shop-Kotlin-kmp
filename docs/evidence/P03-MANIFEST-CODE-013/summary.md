# شواهد P03-MANIFEST-CODE-013

`FeatureRouteGuard` نگاشت مرکزی route به feature را انجام می‌دهد. listener واحد
در `AppNavHost` هر مقصد مستقیم یا داخلیِ خاموش را تشخیص می‌دهد و stack را به
`HomeGraph` امن برمی‌گرداند؛ مقصدهای عمومی و featureهای روشن بدون تغییر عبور
می‌کنند. این guard به‌صورت Koin در composeApp ساخته می‌شود.

```text
.\gradlew.bat --no-daemon :core:navigation:jvmTest :composeApp:compileKotlinJvm
BUILD SUCCESSFUL (exit code 0)
.\gradlew.bat --no-daemon :composeApp:compileKotlinJs
BUILD SUCCESSFUL (exit code 0)
```

تست واحد route خاموش، route روشن و home را پوشش می‌دهد. اجرای مستقیم deep-link
در browser/desktop به‌دلیل نیاز به QA انسانی، طبق برنامه در `P03-QA-MANUAL-020`
انجام می‌شود.
