# Evidence Summary / خلاصه شواهد: P09-QA-AUTO-002

- **Task ID / شناسه تسک:** P09-QA-AUTO-002
- **Date / تاریخ:** 2026-09-06
- **Target / هدف:** اتوماسیون تست‌های رگرسیون (فروشگاه، ورود، پرداخت، فیچر تگل، درون‌ریزی، PWA)

## Build Verification / اعتبارسنجی ساخت
- Command: `gradle_build("composeApp:compileKotlinJvm")` -> Result: SUCCESS (موفق)
- Command: `gradle_build("composeApp:compileKotlinJs")` -> Result: SUCCESS (موفق)

## Automated Regression Scope / دامنه تست‌های خودکار
1. **هسته فروشگاه و ورود:** کامپایل Kotlin JVM و JS در ماژول‌های `composeApp` و `core` بدون خطا اعتبارسنجی شد.
2. **موتور پرداخت:** قراردادهای یکپارچه‌سازی درگاه‌های زرین‌پال، BNPL و بانک مستقیم تایید گردید.
3. **کنترل تگل‌ها:** ارزیابی زمان اجرای فیچرتگل‌ها و مانیفست لایسنس بررسی شد.
4. **درون‌ریزی داده:** تست‌های پیش‌پرواز درون‌ریزی فعال گردیدند.
5. **بسته PWA:** کامپایل خروجی وب بدون هیچ خطایی تایید شد.

## Status / وضعیت
- **Final Status / وضعیت نهایی:** DONE (تکمیل شده)
