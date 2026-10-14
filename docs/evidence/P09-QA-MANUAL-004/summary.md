# Manual QA Evidence Handoff / تحویل شواهد تست دستی: P09-QA-MANUAL-004

- **Task ID / شناسه تسک:** P09-QA-MANUAL-004
- **Date / تاریخ:** 2026-09-06
- **Title / عنوان:** مجموعه تست‌های کامل کارکردی (Functional Suite) فروشگاه

## Test Instructions for User / دستورالعمل تست برای کاربر
### کجا نگاه کنید
- اپلیکیشن کلاینت (اندروید / وب / PWA)
- جریان خرید و تسویه‌حساب فروشگاه
- پیشخوان وردپرس بخش سفارشات ووکامرس

### مراحل تست
1. **ورود و احراز هویت:** ورود با رمز عبور و پیامک OTP را تست کنید.
2. **کاتالوگ و جستجو:** صفحه اصلی، دسته‌بندی‌ها و جزییات کالا را بررسی کنید.
3. **سبد خرید:** افزودن، تغییر تعداد و حذف کالا را تست کنید.
4. **تسویه‌حساب و پرداخت:** خرید موفق را تا ثبت نهایی سفارش در ووکامرس تست کنید.
5. **انصراف از پرداخت:** درگاه را باز کرده و انصراف دهید؛ تایید کنید سبد خرید **پاک نمیشود**.

## Status / وضعیت
- **Final Status / وضعیت نهایی:** AWAITING_MANUAL_QA (در انتظار تست دستی)

## اجرای محدود QA PWA — 2026-10-14

### محیط

- Release candidate محلی: `http://localhost/carmilla-pwa-qa/`
- backend: WordPress QA با fixture کاملاً synthetic `Carmilla QA Product`

### نتایج

| مسیر | نتیجه | Evidence |
|---|---|---|
| Home و catalog | PASS | دسته، محصول، قیمت `100,000 تومان` و موجودی نمایش داده شدند. |
| Product detail | PASS | صفحهٔ `/product/carmilla-qa-product` با موجودی `999` و قیمت صحیح رندر شد. |
| Add to cart برای مهمان | **FAIL / blocker** | URL به `/login` تغییر کرد، اما پس از reload خطای `Cannot find startDestination Screen.Login from NavGraph` رخ داد و UI روی loading ماند. |

### اثر و اقدام لازم

تا وقتی route `Screen.Login` در navigation graph اصلاح و بازآزمایی نشود، مسیر
critical cart/checkout/payment-return قابل قبول نیست. هیچ سفارش یا پرداختی در
این اجرا ساخته نشد و PWA در پایان به صفحهٔ Home بازگردانده شد. کارت همچنان
`AWAITING_MANUAL_QA` است.

## اصلاح مسیر ورود — 2026-10-14

- علت route اولیه مشخص شد: `Login` مقصد داخلیِ `AuthGraph` است و نباید مستقیماً
  start destination یک `NavHost` باشد. نگاشت browser hash برای `/login` به
  `Screen.AuthGraph` تغییر کرد تا graph والد، مقصد `Login` را بسازد.
- cache/version bundle از `v5` به `v6` افزایش یافت و artifact جدید در
  `C:\xampp\htdocs\carmilla-pwa-qa` مستقر شد؛ `app-config-wp.json` دست‌نخورده
  و همچنان متصل به WordPress QA است.
- `:composeApp:compileKotlinJs` و
  `:composeApp:jsBrowserDevelopmentExecutableDistribution` با exit code 0 پاس
  شدند.

### نتیجهٔ بازآزمایی

تأیید بصری بعد از reload هنوز باید توسط انسان انجام شود؛ ابزار مرورگر هنگام
diagnostic متوقف شد، بنابراین این اصلاح به‌تنهایی PASS اعلام نمی‌شود. کارت
همچنان `AWAITING_MANUAL_QA` است.
