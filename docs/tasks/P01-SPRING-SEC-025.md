# P01-SPRING-SEC-025 — پیاده‌سازی Paywall و محافظت از فایل‌های خصوصی در Spring Boot

- Status: TODO
- Phase/Area/Type: P01 / SECURITY / SEC
- Priority/Risk/Size: P0/HIGH / M
- Owner: AI
- Completion authority: BOTH
- Depends on: P01-SPRING-SEC-024
- Blocks: P01-SPRING-SEC-026
- Requirement source: Source Audit P0-07 (Section 6.3 و 13)

## هدف قابل اندازه‌گیری
اطمینان از اینکه فایل‌های دوره‌های آموزشی و رکوردهای درمانی فقط توسط کاربران دارای مجوز (خریداری شده) قابل دسترسی هستند.

## Threat/Exploit Surface
- دسترسی مستقیم به فولدر `/uploads/**` بدون چک کردن Auth/Entitlement.
- امکان ثبت‌نام در دوره‌های پولی بدون پرداخت وجه (P0-07).

## خروجی مورد انتظار
- سرویس Entitlement مرکزی برای چک کردن دسترسی به محتوا.
- استفاده از Signed URL یا Stream محافظت شده برای فایل‌های حساس.

## خارج از محدوده
- آپلود فایل‌های جدید.
- تغییر UI نمایش ویدئو.

## Preconditions
- تسک P01-SPRING-SEC-024 تمام شده باشد.

## Allowed files/directories
- `ShopServer/Shop/src/main/kotlin/**`

## Forbidden actions
- تغییر کدهای کلاینت.

## مراحل پیاده‌سازی
1. اصلاح `CourseService` برای اجبار چک کردن خرید قبل از Enrollment.
2. انتقال فایل‌های حساس از فولدر عمومی به یک فضای محافظت شده.
3. پیاده‌سازی endpoint دریافت URL موقت (Signed URL) برای فایل‌ها.
4. تست دسترسی غیرمجاز به فایل با داشتن لینک مستقیم.

## Automated tests با command و expected result
- تست دانلود فایل بدون توکن معتبر یا بدون خرید دوره باید با شکست مواجه شود.

## Manual tests با environment/data/steps/expected
- کپی کردن لینک یک ویدئو از کنسول مرورگر و تلاش برای باز کردن آن در حالت Incognito.
- انتظار: خطای 403 یا Expired.

## Security/Privacy/Migration checks
- انقضای کوتاه مدت لینک‌های امضا شده.

## Rollback
- بازگشت به حالت فایل‌های عمومی در صورت خرابی جدی در نمایش محتوا برای خریداران.
