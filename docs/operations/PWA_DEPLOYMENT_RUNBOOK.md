# PWA Deployment & Operations Runbook

این سند مراحل استقرار، مدیریت کش و بازگشت به نسخه قبلی (Rollback) را برای نسخه PWA پروژه Carmilla توضیح می‌دهد.

## ۱. فرآیند ساخت (Build)

برای تولید آرتیفکت‌های وب، از دستور زیر استفاده کنید:
```powershell
./gradlew :composeApp:jsBrowserDistribution
```
خروجی در مسیر `composeApp/build/dist/js/productionExecutable/` قرار می‌گیرد. این پوشه شامل `index.html`، فایل‌های JS کامپایل شده و منابع PWA (مانند `manifest.json` و `sw.js`) است.

## ۲. استقرار (Deployment)

### محیط Staging
۱. فایل‌های خروجی را به سرور Staging منتقل کنید.
۲. اطمینان حاصل کنید که هدرهای `Service-Worker-Allowed: /` و `Cache-Control: no-cache` برای فایل `sw.js` تنظیم شده باشند.

### محیط Production
۱. پس از تایید در Staging، فایل‌ها را به محیط عملیاتی منتقل کنید.
۲. از ابزارهایی مانند `rsync` یا CI/CD pipeline برای انتقال امن استفاده کنید.

## ۳. مدیریت کش و Cache Busting

پروژه از Service Worker برای کش کردن منابع استفاده می‌کند. برای اجبار کاربران به دریافت نسخه جدید:

۱. در فایل `composeApp/build.gradle.kts` در تسک `generatePwaFiles` مقدار `CACHE_NAME` را تغییر دهید (مثلاً از `-v1` به `-v2`).
۲. با این کار، در رویداد `activate` سرویس ورکر، کش‌های قدیمی شناسایی و حذف می‌شوند.
۳. فایل `app-config.json` نیز باید با هدر `no-cache` سرو شود تا تغییرات SKU یا API سریعاً اعمال شوند.

## ۴. بازگشت به نسخه قبلی (Rollback)

در صورت بروز مشکل در نسخه جدید:
۱. کد را به آخرین Commit پایدار (Stable) برگردانید (`git revert` یا `git checkout`).
۲. فرآیند Build را مجدداً اجرا کنید.
۳. نسخه جدید را مستقر کنید.
۴. **نکته مهم**: اگر مشکل مربوط به کش مرورگر کاربران است، حتماً نسخه `CACHE_NAME` را یک واحد افزایش دهید تا کش‌های معیوب نسخه قبلی پاک شوند.

## ۵. تست Smoke پس از استقرار
پس از هر استقرار، موارد زیر را چک کنید:
- [ ] باز شدن صفحه اصلی بدون خطا در کنسول.
- [ ] ثبت شدن Service Worker در تب Application مرورگر.
- [ ] کارکرد صحیح Deep-linkها.
- [ ] لود شدن صحیح `app-config.json` متناسب با محیط.
