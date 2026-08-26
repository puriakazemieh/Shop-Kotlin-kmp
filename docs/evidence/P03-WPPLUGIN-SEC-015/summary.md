# شواهد P03-WPPLUGIN-SEC-015

## خروجی

کنترل manifest در فیلتر مرکزی `rest_pre_dispatch` قرار گرفت. مسیرهای REST
قابل‌شناسایی به feature canonical نگاشت می‌شوند و پیش از اجرای callback، اگر
feature خاموش باشد پاسخ استاندارد `FEATURE_DISABLED` با وضعیت HTTP 403 برمی‌گردد.
مسیرهای احراز هویت و `client-manifest` عمداً بدون feature guard باقی ماندند تا
کلاینت بتواند manifest را دریافت و دوباره authenticate کند.

## اعتبارسنجی خودکار

در کانتینر `php:8.1-cli` و مسیر کاری `wordpress/carmilla-bridge` اجرا شد:

```text
php -l includes/class-cb-manifest-controller.php          exit 0
php -l includes/class-cb-plugin.php                      exit 0
php tests/smoke-manifest.php                              exit 0
php tests/smoke-manifest-security.php                     exit 0
php tests/smoke-phase3.php                                exit 0
```

تست امنیتی نگاشت مسیر blog، استثنای auth، عبور feature روشن، خطای
`FEATURE_DISABLED` و status 403 را پوشش می‌دهد. تست واقعی روی سایت و مرورگر
برای `P03-QA-MANUAL-020` نگه داشته شده است.
