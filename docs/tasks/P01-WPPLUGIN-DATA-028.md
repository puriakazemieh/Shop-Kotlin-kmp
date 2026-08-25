# P01-WPPLUGIN-DATA-028 — اصلاح IDهای محلی و Ledger در وردپرس (P0-11)

- Status: DONE
- Phase/Area/Type: P01 / SECURITY / DATA
- Priority/Risk/Size: P0/HIGH / M
- Owner: AI
- Completion authority: BOTH
- Depends on: P01-SPRING-DATA-027
- Blocks: P01-WPPLUGIN-ARCH-029
- Requirement source: Source Audit P0-11 (Section 7.4 و 13)

## هدف قابل اندازه‌گیری
رفع تداخل IDهای کاربران مختلف در متادیتاهای وردپرس و ایجاد یک Ledger پایدار برای تراکنش‌های مالی.

## Threat/Exploit Surface
- استفاده از IDهای ترتیبی (1, 2, 3...) داخل `user_meta` که برای هر کاربر مستقل است و باعث تداخل در جست‌وجوی ادمین می‌شود.
- ریسک تغییر رکورد اشتباه توسط ادمین به دلیل نبود ID سراسری (UUID یا Table ID).

## خروجی مورد انتظار
- انتقال رکوردهای مالی از متادیتا به یک جدول دیتابیس اختصاصی با ID یکتا.
- مهاجرت (Migration) رکوردهای قبلی بدون از دست رفتن داده.

## خارج از محدوده
- تغییرات کلاینت اندروید (فعلاً با سازگاری عقب‌رو).

## Preconditions
- تسک P01-SPRING-DATA-027 تمام شده باشد.

## Allowed files/directories
- `wordpress/carmilla-bridge/**`

## Forbidden actions
- حذف متادیتای قدیمی قبل از اطمینان از موفقیت مهاجرت.

## مراحل پیاده‌سازی
1. ایجاد جدول `wp_cb_ledger` در دیتابیس وردپرس.
2. نوشتن اسکریپت انتقال داده از `user_meta` به جدول جدید.
3. اصلاح کدهای افزونه برای خواندن و نوشتن از جدول جدید به جای متادیتا.
4. تست سناریوی تداخل ID بین دو کاربر آزمایشی.

## Automated tests با command و expected result
- تست SQL برای چک کردن Unique Constraint روی ID تراکنش‌ها.

## Manual tests با environment/data/steps/expected
- ایجاد دو تراکنش برای دو کاربر مختلف؛ بررسی اینکه IDهای آن‌ها در سیستم ادمین متمایز و درست است.

## Rollback
- بازگشت به سیستم متادیتا با استفاده از بک‌آپ دیتابیس.
