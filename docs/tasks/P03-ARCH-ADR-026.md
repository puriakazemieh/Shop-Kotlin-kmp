<div dir="rtl" align="right">

# P03-ARCH-ADR-026 — قرارداد BackendProfile و mode مستقل WordPress

- Status: TODO
- Phase/Area/Type: P03 / ARCH / ADR
- Priority/Risk/Size: P0 / HIGH / S
- Owner: BOTH
- Completion authority: BOTH؛ تغییر runtime/network نیازمند Manual QA
- Depends on: P03-ARCH-CODE-024B
- Blocks: P03-MANIFEST-CODE-025
- Requirement source: درخواست مالک محصول 2026-10-13؛ تفکیک مبنای P03/P04 از پذیرش artifact در P12.

## هدف قابل اندازه‌گیری

قرارداد نسخه‌دار را نهایی کن: `BackendProfile` فقط `WORDPRESS` یا `SPRING` است؛ provider mode وردپرس مستقل و فقط `THEME_ONLY`،`PLUGIN_ONLY` یا `CO_INSTALL` است. BuildSpec trusted این دو را تعیین می‌کند و branding،query یا manifest راه دور حق تغییر origin یا mode را ندارند.

## خروجی و معیار پذیرش

- owner قرارداد،schema/version و مسیر canonical manifest/REST برای سه mode وردپرس مشخص است.
- قرارداد sync،namespace token/cache و رفتار logout/tenant برای هر mode مشخص است.
- Spring provider با WordPress mode اشتباه گرفته نمی‌شود؛ backend سوم یا انتخاب آزاد host افزوده نمی‌شود.
- negative cases برای origin/mode نامعتبر،tenant mismatch و manifest ناسازگار تعریف شده‌اند.

## محدوده و منع‌ها

- مجاز: `core/config/capabilities/**`،`core/network/**`،مستندات قرارداد و Evidence همین کارت.
- ممنوع: پیاده‌سازی host adapter،تغییر endpoint عملیاتی،داده واقعی یا انتشار؛ این‌ها به P04-WORDPRESS-CODE-051 و Gateهای بعدی تعلق دارند.

## Verification و Manual QA

- تست قرارداد/serialization و caseهای منفی اجرا و در `docs/evidence/P03-ARCH-ADR-026/` ثبت شوند.
- Manual QA: در staging با BuildSpec ثابت، هر سه mode WordPress را باز کنید و تلاش کنید با query/branding origin یا mode را عوض کنید. موفقیت: انتخاب رد شود و cache/token tenant دیگر استفاده نشود.
- تا ثبت tester/date/build/result، وضعیت `AWAITING_MANUAL_QA` است.

## Completion record

- Started at:
- Completed at:
- Evidence paths:
- Final status: TODO

</div>
