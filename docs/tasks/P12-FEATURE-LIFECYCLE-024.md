# P12-FEATURE-LIFECYCLE-024 — چرخه قابلیت مستقل در Theme و Plugin

- Status: TODO
- Phase/Area/Type: P12 / FEATURE / CODE
- Priority/Risk/Size: P0/HIGH / HIGH / M
- Owner: BOTH
- Completion authority: BOTH
- Depends on: P12-STOREFRONT-DESIGN-023, P04-WORDPRESS-GATE-037
- Blocks: P12-CLIENT-HOST-ADAPTER-025
- Requirement source: درخواست مالک محصول 2026-10-13؛ تکمیل صریح P04 entitlement و Gateهای دامنه P13/P14.

## هدف قابل اندازه‌گیری

Theme مستقل و Bridge مستقل هر دو همه قابلیت‌های deliverable همان نسخه را بدون نیاز به محصول دیگر ارائه کنند. هر feature دو دروازه دارد: سقف immutable هنگام ساخت artifact و toggle مؤثر مدیر همان host پس از نصب.

## خروجی و معیار پذیرش

- registry یکتا feature→module/schema/migration/route/API/admin UI/dependency برای Theme و Plugin؛ قابلیت domain که Gate جداگانه دارد فقط پس از Gate خودش قابل فروش/فعال‌سازی است.
- BuildSpec/entitlement قابلیت خریداری‌نشده را داخل ZIP یا artifact مجاز نکند؛ مدیر نیز نتواند از ceiling یا dependency عبور کند.
- اگر feature در build موجود و مجاز است، مدیر Theme بدون Bridge و مدیر Bridge روی قالب ثالث بدون Carmilla Theme بتواند آن را روشن/خاموش کند.
- خاموش‌کردن، widget/menu/route/API/write/job را deny کند اما داده مجاز را حذف یا migration مخرب نکند؛ روشن‌کردن مجدد lifecycle امن داشته باشد.
- matrix Theme-only،Plugin-only و co-install برای capabilityهای فروشگاهی و هر vertical Gate-passed پاس شود.

## محدوده و منع‌ها

- مجاز: `wordpress/packages/carmilla-core/**`،`wordpress/carmilla-theme/**`،`wordpress/carmilla-bridge/**`،`tools/**`،`docs/**` و تست‌ها.
- ممنوع: فروش یا ادعای آماده‌بودن LMS/Clinic/پرداخت پیش از Gate همان فاز؛ online entitlement اجباری برای کارکرد عادی سایت؛ وابستگی Theme↔Plugin.

## Verification

- تست install/upgrade/disable/enable و negative route/API/write برای هر host و artifact ceiling.
- UAT انسانی با داده synthetic: Theme-only،Plugin-only روی Storefront/third-party theme و co-install.
- Evidence: `docs/evidence/P12-FEATURE-LIFECYCLE-024/`; بدون Manual QA، DONE ممنوع است.

- Final status: TODO
