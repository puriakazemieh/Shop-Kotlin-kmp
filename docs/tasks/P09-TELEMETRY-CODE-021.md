# P09-TELEMETRY-CODE-021 — اتصال Kotlin و رضایت

**Status:** DONE

مرحله ۳: transport مشترک PWA/Android، persistence رضایت و call-siteهای whitelist‌شده را پیاده می‌کند؛ opt-out باید ارسال را قطع کند.

## Manual QA

1. PWA تست را باز کنید و از پروفایل، صفحهٔ «تنظیمات» را باز کنید.
2. در بخش «آمار ناشناس»، کلید «ارسال آمار ناشناس و تجمیعی» را روشن کنید، سپس صفحه را یک‌بار reload کنید.
3. در WordPress به **Carmilla → Telemetry** بروید و وجود/افزایش bucket با نام `app_activated` و client `pwa` را بررسی کنید.
4. کلید را خاموش کنید، صفحه را دوباره reload کنید و مطمئن شوید شمارنده افزایش پیدا نمی‌کند.

**Success criteria:** رضایت پس از reload حفظ می‌شود؛ فقط زمان روشن بودن رضایت، یک رویداد aggregate جدید ثبت می‌شود؛ هیچ نام، ایمیل، توکن، متن یا شناسهٔ کاربر در جدول نمایش داده نمی‌شود.

## Completion record

- 2026-10-13 — تأیید دستی مالک محصول: PASS. opt-in، ذخیرهٔ رضایت، ارسال `app_activated` و توقف ارسال پس از opt-out در PWA QA تأیید شد.
- Evidence: `docs/evidence/P09-TELEMETRY-CODE-021/summary.md`

