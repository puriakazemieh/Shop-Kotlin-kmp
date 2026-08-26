# شواهد P03-SECURITY-CODE-006

## خروجی

`PrivateSessionNamespace` fingerprint دادهٔ خصوصی را از `backendKind + tenantId + origin` می‌سازد. کلیدهای access/refresh token اکنون به این namespace متصل‌اند و `TokenManager.switchNamespace` در صورت تغییر fingerprint، tokenهای فعال را پاک می‌کند؛ نتیجه logout اجباری است.

## آزمون

`PrivateSessionNamespaceTest` تغییر fingerprint با tenant، backend و origin را پوشش می‌دهد. فرمان `./gradlew.bat :core:config:capabilities:jvmTest :composeApp:compileKotlinJvm` پیش از configuration با کد خروجی 1 و خطای loopback متوقف شد.

## اقدام دستی پیش از release

روی محیط synthetic وارد tenant A شوید، یک token ذخیره کنید، سپس به tenant B با همان origin یا به origin دیگر بروید. انتظار: token قبلی خوانده نشود، کاربر به login بازگردد و دادهٔ خصوصی tenant قبلی نمایش داده نشود.
