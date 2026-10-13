# P12-STOREFRONT-DESIGN-023 — برابری طراحی فروشگاه WordPress و کلاینت Kotlin

- Status: TODO
- Phase/Area/Type: P12 / STOREFRONT / DESIGN
- Priority/Risk/Size: P0/HIGH / HIGH / M
- Owner: BOTH
- Completion authority: BOTH
- Depends on: P12-QA-MANUAL-022, P04-WORDPRESS-GATE-037
- Blocks: P12-FEATURE-LIFECYCLE-024
- Requirement source: درخواست مالک محصول 2026-10-13؛ تکمیل صریح P04-WPTHEME-CODE-030، P08 و P12-BUILDER-CODE-012.

## هدف قابل اندازه‌گیری

فروشگاه Theme WordPress از همان قرارداد طراحی و stateهای فروشگاه Kotlin استفاده کند؛ این به معنی embed کردن KMP در WordPress نیست. tokenها، ساختار صفحه و رفتارهای قابل مشاهده باید یک specification مشترک و قابل آزمون داشته باشند.

## خروجی و معیار پذیرش

- یک design contract نسخه‌دار برای رنگ، typography، spacing، card، navigation، RTL و breakpointها ایجاد شود.
- صفحه‌های catalog، detail، cart، empty/loading/error و حالت feature-off در Theme، PWA و Android golden screenshot داشته باشند.
- داده synthetic واحد از Woo/REST در هر سه client نشان داده شود؛ فیلد یا قیمت canonical متفاوت نباشد.
- اختلاف عمدی platform-native مستند شود؛ شباهت مبهم یا "تقریباً مشابه" معیار PASS نیست.

## محدوده و منع‌ها

- مجاز: `wordpress/carmilla-theme/**`،`wordpress/packages/carmilla-core/**`،`composeApp/**`،`androidApp/**`،`core/designSystem/**`،`docs/**` و harness/test همان‌ها.
- ممنوع: تغییر ownership داده، clone کردن UI Kotlin داخل WordPress، secret/PII، یا ادعای parity iOS/Desktop پیش از Gateهای P16/P17.

## Verification

- characterization و visual-regression با viewportهای RTL موبایل/desktop؛ command و artifact در Evidence ثبت شود.
- UAT انسانی: WordPress Theme،PWA و Android با catalog synthetic یکسان باز شوند؛ مسیر cart و خطای شبکه نیز مقایسه شود.
- Success: تفاوت خارج از specification وجود نداشته باشد و هر exception ownerدار باشد.

## Evidence و وضعیت

- مسیر: `docs/evidence/P12-STOREFRONT-DESIGN-023/`
- تا ثبت tester/date/build/fingerprint و تأیید انسانی، Status باید `AWAITING_MANUAL_QA` بماند.
- Final status: TODO
