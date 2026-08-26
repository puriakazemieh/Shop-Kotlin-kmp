# راهنمای QA دستی P03-QA-MANUAL-020

این تست عمداً توسط AI اجرا نشد و کارت در وضعیت `AWAITING_MANUAL_QA` است.
محیط پیشنهادی: سایت WordPress فعال `kazemieh.com` با افزونه Carmilla Bridge،
یک مرورگر Chrome با DevTools، و build فعلی PWA/client internal بدون rebuild.
فقط داده‌ی synthetic استفاده شود؛ توکن، ایمیل یا سفارش واقعی در screenshot ثبت
نشود.

## مراحل دقیق

1. در `wp-admin` به **Settings → Carmilla Manifest** بروید. قبل از شروع،
   `content.blog` و `commerce.core` را روشن و `academy.core`، `clinic.booking` و
   `psych.tests` را خاموش بگذارید و Save کنید.
2. در DevTools → Network، درخواست
   `/wp-json/carmilla/v1/client-manifest` را باز کنید. موفقیت یعنی HTTP 200،
   `backendProfile=WORDPRESS` و featureهای بالا با همین مقدار دیده شوند؛ مقدار
   ETag را یادداشت کنید.
3. بدون rebuild، صفحه/PWA را refresh کنید و به مسیر محصول یا Cart بروید. انتظار:
   route مستقیم به Home امن برگردد و هیچ callback شبکه‌ی commerce اجرا نشود.
   اگر با درخواست مستقیم endpoint commerce امتحان می‌کنید، پاسخ باید HTTP 403 با
   `code=FEATURE_DISABLED` باشد.
4. `commerce.core` را روشن و Save کنید. بدون rebuild، refresh کنید؛ manifest با
   ETag جدید برگردد و مسیر محصول/Cart دوباره قابل استفاده باشد.
5. برای stale/invalid، هنگام refresh درخواست manifest را در DevTools block کنید
   یا پاسخ synthetic منقضی/نامعتبر بدهید. انتظار: last-known-good معتبر استفاده
   شود؛ اگر stale/نامعتبر است، UI fail-closed با پیام خطا و دکمه «تلاش دوباره»
   نمایش دهد و feature حساس باز نشود.
6. وقتی `academy.core` خاموش است، یک deep link مستقیم به Course/Certificate و
   وقتی `clinic.booking` خاموش است، deep link Therapist/Appointment را باز کنید.
   انتظار: هیچ صفحه‌ی عمودی باز نشود و به Home امن برگردد.
7. در همان وضعیت خاموش، تب را ببندید و PWA/client را کاملاً terminate و دوباره
   اجرا کنید. انتظار: بعد از process restart نیز route و network bypass وجود
   نداشته باشد؛ پس از روشن‌کردن مجدد، قابلیت بدون rebuild برگردد.

## نتیجه‌ای که باید برگردانید

برای هر مرحله PASS/FAIL، تاریخ، مرورگر/دستگاه، build fingerprint، screenshot
redacted از manifest/error و در صورت FAIL شناسه‌ی defect را ثبت کنید. سپس همین
summary را تکمیل و status کارت را به `DONE` تغییر دهید تا صف به
`P03-MANIFEST-OPS-021` باز شود.
