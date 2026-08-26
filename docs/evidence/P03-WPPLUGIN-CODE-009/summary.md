# شواهد P03-WPPLUGIN-CODE-009

## خروجی

Endpoint عمومی `GET /wp-json/carmilla/v1/client-manifest` اضافه شد. پاسخ دارای `schemaVersion`، `manifestVersion`، `minimumAppVersion`، زمان اعتبار و ماتریس قابلیت‌های whitelist شده است؛ هیچ token یا گزینهٔ خصوصی در آن نیست.

برای پاسخ ETag تولید می‌شود و `If-None-Match` برابر، پاسخ 304 می‌دهد. dependencyهای feature در server هم اعمال می‌شوند تا child با parent خاموش واقعاً خاموش بماند.

## آزمون انجام‌شده

PHP محلی در PATH نبود، اما Docker موجود بود و هر دو فرمان زیر با image رسمی `php:8.1-cli` با کد خروجی 0 اجرا شدند:

`docker run --rm -v "${PWD}:/app" -w /app php:8.1-cli php -l includes/class-cb-manifest-controller.php`

`docker run --rm -v "${PWD}:/app" -w /app php:8.1-cli php tests/smoke-manifest.php`

نتیجه: lint بدون خطا و smoke برابر `ALL PASSED` بود. برای release، endpoint را در یک WordPress فعال دوبار با ETag فراخوانی کنید؛ بار دوم باید HTTP 304 بازگرداند.
