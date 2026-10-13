<div dir="rtl" align="right">

# P04-STOREFRONT-CODE-049 — قرارداد طراحی فروشگاه در Theme مستقل

- Status: TODO
- Phase/Area/Type: P04 / STOREFRONT / CODE
- Priority/Risk/Size: P0 / HIGH / M
- Owner: BOTH
- Completion authority: BOTH؛ UI نیازمند Manual QA
- Depends on: P04-WPPLUGIN-CODE-047
- Blocks: P04-FEATURE-CODE-050
- Requirement source: درخواست مالک محصول 2026-10-13.

## هدف قابل اندازه‌گیری

Theme مستقل، فروشگاه Woo را با token و قرارداد قابل آزمون هم‌راستا با Kotlin نمایش دهد: catalog،detail،cart و stateهای loading/empty/error/feature-off در RTL و breakpointهای اصلی. این parity طراحی است، نه embed کردن KMP یا انتقال ownership Woo.

## Acceptance

- contract نسخه‌دار token/layout/typography/spacing/navigation و exceptionهای native ثبت شود.
- Theme-only با داده synthetic Woo همان معنی قیمت،موجودی و وضعیت‌ها را به PWA/Android نشان دهد.
- golden/visual regression برای mobile و desktop RTL وجود داشته باشد.

## Scope و Verification

- مجاز: `wordpress/carmilla-theme/**`،`wordpress/packages/carmilla-core/**`،تست‌ها و `docs/evidence/P04-STOREFRONT-CODE-049/`.
- ممنوع: embed KMP،وابسته‌کردن Theme به Bridge،تغییر داده مشتری یا ادعای iOS/Desktop.
- Manual QA: Theme-only را با catalog synthetic باز کنید، محصول را به cart ببرید و شبکه را قطع کنید. موفقیت: طراحی/RTL و stateها مطابق contract و بدون وابستگی Bridge باشند. تا تأیید انسان `AWAITING_MANUAL_QA`.

## Completion record

- Started at:
- Completed at:
- Evidence paths:
- Final status: TODO

</div>
