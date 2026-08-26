# شواهد P03-ARCH-CODE-002

## خروجی

ماژول مستقل `core:config:capabilities` به تنظیمات Gradle افزوده شد. این ماژول مرز یکتای آینده برای پیکربندی قابلیت، پروفایل backend و Manifest است؛ `core:designSystem` فقط باید مسئول ظاهر باقی بماند.

## آزمون

آزمون `CapabilitiesModuleBoundaryTest` مرز و شناسهٔ پایدار ماژول را مشخص می‌کند. هر دو اجرای Gradle زیر پیش از configuration با خطای محیطی `Unable to establish loopback connection` و کد خروجی 1 متوقف شدند:

- `./gradlew.bat :core:config:capabilities:jvmTest`
- `./gradlew.bat --no-daemon :core:config:capabilities:jvmTest`

## ریسک و پیگیری

این Task رفتار برنامه را تغییر نداده است. اجرای مجدد آزمون پس از رفع دسترسی loopback Gradle لازم است. مدل‌های واقعی در Task بعدی منتقل می‌شوند.
