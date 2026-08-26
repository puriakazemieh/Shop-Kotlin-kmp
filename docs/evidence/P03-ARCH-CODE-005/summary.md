# شواهد P03-ARCH-CODE-005

## خروجی

`EndpointResolver` و `AssetUrlResolver` در ماژول پیکربندی اضافه و از `BackendProfile` تزریق می‌شوند. resolver endpoint مسیر relative را فقط به `apiRoot` قابل اعتماد وصل می‌کند و تلاش برای URL کامل راه‌دور را رد می‌کند.

mapperهای catalog، cart و admin دیگر `PlatformConfig.baseUrl` را نمی‌خوانند. URLهای نسبی asset از `AssetUrlResolver` دریافت می‌شوند و repositoryهای مصرف‌کننده آن را با Koin تزریق می‌کنند.

## آزمون

`UrlResolversTest` مسیر relative و رد origin جایگزین را پوشش می‌دهد. این فرمان پیش از configuration، به علت `Unable to establish loopback connection` با کد خروجی 1 متوقف شد:

`./gradlew.bat :core:config:capabilities:jvmTest :composeApp:compileKotlinJvm`

## ریسک باقی‌مانده

bootstrap فعلی برای سازگاری با تنظیم قدیمی `ApiConfig` از یک پروفایل موقت می‌سازد. artifactهای release باید در Taskهای بعدی پروفایل صریح و قابل اعتماد دریافت کنند؛ endpointهای API نیز باید به adapter canonical منتقل شوند.
