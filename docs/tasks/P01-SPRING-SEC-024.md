# P01-SPRING-SEC-024 — بستن کامل RBAC و IDOR در سرور Spring Boot

- Status: BLOCKED
- Phase/Area/Type: P01 / SECURITY / SEC
- Priority/Risk/Size: P0/HIGH / M
- Owner: AI
- Completion authority: BOTH
- Depends on: P01-QA-MANUAL-021
- Blocks: P01-SPRING-SEC-025
- Requirement source: Source Audit P0-06 (Section 6.3 و 13)

## هدف قابل اندازه‌گیری
جلوگیری از دسترسی غیرمجاز به منابع کاربران دیگر (IDOR) و اجبار نقش ادمین برای تمام مسیرهای مدیریتی در Spring.

## Threat/Exploit Surface
- مسیرهای `/api/admin/**` که فاقد `@PreAuthorize` هستند.
- متدهای تغییر وضعیت سفارش و حمل‌ونقل که مالکیت را چک نمی‌کنند.
- دسترسی به پروفایل و رکوردهای درمانی کاربران دیگر با تغییر ID در URL.

## خروجی مورد انتظار
- اعمال `deny-by-default` روی تمام endpointهای ادمین.
- تست خودکار IDOR برای تمام منابع حساس (Order, Patient, File).

## خارج از محدوده
- پیاده‌سازی نقش‌های جدید (فقط نقش‌های موجود ادمین و کاربر فعلاً Harden شوند).
- تغییر منطق بیزینس (فقط Authorization).

## Preconditions
- Status باید READY باشد.
- محیط تست Spring با دیتابیس ایزوله (Testcontainers) آماده باشد.

## Allowed files/directories
- `ShopServer/Shop/src/main/kotlin/**`

## Forbidden actions
- تغییر کدهای کلاینت.
- دستکاری داده‌های پروداکشن.

## مراحل پیاده‌سازی
1. ممیزی تمام Controllerهای Spring برای یافتن متدهای بدون `@PreAuthorize`.
2. اضافه کردن `@PreAuthorize("hasRole('ADMIN')")` به تمام متدهای مدیریتی مفقوده.
3. اضافه کردن چک مالکیت (Ownership check) در لایه Service برای متدهای کاربر (مانند مشاهده سفارش خود).
4. نوشتن تست‌های Integration امنیتی برای تأیید مسدود بودن IDOR.

## Automated tests با command و expected result
- Command: `.\gradlew.bat :test` (در پوشه سرور)
- Expected: تمام تست‌های امنیتی پاس شوند و درخواست با ID اشتباه خطای 403 یا 404 بدهد.

## Manual tests با environment/data/steps/expected
- ورود با کاربر A و تلاش برای خواندن سفارش کاربر B.
- انتظار: خطای Access Denied یا عدم یافتن منبع.

## Security/Privacy/Migration checks
- عدم نشت اطلاعات کاربران در پیام‌های خطا.

## Rollback
- بازگشت به کامیت قبلی در صورت اختلال در دسترسی‌های قانونی.
