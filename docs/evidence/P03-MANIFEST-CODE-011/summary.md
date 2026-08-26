# Evidence — P03-MANIFEST-CODE-011

## دلیل توقف

این کارت به‌تنهایی پنج تغییر مستقل و چندپلتفرمی را هم‌زمان مطالبه می‌کند: منبع local/generated، دریافت شبکه‌ای امن، cache آخرین manifest معتبر، state و UX پیش از `NavHost`، و اتصال Koin/bootstrap. بنابراین از اندازهٔ M بزرگ‌تر است و طبق دستور کارت، بدون پیاده‌سازی به زیرتسک‌های کوچک‌تر نیاز دارد.

## پیشنهاد تقسیم

1. **Local manifest source**: مدل و فایل generated/local قابل‌ویرایش، با چهار flag اصلی و test.
2. **Remote manifest client**: دریافت tenant manifest با timeout/ETag و decode/validation fail-closed.
3. **Last-known-good store**: persistence، expiry، tenant/backend namespace و قواعد invalidation.
4. **Bootstrap coordinator**: precedence local → remote معتبر → LKG محدود؛ خروجی state مستقل از UI.
5. **Bootstrap UI/DI**: اتصال coordinator پیش از `AppNavHost`، loading/error/retry و test Compose.

## Baseline

```text
.\gradlew.bat :composeApp:compileKotlinJvm
exit code: 1
java.io.IOException: Unable to establish loopback connection
```

خطا پیش از پیکربندی Gradle رخ داده است و نتیجه‌ای دربارهٔ source code تسک ندارد.
