# P00-PWA-AUTH-WP-021C07C — شاهد شبکه و آماده‌سازی QA

تاریخ: 2026-10-26

## محیط ایزوله

- WordPress fixture: `http://localhost/carmilla-p00-browser-auth-qa/`
- صفحهٔ آزمون هم‌مبدأ: `http://localhost/carmilla-p00-browser-auth-qa/web-auth-qa.html`
- حساب مشتری ساختگیِ آن اجرای QA پس از تست حذف شد؛ شناسه و رمز آن نگهداری نشده‌اند.
- این fixture به یک پایگاه‌دادهٔ ساختگی جدا متصل است و برای سایت‌های موجود، داده یا تنظیمات تولیدی ایجاد نکرده است.

## اصلاح‌های کشف‌شده در آزمون واقعی

1. Apache در Windows مقدار `HTTPS=off` می‌دهد؛ استثنای local QA اکنون فقط برای loopback و همین حالت غیرTLS مجاز است.
2. WordPress یک `Authorization` خالی مصنوعی به REST request اضافه می‌کرد؛ فقط همان مقدار خالی نادیده گرفته می‌شود تا سیاست duplicate/headerهای واقعی همچنان fail-closed بماند.
3. گزینه‌های `setcookie` دیگر شامل نام کوکی نیستند؛ نام جداگانه به API PHP داده می‌شود.
4. نام مسیرهای خط‌تیره‌دار recovery به callbackهای underscore صحیح وصل شد.
5. CORS پاسخ POST از headerهای اعتبارسنجی‌شدهٔ همان درخواست ساخته می‌شود.

## آزمون خودکار

- Docker disposable WordPress 6.4.3/PHP 8.1.27: `smoke-web-auth-route-activation-real-wp.php`، 16/16 PASS.
- PHP lint برای `class-cb-web-auth-controller.php` و `class-cb-web-request-policy.php`: PASS.
- `:composeApp:jsBrowserDevelopmentWebpack`: BUILD SUCCESSFUL (1m 4s).

## ماتریس HTTP fixture

تمام POSTهای معتبر با `Origin: http://localhost`، `X-Carmilla-Web-Request: 1` و `Content-Type: application/json` اجرا شدند.

| مسیر | نتیجهٔ آزمون |
|---|---|
| login | 200، `Set-Cookie` HttpOnly/Secure/SameSite=Strict، `Cache-Control: no-store` |
| refresh | 200 و گردش کوکی؛ پس از logout، 401 `INVALID_SESSION` |
| logout | 200 و ابطال نشست |
| register | 201؛ تکرار همان هویت 409 `IDENTITY_UNAVAILABLE` |
| forgot-password | حساب ناشناس 200 عمومی، بدون افشای هویت |
| reset-password | توکن ساختگی 400 `INVALID_TOKEN` |
| reset-password-with-otp | OTP ساختگی 401 `BAD_OTP` |
| origin نامجاز | 403 `WEB_REQUEST_DENIED` و بدون CORS credential |
| OPTIONS | 204 از لایهٔ CORS عمومی؛ درخواست‌های POST همچنان توسط policy اختصاصی route اعتبارسنجی می‌شوند. |

## QA دستی مالک

1. صفحهٔ آزمون را باز کنید، ایمیل و رمز ساختگی بالا را وارد کنید و «ورود» را بزنید.
2. در نتیجه باید `HTTP 200` و `Cache-Control: no-store` ببینید.
3. در DevTools → Application → Cookies → `http://localhost` باید یک کوکی `__Host-carmilla_wp_...` با `HttpOnly`، `Secure` و `SameSite=Strict` باشد. مقدار آن را نمایش/کپی نکنید.
4. «نوسازی نشست» باید 200 دهد. سپس «خروج» باید 200 بدهد و یک‌بار دیگر «نوسازی نشست» باید 401 با پیام نشست نامعتبر/منقضی بدهد.
5. دوباره ورود کنید، سپس در Network روی request ورود کلیک کنید: بدنه نباید `refreshToken` یا `credential` داشته باشد؛ پاسخ باید `no-store` باشد.
6. دکمه‌های خطای بازیابی را بزنید: برای ایمیل ناشناس پاسخ عمومی، برای token نامعتبر پیام توکن نامعتبر، و برای OTP نامعتبر پیام کد نادرست/منقضی باید ببینید.

موفقیت این شش گام، تأیید انسانی این کارت است. PWA واقعی هنوز به این UI متصل نیست؛ آن کار `P00-PWA-AUTH-WEB-022` است و فقط پس از تأیید این کارت READY می‌شود.

## تأیید مالک

در 2026-10-26 مالک اجرای همهٔ موارد QA دستی این سند را تأیید کرد.
