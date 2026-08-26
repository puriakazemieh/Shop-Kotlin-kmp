# Evidence — P03-MANIFEST-CODE-011

## نتیجه

این parent در ابتدا به‌دلیل بزرگ‌بودن scope به پنج زیرتسک مستقل (`011A` تا `011E`)
تقسیم شده بود. هر پنج زیرتسک اکنون پیاده‌سازی، تست و commit شده‌اند؛ بنابراین
parent از `BLOCKED` به `DONE` منتقل شد و هیچ کد جدیدی خارج از زیرتسک‌ها در این
کارت اضافه نشده است.

## پوشش زیرتسک‌ها

- `011A`: منبع local/generated و fail-closed
- `011B`: client شبکه با schema/backend/tenant/unknown-feature/ETag/timeout validation
- `011C`: cache آخرین manifest معتبر با namespace و expiry
- `011D`: هماهنگ‌کنندهٔ precedence و stateهای loading/ready/error/retry/LKG
- `011E`: اتصال bootstrap قبل از `AppNavHost` و UX بارگذاری/خطا/retry

Evidence جزئی هر مورد در `docs/evidence/P03-MANIFEST-CODE-011A/` تا
`docs/evidence/P03-MANIFEST-CODE-011E/` ثبت شده است.

## Verification

```text
.\gradlew.bat :core:navigation:jvmTest :core:config:capabilities:jvmTest :composeApp:compileKotlinJvm :composeApp:compileKotlinJs
exit code: 0
```

مشکل اولیهٔ `Unable to establish loopback connection` با تنظیم مسیر موقت Gradle
در `gradlew.bat` رفع شد. تست‌های دستی UI/network در کارت
`P03-QA-MANUAL-020` پیگیری می‌شوند و هنوز به build deploy‌شده نیاز دارند.

## ریسک باقی‌مانده

تأیید نهایی toggle بدون rebuild، stale/invalid، deep-link و process restart روی
محیط واقعی در کارت 020 هنوز انجام‌پذیر نیست؛ این موضوع parent کدنویسی را متوقف
نمی‌کند، اما Gate فاز ۳ را باز نگه می‌دارد.
