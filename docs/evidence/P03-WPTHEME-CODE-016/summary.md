# شواهد P03-WPTHEME-CODE-016

تابع `carmilla_feature_enabled` در Theme اکنون وضعیت را از option canonical
`cb_manifest_features` می‌خواند و dependencyهای manifest را fail-closed اعمال
می‌کند. `get_theme_mod` برای feature visibility دیگر فراخوانی نمی‌شود؛ بخش
Feature در Customizer فقط توضیح می‌دهد که تغییر باید از Carmilla Manifest
افزونه انجام شود.

## اعتبارسنجی خودکار

در کانتینر `php:8.1-cli` و مسیر کاری `wordpress/carmilla-theme`:

```text
php -l functions.php                 exit 0
php -l inc/customizer.php            exit 0
php -l inc/post-types.php            exit 0
php tests/smoke-manifest-theme.php   exit 0
```

تست synthetic روشن/خاموش‌شدن blog، shop و academy، عدم فراخوانی Theme Mod و
بسته‌شدن زنجیره‌ی dependency را پوشش می‌دهد. تست UI/Customizer واقعی سایت برای
`P03-QA-MANUAL-020` نگه داشته شده است.
