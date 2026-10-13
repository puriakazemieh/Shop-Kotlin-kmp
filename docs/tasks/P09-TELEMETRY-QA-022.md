# P09-TELEMETRY-QA-022 — Evidence واقعی telemetry

**Status:** AWAITING_MANUAL_QA

مرحله ۴: eventهای synthetic، dashboard، consent/opt-out و payload redaction را در محیط QA اثبات و برای تأیید انسانی تحویل می‌دهد.

## Automated QA — 2026-10-13

- رویداد مجاز و consented (`catalog_viewed`) از endpoint واقعی WordPress پاسخ `202` گرفت.
- همان رویداد با `consent:false` پاسخ `400` گرفت.
- نام رویداد خارج از allowlist پاسخ `400` گرفت.
- درخواست دارای فیلدهای اضافهٔ synthetic (`email` و `token`) پاسخ `202` گرفت، اما controller فقط date/tenant/client/event/count را در aggregate نگه می‌دارد.
- retention نودروزه در controller افزوده شد و با `php -l` در source و نصب QA lint شد.

## Manual QA

1. در PWA تست، رضایت را روشن کرده و پس از reload یک event جدید بسازید.
2. در **Carmilla → Telemetry** فقط ستون‌های bucket و count را بررسی کنید؛ هیچ email، token یا متن آزاد نباید دیده شود.
3. رضایت را خاموش کنید، reload کنید و افزایش‌نیافتن شمارنده را تأیید کنید.
4. نتیجه را با `PASS` یا شرح failure اعلام کنید.
