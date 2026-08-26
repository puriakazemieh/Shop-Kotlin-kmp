# شواهد P03-MANIFEST-CODE-011D

`FeatureManifestBootstrapCoordinator` stateهای `Loading`، `Ready` و `Error` را
مستقل از UI تولید می‌کند. نتیجهٔ remote همیشه با featureهای local تقاطع داده
می‌شود، پس manifest نمی‌تواند compiled/local ceiling را دور بزند. خطای remote
با fallback local یا LKG همراه می‌ماند و `retry()` فقط همان remote client را
فراخوانی می‌کند.

```text
.\gradlew.bat --no-daemon :core:config:capabilities:jvmTest
BUILD SUCCESSFUL (exit code 0)
```

تست‌ها remote reduction/ceiling، fallback امن، شمارش retry و استفاده/رد LKG
منقضی را اثبات می‌کنند.
