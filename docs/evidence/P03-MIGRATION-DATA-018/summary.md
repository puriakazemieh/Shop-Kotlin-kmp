# شواهد P03-MIGRATION-DATA-018

`CB_Legacy_Migration` یک migration یک‌باره و غیرمخرب برای Theme Modهای قدیمی
است. فقط در صورت نبود manifest canonical، featureهای allowlist‌شده را به
`cb_manifest_features` منتقل می‌کند و marker نسخه‌ی ۱ ثبت می‌کند؛ اجرای دوم
idempotent است و داده‌ی canonical را overwrite نمی‌کند.

## اعتبارسنجی خودکار

در `php:8.1-cli`:

```text
php -l includes/class-cb-legacy-migration.php   exit 0
php -l carmilla-bridge.php                      exit 0
php tests/smoke-legacy-migration.php            exit 0
php tests/smoke-manifest-security.php           exit 0
```

تست synthetic نگاشت allowlist، انتقال flag خاموش، marker و اجرای تکراری بدون
تغییر را اثبات می‌کند.

## موارد نیازمند بررسی انسانی

mapping شش applicationId، signing key/versionCode، upgrade روی دستگاه‌های موجود
و پاک‌سازی token هنگام تغییر tenant/backend در
`docs/migrations/P03-LEGACY-FLAVOR-MAPPING_FA.md` ثبت شده و برای
`P03-QA-MANUAL-020` باقی مانده است.
