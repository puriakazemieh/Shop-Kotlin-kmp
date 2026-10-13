<div dir="rtl" align="right">

# P04-FEATURE-CODE-050 — چرخهٔ دو لایهٔ قابلیت در Theme و Plugin

- Status: TODO
- Phase/Area/Type: P04 / FEATURE / CODE
- Priority/Risk/Size: P0 / HIGH / M
- Owner: BOTH
- Completion authority: BOTH؛ UI/network/migration نیازمند Manual QA
- Depends on: P04-STOREFRONT-CODE-049
- Blocks: P04-WORDPRESS-CODE-051
- Requirement source: درخواست مالک محصول 2026-10-13.

## هدف قابل اندازه‌گیری

هر capability دو دروازه داشته باشد: ceiling تغییرناپذیر در ZIP/artifact هنگام build و toggle مؤثر مدیر همان host پس از نصب. Theme-only و Plugin-only هر دو از registry مشترک module/schema/route/API/admin/job استفاده می‌کنند و به حضور محصول دیگر وابسته نیستند.

## Acceptance

- artifact فاقد feature امکان load یا فعال‌سازی آن را ندارد؛ مدیر از ceiling/dependency عبور نمی‌کند.
- toggle مجاز UI،route،API،write و job را هم‌زمان deny/allow می‌کند و دادهٔ مجاز را حذف نمی‌کند.
- Theme-only،Plugin-only و co-install برای featureهای Gate-passed،بدون duplicate job/write و با migration امن کار می‌کنند.

## Scope و Verification

- مجاز: `wordpress/packages/carmilla-core/**`،`wordpress/carmilla-theme/**`،`wordpress/carmilla-bridge/**`،build/test tooling و Evidence.
- ممنوع: فروش verticalی که Gate خودش را پاس نکرده، entitlement آنلاین اجباری یا وابستگی Theme↔Plugin.
- Manual QA: یک ZIP بدون feature و یک ZIP با feature بسازید؛ در حالت دوم آن را از پنل host روشن/خاموش و route/API مستقیم را آزمون کنید. موفقیت: ceiling و toggle هر دو enforce شوند. تا تأیید انسان `AWAITING_MANUAL_QA`.

## Completion record

- Started at:
- Completed at:
- Evidence paths:
- Final status: TODO

</div>
