# P11-ANDROID-DISC-001 — Android package/signing/store inventory

## وضعیت

- تاریخ آغاز: 2026-10-12
- baseline commit: `6795feb1e4e6a1cf1ce62cb2dcde9053ec31d2ae`
- وضعیت نهایی این نوبت: `AWAITING_MANUAL_QA`
- این پرونده هیچ credential، keystore، token یا دادهٔ مشتری ندارد.

## موجودی استاتیک مخزن

| مورد | مقدار مشاهده‌شده | منبع |
|---|---|---|
| Namespace | `com.kazemieh.shop.app` | `androidApp/build.gradle.kts:13` |
| applicationId پایه | `com.kazemieh.shop` | `androidApp/build.gradle.kts:17` |
| versionCode / versionName پایه | `1` / `1.0` | `androidApp/build.gradle.kts:20-21` |
| flavorها | `atris`, `chronos`, `academy`, `psych`, `wp` | `androidApp/build.gradle.kts:39-67` |
| applicationIdهای مشتق‌شده | `com.kazemieh.shop.atris`, `.chronos`, `.academy`, `.psych`, `.wp` | از `applicationIdSuffix`های همان فایل |
| signingConfig / storeFile / keyAlias در source قابل رهگیری | مشاهده نشد | جست‌وجوی استاتیک در `androidApp/` |
| فایل keystore/certificate در مخزن | مشاهده نشد | جست‌وجوی `*.jks`, `*.keystore`, `*.pem`, `*.p12`, `*.pfx` |

وجود یا نبود نسخهٔ منتشرشده، مالکیت Play Console، trackها و محل امن کلید امضا از روی مخزن قابل اثبات نیست؛ هیچ فرضی دربارهٔ آن‌ها ثبت نشده است.

## اجرای خودکار

| فرمان | نتیجه | جزئیات |
|---|---|---|
| `git diff --check` | PASS (exit 0) | پیش از تغییر مستندات اجرا شد. |
| `./gradlew.bat :androidApp:assembleAcademyDebug` | FAIL | `:core:network:compileAndroidMain` نتوانست `androidx.annotation:annotation-experimental:1.4.0` را از Google Maven دریافت کند. علت ثبت‌شده دریافت dependency است، نه خطای source؛ build موفق هنوز اثبات نشده است. |

## اقدام دستی لازم (بدون افشای secret)

1. در **Google Play Console** سازمان، به **All apps** بروید و برای هر packageId جدول بالا جست‌وجو کنید. برای هر مورد فقط یکی از این دو نتیجه را ثبت کنید: «هرگز منتشر نشده» یا «منتشر شده»؛ در حالت دوم نام app، trackهای موجود و بیشترین `versionCode` را یادداشت کنید.
2. در سامانهٔ امن نگهداری کلید/مدیریت دسترسی سازمان، برای هر app منتشرشده مالک کلید upload/signing، محل نگهداری تأییدشده، تاریخ آخرین دسترسی و فقط fingerprint عمومی SHA-256 را تأیید کنید. **فایل کلید، رمز، alias محرمانه یا token را در این مخزن یا گفتگو وارد نکنید.**
3. اگر app قبلاً منتشر شده است، در Play Console صفحهٔ **Release > Testing/Production** را باز کنید و تأیید کنید نسخهٔ بعدی فقط با `versionCode` بزرگ‌تر منتشر خواهد شد؛ applicationId موجود را تغییر یا reset نکنید.
4. نتیجه را با نام tester، تاریخ، محیط (Play Console / vault سازمانی)، و شناسهٔ build یا screenshot redacted در همین مسیر Evidence ثبت کنید.

### معیار موفقیت

- برای همهٔ packageIdها وضعیت انتشار و مالکیت مشخص باشد.
- هیچ applicationId منتشرشده reset یا برای محصول متفاوت reuse نشود.
- کلیدها فقط در محل امن سازمان بمانند و در repo/log/Evidence قرار نگیرند.
- اگر build به شبکه دسترسی دارد، فرمان build دوباره با exit code 0 اجرا و گزارش آن ضمیمه شود.

## Rollback

این نوبت فقط مستندات Evidence و وضعیت Task را تغییر داده است. بازگشت، حذف همین دو تغییر مستنداتی است و هیچ تغییر build، packageId، کلید یا انتشار انجام نشده است.
