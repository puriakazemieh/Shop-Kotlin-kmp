# P01-SPRING-SEC-026 — لغو نوبت (Cancel Appointment) به‌صورت Idempotent در Spring

- Status: TODO
- Phase/Area/Type: P01 / SECURITY / SEC
- Priority/Risk/Size: P0/HIGH / S
- Owner: AI
- Completion authority: BOTH
- Depends on: P01-SPRING-SEC-025
- Blocks: P01-SPRING-DATA-027
- Requirement source: Source Audit P0-08 (Section 6.3 و 13)

## هدف قابل اندازه‌گیری
جلوگیری از افزایش غیرمجاز موجودی (Session Credit) با فراخوانی تکراری متد لغو نوبت.

## Threat/Exploit Surface
- فراخوانی متوالی API لغو نوبت (Race condition یا Replay).
- امکان لغو نوبتی که قبلاً لغو شده و دریافت اعتبار تکراری.

## خروجی مورد انتظار
- استفاده از Transition Guard در State Machine رزرو.
- تست Concurrency برای اطمینان از یک‌بار اعمال شدن لغو.

## خارج از محدوده
- سیستم رزرو جدید.
- تغییر درگاه پرداخت.

## Preconditions
- تسک P01-SPRING-SEC-025 تمام شده باشد.

## Allowed files/directories
- `ShopServer/Shop/src/main/kotlin/**`

## Forbidden actions
- تغییر کدهای کلاینت.

## مراحل پیاده‌سازی
1. چک کردن وضعیت فعلی Appointment قبل از لغو (باید در حالت BOOKED باشد).
2. استفاده از Transaction اتمیک برای تغییر وضعیت و شارژ کیف پول.
3. اضافه کردن قفل (Pessimistic یا Optimistic) روی رکورد رزرو.

## Automated tests با command و expected result
- تست ارسال ۲ درخواست همزمان برای لغو یک نوبت؛ فقط یکی باید موفق باشد.

## Manual tests با environment/data/steps/expected
- دبل‌کلیک روی دکمه لغو در اپلیکیشن (یا ارسال تکراری با Postman).
- انتظار: فقط یک بار افزایش اعتبار در کیف پول.

## Rollback
- بازگشت به نسخه قبلی در صورت بروز Deadlock در دیتابیس.
