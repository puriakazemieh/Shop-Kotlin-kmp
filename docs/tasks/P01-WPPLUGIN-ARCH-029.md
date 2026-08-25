# P01-WPPLUGIN-ARCH-029 — حذف وضعیت Split-brain میان پوسته و افزونه وردپرس

- Status: BLOCKED
- Phase/Area/Type: P01 / SECURITY / ARCH
- Priority/Risk/Size: P0/HIGH / M
- Owner: AI
- Completion authority: BOTH
- Depends on: P01-WPPLUGIN-DATA-028
- Blocks: P01-SECURITY-SEC-022
- Requirement source: Source Audit P0-13 (Section 7.3 و 13)

## هدف قابل اندازه‌گیری
توقف نوشتن بیزنس لاجیک در پوسته (Theme) و انتقال تمام وظایف داده و منطق به افزونه هسته (Plugin).

## Threat/Exploit Surface
- ذخیره داده‌های حساس (مانند رزرو و پیام) در ساختارهای متفاوت توسط پوسته و افزونه.
- گم شدن داده‌ها با تعویض پوسته.
- عدم امکان ممیزی امنیتی متمرکز به دلیل پخش بودن کدها.

## خروجی مورد انتظار
- پوسته فقط شامل فایل‌های CSS/JS و Template باشد (Presentation-only).
- افزونه تنها مالک رکوردهای دیتابیس و APIها باشد.

## خارج از محدوده
- ریفکتور کامل UI پوسته.

## Preconditions
- تسک P01-WPPLUGIN-DATA-028 تمام شده باشد.

## Allowed files/directories
- `wordpress/carmilla-theme/**`
- `wordpress/carmilla-bridge/**`

## Forbidden actions
- حذف کدهای پوسته قبل از انتقال کامل به افزونه.

## مراحل پیاده‌سازی
1. شناسایی تمام توابعی در پوسته که عمل Write (ذخیره در DB) انجام می‌دهند.
2. انتقال این توابع به کلاس‌های مناسب در افزونه.
3. فراخوانی APIهای افزونه توسط پوسته به جای دسترسی مستقیم به DB.
4. غیرفعال کردن کدهای تکراری در پوسته.

## Automated tests با command و expected result
- تست یکپارچگی: لغو نوبت یا ثبت پیام در پوسته باید در جداول افزونه منعکس شود.

## Manual tests با environment/data/steps/expected
- تغییر پوسته به یک پوسته پیش‌فرض وردپرس و چک کردن اینکه آیا داده‌های افزونه (مانند دوره‌های آموزشی) هنوز در API در دسترس هستند.
- انتظار: داده‌ها نباید وابسته به فعال بودن پوسته Carmilla باشند.

## Rollback
- بازگرداندن فایل‌های پوسته از ورژن کنترل.
