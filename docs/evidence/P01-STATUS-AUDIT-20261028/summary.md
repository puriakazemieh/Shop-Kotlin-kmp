# ممیزی وضعیت فاز ۰۱ — ۲۰۲۶-۱۰-۲۸

## دامنه

تطبیق `tasks.md`، کارت‌های P01، Master Checklist، راهنمای تست دستی و کدهای KMP، WordPress و Spring. این گزارش جای QA انسانی یا آزمون محیط production نیست.

## نتایج قابل اثبات

- `P01-SECURITY-CODE-006` در کد و آزمون حاضر است: هفت آزمون JVM برای `ApiOriginValidator` در ۲۰۲۶-۱۰-۲۸ بدون failure گذشتند. این مورد `DONE` باقی ماند.
- `P01-SECURITY-CODE-003` و `P01-SECURITY-CODE-004` از پیش `AWAITING_MANUAL_QA` بودند و تغییری نکردند.
- `P01-SECURITY-CODE-005B` و `P01-SECURITY-CODE-005D` نیز `TODO` شدند: factory iOS از `NSUserDefaultsSettings` و factory JVM از `PreferencesSettings` استفاده می‌کنند؛ در نتیجه ادعای Keychain و حذف token پس از shutdown با کد فعلی سازگار نیست.
- `P01-PAYMENT-CODE-007` تا `009` از پیش `READY/TODO` بودند و همچنان ناتمام‌اند. WordPress در callback فقط `order`/`status` بازمی‌گرداند، اما کلاینت `token` می‌خواند.

## نقص قطعی fail-closed

`CB_Plugin::register_routes()` در `wordpress/carmilla-bridge/includes/class-cb-plugin.php`، `CB_Admin_Content_Controller` را بدون guard ثبت می‌کند. همان controller در `class-cb-admin-content-controller.php` پس از guard، مسیرهای `/api/admin/clinic/switch-requests` و `/api/admin/psych-tests` را دوباره با permission ادمین ثبت می‌کند. افزون بر آن، standalone boot فیلتر `cb_enable_health_lms` را صریحاً true می‌کند. بنابراین معیار `P01-WPPLUGIN-SEC-018` که با feature خاموش هیچ API discovery/direct URL نباشد برقرار نیست.

## دلیل تنزل وضعیت‌ها

- موارد `014` تا `019` و `020` تا `023`: Evidence عمدتاً lint/compile یا شرح ادعاست؛ آزمون‌های acceptance لازم (IDOR، replay/concurrency، secret scan قابل بازتولید، manual QA و Gate) موجود نیستند. افزون بر این، نقص fail-closed بالا Gate و review امنیت را نامعتبر می‌کند.
- `P01-SECURITY-CODE-005`: زیرکارت Web session هنوز `AWAITING_MANUAL_QA` است؛ پس parent نمی‌تواند DONE باشد.
- `024` تا `029`: کدهای Spring و WordPress بخشی از کنترل‌ها را دارند، اما Evidence تنها compile/تحلیل ایستا است یا کارت با معیار Master هم‌راستا نیست. بنابراین هیچ‌یک معیار پذیرش کامل را اثبات نمی‌کند.

## آزمون‌های اجراشده در ممیزی

- `./gradlew.bat :core:network:jvmTest --tests "com.kazemieh.network.common.ApiOriginValidatorTest" --console=plain` — ۷ آزمون، صفر failure.
- `docker run --rm -v "${PWD}/wordpress/carmilla-bridge:/app" -w /app php:8.1-cli php tests/smoke-security.php` — exit ۰؛ اما فقط ثبت سه CPT را می‌سنجد و مسیرهای REST یا باقی معیارهای P01 را پوشش نمی‌دهد.
