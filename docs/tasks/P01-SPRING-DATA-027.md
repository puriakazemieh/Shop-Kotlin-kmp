# P01-SPRING-DATA-027 — استراتژی مهاجرت دیتابیس (Migration) برای پروداکشن در Spring

- Status: BLOCKED
- Phase/Area/Type: P01 / SECURITY / DATA
- Priority/Risk/Size: P0/HIGH / S
- Owner: AI
- Completion authority: BOTH
- Depends on: P01-SPRING-SEC-026
- Blocks: P01-WPPLUGIN-DATA-028
- Requirement source: Source Audit P0-09 (Section 6.3 و 13)

## هدف قابل اندازه‌گیری
جلوگیری از حذف تصادفی یا خرابی داده‌ها در زمان آپدیت Schema دیتابیس در محیط پروداکشن.

## Threat/Exploit Surface
- استفاده از `ddl-auto=update` در محیط عملیاتی که ریسک تخریب داده دارد.
- نبود نسخه مشخص برای تغییرات SQL.

## خروجی مورد انتظار
- غیرفعال کردن `ddl-auto` در پروفایل پروداکشن.
- تنظیم Flyway یا Liquibase برای مدیریت نسخه‌های دیتابیس.

## خارج از محدوده
- ریفکتور بزرگ تیبل‌ها.
- دیتابیس‌های غیر از PostgreSQL.

## Preconditions
- تسک P01-SPRING-SEC-026 تمام شده باشد.

## Allowed files/directories
- `ShopServer/Shop/src/main/resources/**`
- `ShopServer/Shop/src/main/kotlin/**`

## Forbidden actions
- حذف داده‌های موجود کاربر بدون بک‌آپ.

## مراحل پیاده‌سازی
1. اضافه کردن وابستگی Flyway به Gradle.
2. استخراج Schema فعلی به اولین فایل Migration (V1__init.sql).
3. تنظیم `spring.jpa.hibernate.ddl-auto=validate` برای پروداکشن.
4. تست اجرای Migration روی یک دیتابیس خالی و یک دیتابیس دارای داده قدیمی.

## Automated tests با command e expected result
- اجرای موفقیت‌آمیز اپلیکیشن با وضعیت validate.

## Manual tests با environment/data/steps/expected
- بررسی لاگ‌های استارتاپ برای اطمینان از کنترل Flyway.

## Rollback
- بازگشت به تنظیمات ddl-auto قبلی در صورت شکست در محیط تست.
