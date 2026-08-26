# شواهد P03-ARCH-CODE-003

## خروجی

چهار مفهوم مستقل زیر در `core:config:capabilities` ساخته و به composition dependency برنامه افزوده شدند:

- `BackendProfile`: نوع backend، جدا از ظاهر و tenant.
- `BrandingConfig`: شناسه، نام نمایشی و واحد پول؛ بدون endpoint و flag.
- `BuildIdentity`: شناسهٔ برنامه و نسخهٔ build.
- `FeatureManifest`: دادهٔ خام manifest، پیش از اعمال policy و dependency.

هر مدل با `kotlinx.serialization` قابل serialization است و validation پایهٔ مقدارهای خالی/نامعتبر را انجام می‌دهد. آزمون JVM برای serialization و validation افزوده شد.

## آزمون

فرمان `./gradlew.bat :core:config:capabilities:jvmTest` با کد خروجی 1 پیش از configuration به‌علت `Unable to establish loopback connection` متوقف شد. در نتیجه اجرای واقعی آزمون پس از رفع مشکل محیط Gradle باقی است.

## دامنهٔ باقی‌مانده

انتقال رفتار runtime از `BrandConfig` قدیمی به bootstrap Manifest، یک migration مرحله‌ای است و در Taskهای پس از این تسک انجام می‌شود؛ این Task فقط مدل‌ها و مرز dependency را تثبیت کرد.
