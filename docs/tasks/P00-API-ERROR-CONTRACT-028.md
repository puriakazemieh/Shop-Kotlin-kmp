# P00-API-ERROR-CONTRACT-028 — قرارداد خطای قابل‌فهم PWA و WordPress

- Status: AWAITING_MANUAL_QA; Size: M; Owner: BOTH; Risk: HIGH
- Depends on: `P00-PWA-AUTH-CODE-020`.
- Blocks: QA معتبرِ ورود/ثبت‌نام PWA و هر ادعای «پیام خطای قابل‌فهم» در فاز صفر.
- دلیل ایجاد: در اجرای واقعی ۲۰ اکتبر، WordPress `status` را عددی ارسال می‌کند ولی مدل PWA آن را متنی می‌خواند؛ decode کل envelope خطا شکست می‌خورد و حتی `BAD_CREDENTIALS` به پیام عمومی تبدیل می‌شود. افزون بر آن، بسیاری از `errorCode`های فعال WordPress در mapper کلاینت ثبت نشده‌اند.

## هدف

یک پاسخ خطای امن، قابل‌فهم و قابل‌آزمون برای PWA و Bridge پیاده شود. کاربر نباید «خطای نامشخص رخ داد» ببیند. اگر علت از روی قرارداد قابل تشخیص نیست، متن باید علت عملیاتیِ صادقانه را بگوید؛ مانند «پاسخ نامعتبر از سرور دریافت شد» یا «ارتباط با سرور برقرار نشد»، نه اینکه خطای داخلی یا detail حساس سرور نمایش داده شود.

## رفتار مورد انتظار

1. مدل PWA باید envelope فعلی WordPress با `status` عددی و envelope انتقالی Spring با `status` متنی را بخواند و `errorCode` را از دست ندهد. قرارداد canonical آینده، `status` و `code` عددی HTTP است.
2. هر `errorCode` قابل‌ارسال از routeهای فعال Bridge باید یا به متن فارسی دقیق و امن نگاشت شود، یا در registry صریحاً منع شده باشد. fallback باید بدون افشای exception/SQL/provider، علت دسته‌ای را نشان دهد: شبکه، timeout، پاسخ نامعتبر، احراز هویت لازم، مجوز ناکافی، قابلیت خاموش، پیکربندی ناقص یا خطای موقت سرویس.
3. ثبت‌نام باید علت دقیق را از هم جدا کند: ایمیل/شمارهٔ تکراری، ورودی ناقص یا نامعتبر، رمز ضعیف، rate limit، سرویس موقتاً در دسترس نیست، و خطای ناشناختهٔ امن. `REGISTER_FAILED` نباید برای تمام علت‌ها استفاده شود.
4. ورود با رمز و OTP باید برای رمز/کد اشتباه، کد منقضی، cooldown، OTP unavailable و حساب غیرفعال پیام مستقل داشته باشند.
5. loading در ورود، ثبت‌نام، بازیابی و تأیید OTP در تمام success/error/cancellation pathها پایان یابد و دوبارزدن دکمه درخواست موازی نسازد.
6. متن‌های خام provider، stack trace، exception، SQL، token، شمارهٔ همراه یا ایمیل نباید در message bar یا evidence ثبت شوند.

## محدودهٔ مجاز

- `core/common/**`, `core/network/**`, `core/data/**`, `feature/auth/**`, `composeApp/**`
- `wordpress/carmilla-bridge/includes/class-cb-auth-controller.php` و helperهای error مرتبط
- تست‌های محدود Kotlin/JS و PHP مربوط به خطاهای authentication
- `docs/evidence/P00-API-ERROR-CONTRACT-028/**` و فقط row همین Task

## خارج از محدوده

- فعال‌کردن یا دورزدن نشست browser-cookie؛ آن کار فقط در `P00-PWA-AUTH-WP-021` انجام می‌شود.
- parity کامل Spring و WordPress؛ آن کار در `P15-SPRING-API-004` انجام می‌شود.
- ذخیره‌کردن JWT در localStorage/sessionStorage یا نمایش raw server error.

## تست خودکار موردنیاز

- یک characterization test با پاسخ واقعی‌شکل WordPress (`status: 401`, `errorCode: BAD_CREDENTIALS`) که پیام «ایمیل یا رمز عبور اشتباه است» را اثبات کند.
- testهای خطای عددی/متنیِ transition که `errorCode` را حفظ کنند.
- test registry برای همهٔ `errorCode`های routeهای فعال Bridge؛ code جدید بدون mapping باید test را fail کند.
- testهای ViewModel برای پایان loading در failure/cancellation و عدم اجرای درخواست موازی.
- smoke PHP برای ثبت‌نام تکراری، رمز ضعیف و ورودی نامعتبر با `errorCode`های متمایز و بدون دادهٔ واقعی.

## QA دستی پس از کد

- کجا: PWA QA متصل به WordPress synthetic.
- چگونه: یک رمز اشتباه، یک ایمیل تکراری، یک رمز کوتاه، کد OTP اشتباه و یک درخواست در حالت OTP unavailable را اجرا کن.
- موفقیت: در هر مورد spinner متوقف شود، علت فارسیِ مناسب دیده شود، داده حساس دیده نشود و یک کلیک بیش از یک درخواست نسازد.
- Status پس از کد تا تأیید مالک: `AWAITING_MANUAL_QA`.

## Evidence و rollback

- Evidence: `docs/evidence/P00-API-ERROR-CONTRACT-028/` شامل command، نسخهٔ artifact و نتیجهٔ data-redacted.
- Rollback: forward-fix با حفظ envelope قبلی در parser؛ حذف یا تغییر خاموشِ errorCode مجاز نیست.

## اجرا — ۲۰۲۶-۱۰-۲۵

- decoder عددی/متنی، fallback امن بدون پیام «خطای نامشخص»، تفکیک خطاهای ثبت‌نام WordPress و smoke test disposable اجرا شدند.
- evidence و دو fixture ایزوله در [verification](../evidence/P00-API-ERROR-CONTRACT-028/verification.md) ثبت شده‌اند.
- به‌علت تغییر UI/network، تسک تا تأیید دستی مالک `AWAITING_MANUAL_QA` باقی می‌ماند.
