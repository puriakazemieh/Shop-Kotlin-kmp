# شواهد P03-QA-AUTO-019

تست `P03ManifestMatrixTest` ماتریس F0 تا F4 را برای هر دو profile
`WORDPRESS` و `SPRING` اجرا می‌کند. برای هر fixture، catalog/dependency، مسیر
route و guard شبکه/use-case بررسی می‌شوند؛ feature خاموش callback را اجرا نمی‌کند
و route مستقیم آن Blocked است.

```text
.\gradlew.bat --no-daemon :core:navigation:jvmTest :core:config:capabilities:jvmTest
BUILD SUCCESSFUL (exit code 0)
.\gradlew.bat --no-daemon :composeApp:compileKotlinJvm :composeApp:compileKotlinJs
BUILD SUCCESSFUL (exit code 0)
```

این شواهد synthetic و بدون داده‌ی واقعی است. تست WordPress واقعی، UI، deep-link
و process restart برای `P03-QA-MANUAL-020` باقی مانده است.
