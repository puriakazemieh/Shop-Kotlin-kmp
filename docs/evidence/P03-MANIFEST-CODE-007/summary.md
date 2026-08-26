# شواهد P03-MANIFEST-CODE-007

## خروجی

`FeatureCatalog` قرارداد قابلیت‌های v1 و همهٔ dependencyهای ADR-004 را نگه می‌دارد. resolver فقط schema نسخهٔ 1 و شناسه‌های شناخته‌شده را می‌پذیرد؛ قابلیت فرزند وقتی هر پیش‌نیازش خاموش باشد، خاموش می‌شود. catalog دارای شناسهٔ تکراری، dependency ناشناخته یا cycle در زمان ساخت رد می‌شود.

## آزمون

`FeatureCatalogTest` خاموش‌شدن فرزند، feature ناشناخته، schema نامعتبر و cycle را پوشش می‌دهد. اجرای `./gradlew.bat :core:config:capabilities:jvmTest` با کد خروجی 1 پیش از configuration به‌دلیل loopback متوقف شد.
