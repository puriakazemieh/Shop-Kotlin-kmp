# شواهد P03-MANIFEST-CODE-011C

`InMemoryLastKnownGoodManifestCache` رکورد را فقط بعد از resolve شدن با catalog
و ceiling و با expiry آینده ذخیره می‌کند. کلید cache از fingerprint کامل
`backend/tenant/origin` ساخته می‌شود؛ بنابراین backend یا tenant دیگر رکورد را
مصرف نمی‌کند. خواندن رکورد منقضی یا نامعتبر آن را حذف می‌کند و `invalidate`
پاک‌سازی صریح را فراهم می‌سازد.

```text
.\gradlew.bat --no-daemon :core:config:capabilities:jvmTest
BUILD SUCCESSFUL (exit code 0)
```

آزمون‌ها expiry، جداسازی backend/tenant، invalidation و رد feature ناشناخته را
اثبات می‌کنند.
