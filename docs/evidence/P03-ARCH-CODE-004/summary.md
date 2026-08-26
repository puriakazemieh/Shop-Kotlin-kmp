# شواهد P03-ARCH-CODE-004

## خروجی

`BackendProfile` اکنون یک data class immutable با این مقدارهای bootstrap قابل اعتماد است: `kind`، `apiRoot`، `assetRoot`، `allowedAuthHosts`، `contractVersion` و `manifestPath`.

اعتبارسنجی فقط URLهای HTTPS، حداقل یک host احراز هویت معتبر، نسخهٔ مثبت قرارداد و مسیر absolute Manifest را می‌پذیرد. `FeatureManifest` هیچ فیلد origin یا host احراز هویت ندارد؛ بنابراین JSON راه‌دور قادر به تغییر آن‌ها نیست و کلید ناشناخته هنگام parse رد می‌شود.

## آزمون

`BackendProfileTest` مسیر مثبت و تلاش برای تزریق `apiRoot` از Manifest را پوشش می‌دهد. اجرای `./gradlew.bat :core:config:capabilities:jvmTest` با کد خروجی 1 پیش از configuration به‌دلیل خطای محیطی loopback متوقف شد.

## ریسک باقی‌مانده

تزریق پروفایل واقعی به شبکه و حذف fallbackهای global در Task بعدی انجام می‌شود. تنظیمات تولیدی باید فقط از build/pairing قابل اعتماد وارد شوند.
