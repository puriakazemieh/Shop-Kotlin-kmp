# شواهد P03-MANIFEST-CODE-008

## خروجی

`CompiledFeatureCeiling` افزوده شد. Manifest فقط می‌تواند قابلیت‌های موجود در artifact را کم کند و هر قابلیت خارج از ceiling، حتی اگر در Manifest روشن باشد، خاموش باقی می‌ماند. سقف `shopOnly` به‌صورت محافظه‌کارانه فقط محتوا، commerce و wallet را اجازه می‌دهد؛ Clinic، Psych و Admin خارج از آن هستند.

## آزمون

`CompiledFeaturePolicyTest` تلاش برای روشن‌کردن Clinic، Psych و Admin در artifact فروشگاهی را بررسی می‌کند. اجرای Gradle با کد 1 پیش از configuration به خطای loopback خورد.
