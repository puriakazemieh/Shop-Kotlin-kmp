<div dir="rtl" align="right">

# ADR-013 — Telemetry داخلی، opt-in و privacy-first

- تاریخ: 2026-10-13
- وضعیت: مصوب برای پیاده‌سازی P09؛ راه‌اندازی runtime و پذیرش انسانی هنوز انجام نشده است.
- مرجع: `P09-TELEMETRY-ADR-019` و توقف‌گاه P09.

## تصمیم

منبع اصلی telemetry یک قرارداد داخلی نسخه‌دار است. WordPress/Bridge نخستین provider آن است؛ Spring در فاز خودش همان قرارداد را implement می‌کند. Firebase فقط adapter اختیاری crash/performance برای اپ native است و مرجع دادهٔ محصول یا فروش نیست.

## قرارداد

هر event فقط این فیلدها را دارد: `schemaVersion`،`name`،`occurredAt`،`anonymousId` تصادفی،`tenantId`،`client`،`buildFingerprint` و properties whitelist شده. نام‌های مجاز: `app_activated`،`pwa_installed`،`catalog_viewed`،`cart_changed`،`checkout_started`،`checkout_result`،`app_error` و `support_opened`.

شناسهٔ کاربر، email،phone،address،token،cookie،payment authority/card،متن آزاد،URL query،دادهٔ سلامت و پاسخ آزمون هرگز پذیرفته یا ذخیره نمی‌شوند. `anonymousId` با logout/reset عوض می‌شود. backend فقط aggregate و payload redacted نگه می‌دارد؛ retention پیش‌فرض 90 روز است.

## رضایت و امنیت

پیش‌فرض opt-out است. تا ثبت رضایت صریح، SDK/transport هیچ درخواست telemetry نمی‌سازد. opt-out باید صف محلی را حذف و ارسال بعدی را قطع کند. endpoint rate-limited،origin-bound و بدون cookie/session حساس است؛ dashboard فقط `manage_options` دارد. ثبت event معتبر به معنای مجوز analytics/ads personalization نیست.

## مرزها و پذیرش

مرحله P09 فقط دادهٔ synthetic و dashboard QA را می‌پذیرد. Firebase/GA/PostHog بدون card/adapter جدا فعال نمی‌شوند. P09-TELEMETRY-CODE-020 backend/dashboard،021 client/consent و022 evidence واقعی را انجام می‌دهند؛ این ADR مجوز PASS Gate یا انتشار نیست.

</div>
