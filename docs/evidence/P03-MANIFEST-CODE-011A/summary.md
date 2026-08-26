# شواهد P03-MANIFEST-CODE-011A

## نتیجه

- `LocalFeatureManifestConfig` چهار flag پایه را در یک config local نگه می‌دارد.
- `LocalFeatureManifestSource` schema نامعتبر یا backend نامطابق را به `ResolvedFeatures` خالی تبدیل می‌کند.
- `GeneratedLocalFeatureManifest` تنها محل composeApp برای پیکربندی پیش‌فرض این چهار flag است.

## رفع Gradle

wrapper ویندوز Gradle اکنون پیش از اجرای Java، `TEMP` و `TMP` را به مسیر کوتاه `%SystemDrive%\jtmp` می‌برد؛ متغیر `KMP_GRADLE_TMP` برای override باقی است. این تغییر خطای `Unable to establish loopback connection` را در محیط فعلی رفع کرد.

یک خطای واقعیِ آشکارشده در build نیز اصلاح شد: `OrderRepositoryImpl` اکنون `AssetUrlResolver` را برای تبدیل cart حاصل از reorder دریافت می‌کند.

## فرمان‌های موفق

```text
.\gradlew.bat --no-daemon :core:config:capabilities:jvmTest
exit code: 0

.\gradlew.bat --no-daemon :composeApp:compileKotlinJvm
exit code: 0
BUILD SUCCESSFUL in 1m 3s
```
