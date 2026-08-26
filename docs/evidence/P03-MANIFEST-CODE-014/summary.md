# شواهد P03-MANIFEST-CODE-014

`FeatureUseCaseGuard` یک guard مشترک برای use-case/repository/worker فراهم
می‌کند. وقتی feature خاموش است، block حتی یک‌بار هم اجرا نمی‌شود؛ در نتیجه
request شبکه و side effect ایجاد نمی‌شود. guard در Koin composeApp ثبت شده تا
مسیرهای background و data بتوانند همین قرارداد را تزریق کنند.

```text
.\gradlew.bat --no-daemon :core:config:capabilities:jvmTest :composeApp:compileKotlinJvm
BUILD SUCCESSFUL (exit code 0)
.\gradlew.bat --no-daemon :composeApp:compileKotlinJs
BUILD SUCCESSFUL (exit code 0)
```

تست‌ها شمارش request صفر برای feature خاموش و اجرای دقیق یک‌باره برای feature
روشن را اثبات می‌کنند؛ تست دستی لازم نیست (N/A).
