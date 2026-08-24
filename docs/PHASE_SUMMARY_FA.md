# خلاصه ساده فازهای Carmilla

برای اجرای جزئیات به docs/tasks.md و کارت هر Task مراجعه کن.

## P00 — کنترل پروژه، Baseline، QA و Business Foundation

- هدف: بدون تغییر گسترده رفتار، نقطه شروع تکرارپذیر، scope نسخه اول، تصمیم‌های معماری، محیط تست و معیار موفقیت ساخته شود.
- Taskها:
  - [P00-PROGRAM-DISC-001](tasks/P00-PROGRAM-DISC-001.md) — AI وضعیت فنی و فایل baseline را read-only تهیه کند؛ انسان مالکیت repo/artifact و درستی نتیجه را تأیید کند
  - [P00-PROGRAM-DISC-002](tasks/P00-PROGRAM-DISC-002.md) — inventory همه applicationId/bundleId، signing key owner، store track، domain و artifact قبلی
  - [P00-PROGRAM-ADR-003](tasks/P00-PROGRAM-ADR-003.md) — scope اولین release روی `shop-only + WordPress + Theme + PWA` freeze شود
  - [P00-ARCH-ADR-004](tasks/P00-ARCH-ADR-004.md) — ADR دو `BackendProfile`، Feature Manifest hybrid، Theme/Core boundary و customer overlay تصویب شود
  - [P00-MANIFEST-DISC-005](tasks/P00-MANIFEST-DISC-005.md) — catalog همه featureهای فعلی و consumerهای UI/route/API/DI تهیه شود
  - [P00-CORE-DISC-006](tasks/P00-CORE-DISC-006.md) — snapshot قرارداد API فعلی KMP/WordPress/Spring و mismatchها تهیه شود
  - [P00-QA-DISC-007](tasks/P00-QA-DISC-007.md) — Test Strategy، severity، test case template و traceability تعریف شود
  - [P00-QA-DATA-008](tasks/P00-QA-DATA-008.md) — حساب‌ها و داده synthetic برای Guest/Customer/Admin/Shop Manager تعریف شوند
  - [P00-QA-OPS-009](tasks/P00-QA-OPS-009.md) — baseline build/test فعلی اجرا و command/result ثبت شود
  - [P00-QA-OPS-010](tasks/P00-QA-OPS-010.md) — محیط‌های مرجع `F0 minimal`, `F1 shop`, `F2 academy`, `F3 clinic/psych`, `F4 all` طراحی شوند
  - [P00-PROGRAM-CODE-011](tasks/P00-PROGRAM-CODE-011.md) — mismatch نسخه Theme (`style.css`/constant) بدون تغییر رفتار رفع شود
  - [P00-PROGRAM-OPS-012](tasks/P00-PROGRAM-OPS-012.md) — policy backup/restore و محل artifact/evidence تعریف شود
  - [P00-SECURITY-DISC-013](tasks/P00-SECURITY-DISC-013.md) — data classification، retention اولیه، threat surfaces و secret inventory ساخته شود
  - [P00-OBSERVABILITY-ADR-014](tasks/P00-OBSERVABILITY-ADR-014.md) — تفکیک audit log، operational log و product analytics + event dictionary نسخه صفر
  - [P00-BUSINESS-BIZ-015](tasks/P00-BUSINESS-BIZ-015.md) — SKU اولیه، pricing hypothesis، support scope، refund taxonomy و unit economics sheet
  - [P00-BUSINESS-BIZ-016](tasks/P00-BUSINESS-BIZ-016.md) — ۱۰ lead بالقوه و ۳ تا ۵ design partner کاندید شناسایی شوند
  - [P00-BUSINESS-BIZ-017](tasks/P00-BUSINESS-BIZ-017.md) — شرایط کتبی ژاکت/راست‌چین برای سهم، انحصار، تسویه، refund و support درخواست شود
  - [P00-QA-MANUAL-018](tasks/P00-QA-MANUAL-018.md) — exploratory baseline نسخه فعلی روی WordPress Theme/Plugin و targetهای قابل اجرای Android/Web/Desktop؛ iOS بدون محیط `BLOCKED` ثبت شود
  - [P00-PROGRAM-GATE-019](tasks/P00-PROGRAM-GATE-019.md) — Gate خروج فاز صفر

## P01 — Stop-Ship Security و صحت تراکنش

- هدف: مسیرهای قابل‌دسترسی WordPress/KMP/Payment ایمن شوند، بدون اینکه منتظر refactor بزرگ Foundation بمانند. Security پس از این فاز نیز در DoD هر Task ادامه دارد.
- Taskها:
  - [P01-SECURITY-DISC-001](tasks/P01-SECURITY-DISC-001.md) — تمام یافته‌های P0 سند ممیزی به ticketهای اتمیک خصوصی تبدیل شوند
  - [P01-SECURITY-OPS-002](tasks/P01-SECURITY-OPS-002.md) — اگر Spring فعلی public است، تا hardening allowlist/خاموش یا محدود شود
  - [P01-SECURITY-CODE-003](tasks/P01-SECURITY-CODE-003.md) — request/response/token logging کلاینت redacted و debug-only شود
  - [P01-SECURITY-CODE-004](tasks/P01-SECURITY-CODE-004.md) — cleartext و trust-all TLS حذف؛ debug exception صریح و محدود
  - [P01-SECURITY-CODE-005](tasks/P01-SECURITY-CODE-005.md) — token storage پلتفرم‌ها امن‌تر و Bearer فقط به host مجاز ارسال شود
  - [P01-SECURITY-CODE-006](tasks/P01-SECURITY-CODE-006.md) — `?api=` و override origin آزاد از production حذف یا allowlist شود
  - [P01-PAYMENT-CODE-007](tasks/P01-PAYMENT-CODE-007.md) — نتیجه deep link فقط trigger query باشد؛ status کلاینت trusted نباشد
  - [P01-PAYMENT-CODE-008](tasks/P01-PAYMENT-CODE-008.md) — پاک‌شدن cart فقط بعد از verify authoritative موفق انجام شود
  - [P01-PAYMENT-CODE-009](tasks/P01-PAYMENT-CODE-009.md) — قرارداد callback و نام پارامتر Android/PWA/WP یکسان و opaque شود
  - [P01-WPPLUGIN-SEC-010](tasks/P01-WPPLUGIN-SEC-010.md) — JWT secret/default، issuer/audience/expiry/rotation/revocation اصلاح شود
  - [P01-WPPLUGIN-SEC-011](tasks/P01-WPPLUGIN-SEC-011.md) — OTP hash، purpose، expiry، attempts، resend cooldown و debug-off
  - [P01-WPPLUGIN-SEC-012](tasks/P01-WPPLUGIN-SEC-012.md) — CORS default بسته و origin دقیق tenant allowlist شود
  - [P01-WPPLUGIN-SEC-013](tasks/P01-WPPLUGIN-SEC-013.md) — role/capability matrix granular؛ `shop_manager` ادمین سلامت نباشد
  - [P01-WPPLUGIN-SEC-014](tasks/P01-WPPLUGIN-SEC-014.md) — ownership/IDOR و post type validation برای read/write/delete
  - [P01-WPPLUGIN-CODE-015](tasks/P01-WPPLUGIN-CODE-015.md) — Wallet/session-credit خارج Scope shop-only از route/job/API production غیرفعال و fail-closed شود؛ فقط اگر legacy فعال/فروخته شده است ledger/transaction اتمیک شود
  - [P01-WPPLUGIN-CODE-016](tasks/P01-WPPLUGIN-CODE-016.md) — Booking خارج Scope نسخه اول deregister/fail-closed شود؛ hardening کامل فقط اگر surface فعلی قابل دسترس است
  - [P01-PAYMENT-SEC-017](tasks/P01-PAYMENT-SEC-017.md) — amount/currency/order/reference قبل از verify تطبیق و replay مسدود شود
  - [P01-WPPLUGIN-SEC-018](tasks/P01-WPPLUGIN-SEC-018.md) — LMS/Clinic/Psych پیش‌فرض خاموش و endpoint/media/job آن‌ها fail-closed شود؛ entitlement کامل به فازهای ۱۳/۱۴ موکول شود
  - [P01-SECURITY-CODE-019](tasks/P01-SECURITY-CODE-019.md) — hardcoded/demo secret و credential پیش‌فرض از artifactها حذف شوند
  - [P01-QA-AUTO-020](tasks/P01-QA-AUTO-020.md) — حداقل harness hermetic موردنیاز و regression خودکار یافته‌های ۰۰۳ تا ۰۱۹ اضافه شود
  - [P01-QA-MANUAL-021](tasks/P01-QA-MANUAL-021.md) — تست دستی auth/IDOR/payment و اثبات بسته‌بودن Wallet/Booking/LMS/Clinic خارج Scope
  - [P01-SECURITY-SEC-022](tasks/P01-SECURITY-SEC-022.md) — review مستقل diffهای امنیتی و threat model به‌روز شود
  - [P01-SECURITY-GATE-023](tasks/P01-SECURITY-GATE-023.md) — Gate خروج امنیت

## P02 — Engineering Foundation و Quality Harness

- هدف: حداقل Foundation لازم برای مسیر درآمد با تست رفتاری و بدون بازنویسی بزرگ ساخته شود. Version Catalog موجود audit می‌شود؛ Convention Plugin، AGP 9 host و تفکیک کامل Navigation می‌توانند موازی ادامه یابند و تا قبل از Android Gate تمام شوند، اما WordPress/PWA را بی‌دلیل مسدود نمی‌کنند.
- Taskها:
  - [P02-CORE-DISC-001](tasks/P02-CORE-DISC-001.md) — dependency graph ۲۹ ماژول، cycle و boundary violation مستند شود
  - [P02-CORE-CODE-002](tasks/P02-CORE-CODE-002.md) — versionهای hardcoded به Version Catalog موجود منتقل شوند
  - [P02-CORE-ADR-003](tasks/P02-CORE-ADR-003.md) — scope convention plugin و pluginهای مجاز تصویب شود
  - [P02-CORE-CODE-004](tasks/P02-CORE-CODE-004.md) — `build-logic` و اولین convention plugin برای KMP library ساخته شود
  - [P02-CORE-CODE-005](tasks/P02-CORE-CODE-005.md) — conventionهای Android/KMP/Compose/test مرحله‌ای اعمال شوند
  - [P02-QA-CODE-006](tasks/P02-QA-CODE-006.md) — harness امنیت فاز ۱ به `commonTest` و fixture foundation عمومی ارتقا یابد
  - [P02-QA-CODE-007](tasks/P02-QA-CODE-007.md) — harness WordPress فاز ۱ به integration environment با WP/Woo/PHP matrix ارتقا یابد
  - [P02-QA-CODE-008](tasks/P02-QA-CODE-008.md) — Spring test profile یا Testcontainers مستقل شود
  - [P02-CI-CODE-009](tasks/P02-CI-CODE-009.md) — PR gate برای lint/unit/integration/package و artifact report
  - [P02-CI-CODE-010](tasks/P02-CI-CODE-010.md) — ktlint/detekt و WPCS/Plugin Check/Theme Check تنظیم شوند
  - [P02-CI-CODE-011](tasks/P02-CI-CODE-011.md) — dependency locking/verification و secret scan اضافه شود
  - [P02-ARCH-ADR-012](tasks/P02-ARCH-ADR-012.md) — ADR جداسازی Android application shell برای AGP 9
  - [P02-ARCH-CODE-013](tasks/P02-ARCH-CODE-013.md) — thin `androidApp` ایجاد و application plugin از shared KMP جدا شود؛ deadline قبل از فاز ۱۱
  - [P02-ARCH-CODE-014](tasks/P02-ARCH-CODE-014.md) — Navigation تدریجی به graphهای feature تقسیم شود؛ full split پیش‌شرط Manifest نیست
  - [P02-CORE-CODE-015](tasks/P02-CORE-CODE-015.md) — dependency inversionهای navigation/profile/admin به‌صورت Taskهای کوچک
  - [P02-CI-OPS-016](tasks/P02-CI-OPS-016.md) — release artifact workflow از debug build جدا شود
  - [P02-QA-MANUAL-017](tasks/P02-QA-MANUAL-017.md) — smoke کامل رفتار قبل/بعد Foundation
  - [P02-CORE-GATE-018](tasks/P02-CORE-GATE-018.md) — Gate حداقل Foundation برای ورود به Manifest/WordPress

## P03 — دو Backend Profile و Feature Manifest انتها‌به‌انتها

- هدف: فقط `WORDPRESS` و `SPRING` در build باقی بمانند؛ برند/tenant/vertical از flavor جدا و قابلیت مؤثر در UI، route، use-case و backend enforce شود.
- Taskها:
  - [P03-MANIFEST-ADR-001](tasks/P03-MANIFEST-ADR-001.md) — schema نهایی `FeatureManifest v1`، dependency و fail-closed rules freeze شود
  - [P03-ARCH-CODE-002](tasks/P03-ARCH-CODE-002.md) — ماژول `core:config/capabilities` ساخته شود
  - [P03-ARCH-CODE-003](tasks/P03-ARCH-CODE-003.md) — `BackendProfile`, `BrandingConfig`, `BuildIdentity`, `FeatureManifest` جدا شوند
  - [P03-ARCH-CODE-004](tasks/P03-ARCH-CODE-004.md) — `BackendProfile` immutable با apiRoot/assetRoot/allowedAuthHosts
  - [P03-ARCH-CODE-005](tasks/P03-ARCH-CODE-005.md) — `EndpointResolver` و `AssetUrlResolver` تزریق و global/fallbackها حذف شوند
  - [P03-SECURITY-CODE-006](tasks/P03-SECURITY-CODE-006.md) — token/cache namespace بر اساس backend+tenant+origin
  - [P03-MANIFEST-CODE-007](tasks/P03-MANIFEST-CODE-007.md) — feature catalog و dependency resolver پیاده شود
  - [P03-MANIFEST-CODE-008](tasks/P03-MANIFEST-CODE-008.md) — compiled feature ceiling و platform policy اضافه شود
  - [P03-WPPLUGIN-CODE-009](tasks/P03-WPPLUGIN-CODE-009.md) — endpoint canonical `client-manifest` در WordPress
  - [P03-WPPLUGIN-CODE-010](tasks/P03-WPPLUGIN-CODE-010.md) — پنل toggle و dependency validation در wp-admin
  - [P03-MANIFEST-CODE-011](tasks/P03-MANIFEST-CODE-011.md) — bootstrap state و source precedence: فایل generated/local قابل ویرایش برای هر app، سپس manifest معتبر tenant؛ قبل از NavHost و با last-known-good محدود
  - [P03-MANIFEST-CODE-012](tasks/P03-MANIFEST-CODE-012.md) — shadow mode و telemetry اختلاف flag قدیم/جدید
  - [P03-MANIFEST-CODE-013](tasks/P03-MANIFEST-CODE-013.md) — central route/deep-link guard
  - [P03-MANIFEST-CODE-014](tasks/P03-MANIFEST-CODE-014.md) — use-case/repository/background guard
  - [P03-WPPLUGIN-SEC-015](tasks/P03-WPPLUGIN-SEC-015.md) — backend endpoint enforcement بر همان policy
  - [P03-WPTHEME-CODE-016](tasks/P03-WPTHEME-CODE-016.md) — Theme visibility از manifest/plugin config واحد
  - [P03-ANDROID-CODE-017](tasks/P03-ANDROID-CODE-017.md) — dimension backend فقط `wordpress/spring`؛ tenant config جدا
  - [P03-MIGRATION-DATA-018](tasks/P03-MIGRATION-DATA-018.md) — mapping flavor/package/token legacy و one-time migration
  - [P03-QA-AUTO-019](tasks/P03-QA-AUTO-019.md) — matrix tests برای F0/F1/F2/F3/F4 و دو profile fixture
  - [P03-QA-MANUAL-020](tasks/P03-QA-MANUAL-020.md) — toggle واقعی بدون rebuild در WordPress/PWA/client internal
  - [P03-MANIFEST-OPS-021](tasks/P03-MANIFEST-OPS-021.md) — aliasهای legacy با deprecation/telemetry نگه داشته شوند
  - [P03-MANIFEST-GATE-022](tasks/P03-MANIFEST-GATE-022.md) — Gate Manifest

## P04 — دو محصول مستقل WordPress: Theme کامل و Bridge/App Builder

- هدف: Theme بدون Bridge همه featureهای موجود را ارائه دهد؛ Bridge نیز بدون Carmilla Theme روی قالب ثالث، سایت را به clientها متصل و App Builder را به‌صورت control plane مدیریت کند. هر دو ZIP یک Shared Core versioned را بسته‌بندی می‌کنند و در نصب هم‌زمان فقط یک kernel boot می‌شود.
- مرحله ۱ — قرارداد و foundation:
  - [P04-WPPLUGIN-ADR-001](tasks/P04-WPPLUGIN-ADR-001.md) — قرارداد دو محصول مستقل، مالکیت داده و کانال انتشار
  - [P04-WPPLUGIN-ADR-002](tasks/P04-WPPLUGIN-ADR-002.md) — مرز Shared Core،Theme Host،Bridge Host و version authority
  - [P04-WPPLUGIN-CODE-003](tasks/P04-WPPLUGIN-CODE-003.md) — schema version و migration runner resumable
  - [P04-WPPLUGIN-CODE-004](tasks/P04-WPPLUGIN-CODE-004.md) — bootstrap و package اولیه Shared Core
  - [P04-WPTHEME-CODE-005](tasks/P04-WPTHEME-CODE-005.md) — اتصال Theme Host به kernel بسته‌بندی‌شده
  - [P04-WPPLUGIN-CODE-006](tasks/P04-WPPLUGIN-CODE-006.md) — capability/prerequisite/fail-closed مشترک
  - [P04-WPPLUGIN-CODE-007](tasks/P04-WPPLUGIN-CODE-007.md) — Bridge baseline روی قالب‌های ثالث
- مرحله ۲ — قرارداد داده،امنیت و UI پایه:
  - [P04-WPPLUGIN-CODE-008](tasks/P04-WPPLUGIN-CODE-008.md) — WooCommerce canonical CRUD
  - [P04-WPPLUGIN-CODE-009](tasks/P04-WPPLUGIN-CODE-009.md) — REST contract v1
  - [P04-WPPLUGIN-CODE-010](tasks/P04-WPPLUGIN-CODE-010.md) — role/capability matrix
  - [P04-WPPLUGIN-CODE-011](tasks/P04-WPPLUGIN-CODE-011.md) — onboarding/preflight
  - [P04-WPPLUGIN-CODE-012](tasks/P04-WPPLUGIN-CODE-012.md) — lifecycle و opt-in cleanup
  - [P04-WPPLUGIN-CODE-013](tasks/P04-WPPLUGIN-CODE-013.md) — Privacy/export/erase/retention
  - [P04-WPPLUGIN-CODE-014](tasks/P04-WPPLUGIN-CODE-014.md) — settings امن و audit
  - [P04-WPPLUGIN-CODE-015](tasks/P04-WPPLUGIN-CODE-015.md) — HPOS و Checkout Blocks
  - [P04-WPTHEME-CODE-016](tasks/P04-WPTHEME-CODE-016.md) — template hierarchy و responsive
  - [P04-WPTHEME-CODE-017](tasks/P04-WPTHEME-CODE-017.md) — accessibility
  - [P04-WPTHEME-CODE-025](tasks/P04-WPTHEME-CODE-025.md) — Elementor Canvas/Full Width و برگه‌ها
  - [P04-WPPLUGIN-CODE-018](tasks/P04-WPPLUGIN-CODE-018.md) — i18n/escaping/textdomain
- مرحله ۳ — انتقال featureها و استقلال دو محصول:
  - [P04-WORDPRESS-CODE-026](tasks/P04-WORDPRESS-CODE-026.md) — Content/Pages/Media/Store در Shared Core
  - [P04-WORDPRESS-CODE-027](tasks/P04-WORDPRESS-CODE-027.md) — Academy/LMS در Shared Core
  - [P04-WORDPRESS-CODE-028](tasks/P04-WORDPRESS-CODE-028.md) — Clinic/Therapist/Appointment در Shared Core
  - [P04-WORDPRESS-CODE-029](tasks/P04-WORDPRESS-CODE-029.md) — PsychTest/Support/Interactions در Shared Core
  - [P04-WPTHEME-CODE-030](tasks/P04-WPTHEME-CODE-030.md) — Theme standalone کامل
  - [P04-WPPLUGIN-CODE-031](tasks/P04-WPPLUGIN-CODE-031.md) — Bridge standalone و any-theme کامل
  - [P04-WORDPRESS-CODE-032](tasks/P04-WORDPRESS-CODE-032.md) — co-install arbitration و version compatibility
  - [P04-WPPLUGIN-CODE-033](tasks/P04-WPPLUGIN-CODE-033.md) — App Builder control plane
- مرحله ۴ — package،QA و Gate:
  - [P04-CI-CODE-019](tasks/P04-CI-CODE-019.md) — دو ZIP مستقل و reproducible و CI
  - [P04-QA-AUTO-020](tasks/P04-QA-AUTO-020.md) — ماتریس خودکار Theme-only/Bridge-only/both/upgrade
  - [P04-QA-MANUAL-021](tasks/P04-QA-MANUAL-021.md) — UAT کامل Theme standalone
  - [P04-WPPLUGIN-MANUAL-034](tasks/P04-WPPLUGIN-MANUAL-034.md) — UAT Bridge روی قالب ثالث و clientها
  - [P04-WORDPRESS-MANUAL-035](tasks/P04-WORDPRESS-MANUAL-035.md) — UAT co-install/upgrade/theme switch
  - [P04-QA-MANUAL-022](tasks/P04-QA-MANUAL-022.md) — UI/RTL/accessibility/error/offline regression
  - [P04-WPPLUGIN-DOC-023](tasks/P04-WPPLUGIN-DOC-023.md) — مستند دو SKU و compatibility
  - [P04-WPTHEME-GATE-036](tasks/P04-WPTHEME-GATE-036.md) — Theme Standalone Gate
  - [P04-WPPLUGIN-GATE-024](tasks/P04-WPPLUGIN-GATE-024.md) — Bridge/App Builder Standalone Gate
  - [P04-WORDPRESS-GATE-037](tasks/P04-WORDPRESS-GATE-037.md) — Coexistence و WordPress RC Gate

## P05 — Payment Platform، زرین‌پال، BNPL و بانک مستقیم

- هدف: پرداخت provider-agnostic، idempotent و قابل reconciliation شود. WooCommerce مرجع Order و کلاینت فقط hosted checkout و authoritative status را مصرف کند.
- Taskها:
  - [P05-PAYMENT-ADR-001](tasks/P05-PAYMENT-ADR-001.md) — contract `PaymentProvider` و capability matrix تصویب شود
  - [P05-PAYMENT-CODE-002](tasks/P05-PAYMENT-CODE-002.md) — domain Money، PaymentIntent/Event/Refund/Settlement/Entitlement
  - [P05-PAYMENT-DATA-003](tasks/P05-PAYMENT-DATA-003.md) — جدول‌ها، unique key و migration پرداخت
  - [P05-PAYMENT-CODE-004](tasks/P05-PAYMENT-CODE-004.md) — Woo gateway base با HPOS/Blocks و capability-driven UI
  - [P05-PAYMENT-CODE-005](tasks/P05-PAYMENT-CODE-005.md) — checkout session endpoint با cart recalculation
  - [P05-PAYMENT-CODE-006](tasks/P05-PAYMENT-CODE-006.md) — hosted redirect + opaque HTTPS result session
  - [P05-PAYMENT-CODE-007](tasks/P05-PAYMENT-CODE-007.md) — callback recorder و server-to-server verify
  - [P05-PAYMENT-CODE-008](tasks/P05-PAYMENT-CODE-008.md) — idempotency و lock برای callback/refund/deliver
  - [P05-PAYMENT-CODE-009](tasks/P05-PAYMENT-CODE-009.md) — outbox fulfillment و entitlement grant/revoke
  - [P05-PAYMENT-CODE-010](tasks/P05-PAYMENT-CODE-010.md) — `ZarinPalProvider` بر اساس مستند جاری
  - [P05-PAYMENT-CODE-011](tasks/P05-PAYMENT-CODE-011.md) — refund/reverse/manual-review workflow زرین‌پال
  - [P05-PAYMENT-OPS-012](tasks/P05-PAYMENT-OPS-012.md) — retry و reconciliation نزدیک‌زمان/روزانه
  - [P05-PAYMENT-OPS-013](tasks/P05-PAYMENT-OPS-013.md) — settlement ledger/dashboard/CSV fallback
  - [P05-PAYMENT-CODE-014](tasks/P05-PAYMENT-CODE-014.md) — `DigiPayProvider` با OAuth/ticket/verify/deliver/refund
  - [P05-PAYMENT-MANUAL-015](tasks/P05-PAYMENT-MANUAL-015.md) — قرارداد زمان deliver/settlement هر SKU دیجی‌پی تأیید شود
  - [P05-PAYMENT-DISC-016](tasks/P05-PAYMENT-DISC-016.md) — merchant docs رسمی اسنپ‌پی دریافت و archive شود
  - [P05-PAYMENT-CODE-017](tasks/P05-PAYMENT-CODE-017.md) — `SnappPayProvider` فقط پس از Task 016
  - [P05-PAYMENT-ADR-018](tasks/P05-PAYMENT-ADR-018.md) — PSP مستقیم اول و شرایط terminal مشتری انتخاب شود
  - [P05-PAYMENT-CODE-019](tasks/P05-PAYMENT-CODE-019.md) — adapter PSP منتخب
  - [P05-PAYMENT-CODE-020](tasks/P05-PAYMENT-CODE-020.md) — Product/Platform Payment Policy Router
  - [P05-QA-AUTO-021](tasks/P05-QA-AUTO-021.md) — provider contract fake + failure/replay/concurrency suite
  - [P05-QA-MANUAL-022](tasks/P05-QA-MANUAL-022.md) — sandbox end-to-end هر provider advertised
  - [P05-SECURITY-SEC-023](tasks/P05-SECURITY-SEC-023.md) — review مستقل payment threat/replay/secret/log
  - [P05-PAYMENT-GATE-024](tasks/P05-PAYMENT-GATE-024.md) — Gate Payment Core/ZarinPal
  - [P05-PAYMENT-GATE-025](tasks/P05-PAYMENT-GATE-025.md) — Gate مستقل DigiPay
  - [P05-PAYMENT-GATE-026](tasks/P05-PAYMENT-GATE-026.md) — Gate مستقل SnappPay
  - [P05-PAYMENT-GATE-027](tasks/P05-PAYMENT-GATE-027.md) — Gate مستقل PSP مستقیم منتخب

## P06 — SMS، Email، Generic HTTP و Secret Management

- هدف: هر سایت API key/URL/SMTP خودش را در پنل امن تنظیم کند؛ Theme و app هیچ secretی دریافت نکنند.
- Taskها:
  - [P06-MESSAGE-ADR-001](tasks/P06-MESSAGE-ADR-001.md) — NotificationService/Provider/DeliveryResult contract
  - [P06-MESSAGE-DATA-002](tasks/P06-MESSAGE-DATA-002.md) — config schema، delivery audit و retention
  - [P06-MESSAGE-CODE-003](tasks/P06-MESSAGE-CODE-003.md) — پنل `Carmilla → Integrations` با capability اختصاصی
  - [P06-MESSAGE-CODE-004](tasks/P06-MESSAGE-CODE-004.md) — `wp_mail` adapter پیش‌فرض
  - [P06-MESSAGE-CODE-005](tasks/P06-MESSAGE-CODE-005.md) — Generic SMS HTTP adapter با method/auth/header/body mapping
  - [P06-MESSAGE-CODE-006](tasks/P06-MESSAGE-CODE-006.md) — Generic Email REST/SMTP configuration
  - [P06-SECURITY-CODE-007](tasks/P06-SECURITY-CODE-007.md) — secret encryption/masking/rotation؛ key خارج DB
  - [P06-SECURITY-SEC-008](tasks/P06-SECURITY-SEC-008.md) — SSRF defense برای URL دلخواه
  - [P06-MESSAGE-CODE-009](tasks/P06-MESSAGE-CODE-009.md) — template engine با variable allowlist و RTL preview
  - [P06-MESSAGE-CODE-010](tasks/P06-MESSAGE-CODE-010.md) — OTP flow به ProviderResult واقعی متصل شود
  - [P06-MESSAGE-CODE-011](tasks/P06-MESSAGE-CODE-011.md) — Action Scheduler queue، retry، dedupe و fallback
  - [P06-MESSAGE-CODE-012](tasks/P06-MESSAGE-CODE-012.md) — redacted health/delivery log و test connection
  - [P06-QA-AUTO-013](tasks/P06-QA-AUTO-013.md) — fake provider tests برای success/timeout/4xx/5xx/retry
  - [P06-QA-MANUAL-014](tasks/P06-QA-MANUAL-014.md) — SMS/Email sandbox واقعی برای هر preset
  - [P06-MESSAGE-DOC-015](tasks/P06-MESSAGE-DOC-015.md) — راهنمای تنظیم، rotation، troubleshooting و disclosure
  - [P06-MESSAGE-GATE-016](tasks/P06-MESSAGE-GATE-016.md) — Gate Integrations

## P07 — Seed Pack، Import/Export و Customer Migration

- هدف: داده دمو بر اساس toggleها به‌شکل idempotent ساخته و داده مجاز سایت برای مشتری مشخص، نسخه‌دار و امن منتقل شود.
- Taskها:
  - [P07-SEED-ADR-001](tasks/P07-SEED-ADR-001.md) — Seed format v1، stable key، ownership و conflict policy
  - [P07-SEED-CODE-002](tasks/P07-SEED-CODE-002.md) — importer از Theme به Core Plugin منتقل شود
  - [P07-SEED-DATA-003](tasks/P07-SEED-DATA-003.md) — `seed_runs` و `seed_objects` registry/migration
  - [P07-SEED-CODE-004](tasks/P07-SEED-CODE-004.md) — dry-run با create/update/skip/conflict count
  - [P07-SEED-CODE-005](tasks/P07-SEED-CODE-005.md) — upsert با stable key/hash و حفظ تغییر مشتری
  - [P07-SEED-CODE-006](tasks/P07-SEED-CODE-006.md) — cursor/journal/lock و resume
  - [P07-SEED-CODE-007](tasks/P07-SEED-CODE-007.md) — rollback فقط object ساخته‌شده و تغییرنکرده
  - [P07-SEED-CODE-008](tasks/P07-SEED-CODE-008.md) — feature snapshot/dependency-aware import
  - [P07-SEED-CODE-009](tasks/P07-SEED-CODE-009.md) — media allowlist/MIME/size/hash/license/sideload
  - [P07-SEED-DATA-010](tasks/P07-SEED-DATA-010.md) — `base-fa-v1`: home/about/contact/privacy/terms، menu، حداقل ۶ post و محتوای عمومی
  - [P07-SEED-DATA-011](tasks/P07-SEED-DATA-011.md) — `shop-fa-v1`: حداقل ۴ category و ۱۲ product شامل simple/variable/physical/digital/out-of-stock + coupon
  - [P07-SEED-DATA-012](tasks/P07-SEED-DATA-012.md) — `academy-fa-v1`: حداقل ۲ رایگان/۲ پولی، section/lesson/quiz/certificate
  - [P07-SEED-DATA-013](tasks/P07-SEED-DATA-013.md) — `clinic-public-fa-v1`: متخصص/خدمت/slot کاملاً synthetic
  - [P07-SEED-DATA-014](tasks/P07-SEED-DATA-014.md) — `psych-synthetic-fa-v1`: نمونه غیرتشخیصی و بدون copyright نامعلوم
  - [P07-SEED-DATA-015](tasks/P07-SEED-DATA-015.md) — `all-fa-v1` composition، نه duplicate copy
  - [P07-MIGRATION-ADR-016](tasks/P07-MIGRATION-ADR-016.md) — Demo Import و Customer Migration به دو workflow جدا
  - [P07-MIGRATION-DATA-017](tasks/P07-MIGRATION-DATA-017.md) — export NDJSON + media manifest + checksums
  - [P07-MIGRATION-CODE-018](tasks/P07-MIGRATION-CODE-018.md) — mapping `(sourceSiteUuid, sourceObjectId)` و two-pass relations
  - [P07-MIGRATION-CODE-019](tasks/P07-MIGRATION-CODE-019.md) — delta import و URL/domain/media rewrite
  - [P07-MIGRATION-DATA-020](tasks/P07-MIGRATION-DATA-020.md) — Base Pack و Customer Overlay نسخه مستقل
  - [P07-MIGRATION-SEC-021](tasks/P07-MIGRATION-SEC-021.md) — AEAD encryption، signature/checksum، expiry/customer binding
  - [P07-MIGRATION-SEC-022](tasks/P07-MIGRATION-SEC-022.md) — denylist users/orders/payments/secrets/health data پیش‌فرض
  - [P07-QA-AUTO-023](tasks/P07-QA-AUTO-023.md) — دو suite مستقل `core-seed` و `customer-migration` برای import/retry/conflict/rollback
  - [P07-QA-MANUAL-024](tasks/P07-QA-MANUAL-024.md) — staging migration کامل + delta + rollback
  - [P07-SEED-DOC-025](tasks/P07-SEED-DOC-025.md) — راهنمای pack authoring، license و migration runbook
  - [P07-SEED-GATE-026](tasks/P07-SEED-GATE-026.md) — Gate A: Seed/Import Core برای base+shop و PWA
  - [P07-MIGRATION-GATE-027](tasks/P07-MIGRATION-GATE-027.md) — Gate B: Customer Migration Service

## P08 — PWA متصل به WordPress

- هدف: Compose Web به PWA نصب‌پذیر و production-ready تبدیل شود؛ WordPress Theme سطح public/SEO و PWA سطح app در `/app/` یا origin کنترل‌شده باشد.
- Taskها:
  - [P08-PWA-ADR-001](tasks/P08-PWA-ADR-001.md) — تصمیم same-origin `/app/` در برابر subdomain و SEO boundary
  - [P08-PWA-CODE-002](tasks/P08-PWA-CODE-002.md) — source set `webMain/jsMain` و production distribution اصلاح شود
  - [P08-PWA-CODE-003](tasks/P08-PWA-CODE-003.md) — `app-config.json` trusted و حذف `?api=` production
  - [P08-PWA-CODE-004](tasks/P08-PWA-CODE-004.md) — `manifest.webmanifest` با id/scope/start_url/name/icons
  - [P08-PWA-CODE-005](tasks/P08-PWA-CODE-005.md) — service worker با cache namespace tenant+revision
  - [P08-PWA-CODE-006](tasks/P08-PWA-CODE-006.md) — cache policy فقط app shell/public catalog/content
  - [P08-PWA-CODE-007](tasks/P08-PWA-CODE-007.md) — offline fallback و network error UX
  - [P08-PWA-CODE-008](tasks/P08-PWA-CODE-008.md) — logout/private cache purge و session expiry
  - [P08-PWA-CODE-009](tasks/P08-PWA-CODE-009.md) — update lifecycle، prompt، skip-waiting policy و rollback
  - [P08-PWA-SEC-010](tasks/P08-PWA-SEC-010.md) — CSP/HSTS/Referrer/Permissions Policy و origin binding
  - [P08-PWA-CODE-011](tasks/P08-PWA-CODE-011.md) — Web Push/VAPID tenant-owned با consent
  - [P08-PWA-CODE-012](tasks/P08-PWA-CODE-012.md) — deep link/history/back/refresh/share target در scope
  - [P08-OBSERVABILITY-CODE-013](tasks/P08-OBSERVABILITY-CODE-013.md) — event taxonomy مصوب و error/performance telemetry opt-in
  - [P08-PWA-PERF-014](tasks/P08-PWA-PERF-014.md) — performance budget برای startup/assets/API
  - [P08-QA-AUTO-015](tasks/P08-QA-AUTO-015.md) — Playwright install/update/offline/cache/deep-link tests
  - [P08-QA-MANUAL-016](tasks/P08-QA-MANUAL-016.md) — Chrome Android، Edge/Chrome/Firefox و Safari iOS behavior
  - [P08-QA-MANUAL-017](tasks/P08-QA-MANUAL-017.md) — keyboard/screen reader/RTL/zoom 200%
  - [P08-PWA-OPS-018](tasks/P08-PWA-OPS-018.md) — staging/production deploy، cache bust و rollback runbook
  - [P08-PWA-GATE-019](tasks/P08-PWA-GATE-019.md) — Gate PWA RC

## P09 — Regression کامل، Observability و Closed Beta مرحله‌ای

- هدف: WordPress/Theme/PWA RC در محیط‌های واقعی ولی کنترل‌شده سنجیده شود. Beta ابزار پیدا‌کردن blocker است، نه جایگزین QA داخلی.
- Taskها:
  - [P09-QA-DOC-001](tasks/P09-QA-DOC-001.md) — traceability نهایی feature→requirement→test→evidence
  - [P09-QA-AUTO-002](tasks/P09-QA-AUTO-002.md) — regression automation shop/auth/payment/toggle/import/PWA
  - [P09-QA-MANUAL-003](tasks/P09-QA-MANUAL-003.md) — clean install/upgrade/rollback روی WP/PHP/Woo matrix
  - [P09-QA-MANUAL-004](tasks/P09-QA-MANUAL-004.md) — Functional suite کامل shop-only
  - [P09-QA-MANUAL-005](tasks/P09-QA-MANUAL-005.md) — UI/Visual/RTL/LTR/accessibility/browser suite
  - [P09-QA-MANUAL-006](tasks/P09-QA-MANUAL-006.md) — resilience: offline/timeout/retry/duplicate/process death
  - [P09-SECURITY-SEC-007](tasks/P09-SECURITY-SEC-007.md) — security review/pentest محدود surface public
  - [P09-OBSERVABILITY-CODE-008](tasks/P09-OBSERVABILITY-CODE-008.md) — dashboard activation/import/PWA/checkout/error/support
  - [P09-OBSERVABILITY-SEC-009](tasks/P09-OBSERVABILITY-SEC-009.md) — consent/data minimization و audit analytics payload
  - [P09-BUSINESS-BIZ-010](tasks/P09-BUSINESS-BIZ-010.md) — design partner agreement، scope، feedback و data terms
  - [P09-BUSINESS-OPS-011](tasks/P09-BUSINESS-OPS-011.md) — onboarding تقویم‌دار، support channel و SLA pilot
  - [P09-BUSINESS-BIZ-012](tasks/P09-BUSINESS-BIZ-012.md) — feedback taxonomy و triage Product/Bug/Compatibility/Docs
  - [P09-QA-MANUAL-013](tasks/P09-QA-MANUAL-013.md) — UAT cohort ۳–۵ نفره با داده sanitised
  - [P09-OBSERVABILITY-BIZ-014](tasks/P09-OBSERVABILITY-BIZ-014.md) — دو چرخه review هفتگی KPI/support/UX
  - [P09-QA-MANUAL-015](tasks/P09-QA-MANUAL-015.md) — expanded beta فقط پس از Gate cohort اول
  - [P09-BUSINESS-BIZ-016](tasks/P09-BUSINESS-BIZ-016.md) — unit economics با support hours/refund واقعی بازبینی
  - [P09-QA-OPS-017](tasks/P09-QA-OPS-017.md) — release drill، rollback، restore و incident simulation
  - [P09-QA-GATE-018](tasks/P09-QA-GATE-018.md) — Gate Beta/Marketplace Candidate

## P10 — انتشار محدود WordPress/PWA، Marketplace و Partner Pilot

- هدف: اولین SKUهای قابل پشتیبانی بدون انتظار برای Android/Spring/iOS/Desktop به بازار برسند.
- Taskها:
  - [P10-BUSINESS-BIZ-001](tasks/P10-BUSINESS-BIZ-001.md) — marketplace اول با scoring قرارداد انتخاب شود
  - [P10-BUSINESS-BIZ-002](tasks/P10-BUSINESS-BIZ-002.md) — SKU و expectation جدا: Theme، Connector، PWA Pack
  - [P10-BUSINESS-BIZ-003](tasks/P10-BUSINESS-BIZ-003.md) — قیمت launch و guardrail تخفیف/حاشیه
  - [P10-WPTHEME-DOC-004](tasks/P10-WPTHEME-DOC-004.md) — readme/changelog/license/attribution/screenshot
  - [P10-WPPLUGIN-DOC-005](tasks/P10-WPPLUGIN-DOC-005.md) — install/onboarding/provider/import/upgrade/troubleshooting docs
  - [P10-BUSINESS-DOC-006](tasks/P10-BUSINESS-DOC-006.md) — known limitations و compatibility matrix عمومی
  - [P10-BUSINESS-DOC-007](tasks/P10-BUSINESS-DOC-007.md) — ویدئوی نصب، PWA و payment setup با داده demo
  - [P10-BUSINESS-OPS-008](tasks/P10-BUSINESS-OPS-008.md) — demo site و downloadable artifact بدون PII/secret
  - [P10-CI-OPS-009](tasks/P10-CI-OPS-009.md) — reproducible RC ZIP/PWA با checksum/SBOM/signature policy
  - [P10-BUSINESS-OPS-010](tasks/P10-BUSINESS-OPS-010.md) — support runbook، macro، escalation، SLA و refund triage
  - [P10-BUSINESS-BIZ-011](tasks/P10-BUSINESS-BIZ-011.md) — submission marketplace اول
  - [P10-PROGRAM-OPS-012](tasks/P10-PROGRAM-OPS-012.md) — release محدود `0.9.x-rc.n` برای componentهای RC-passed
  - [P10-BUSINESS-BIZ-013](tasks/P10-BUSINESS-BIZ-013.md) — limited launch ظرفیت‌محور
  - [P10-OBSERVABILITY-BIZ-014](tasks/P10-OBSERVABILITY-BIZ-014.md) — چهار هفته review فروش/activation/refund/ticket/margin
  - [P10-BUSINESS-EXPERIMENT-015](tasks/P10-BUSINESS-EXPERIMENT-015.md) — یک pricing/landing experiment کنترل‌شده
  - [P10-BUSINESS-BIZ-016](tasks/P10-BUSINESS-BIZ-016.md) — shortlist ۳–۵ partner و یک compatibility pilot
  - [P10-BUSINESS-BIZ-017](tasks/P10-BUSINESS-BIZ-017.md) — marketplace دوم فقط بعد از شرط‌های ورود
  - [P10-BUSINESS-GATE-018](tasks/P10-BUSINESS-GATE-018.md) — Gate Stable WordPress/PWA
  - [P10-PROGRAM-OPS-019](tasks/P10-PROGRAM-OPS-019.md) — فقط پس از Pass شدن Task 018، نسخه مستقل `1.0.0` componentهای تأییدشده منتشر شود

## P11 — Android WordPress و Managed Delivery

- هدف: Android برای WordPress با هویت و signing مشتری، پرداخت امن و تحویل تکرارپذیر عرضه شود. ابتدا service/operator-assisted، نه self-service عمومی.
- Taskها:
  - [P11-ANDROID-DISC-001](tasks/P11-ANDROID-DISC-001.md) — package/signing/store inventory فاز صفر نهایی شود
  - [P11-ANDROID-CODE-002](tasks/P11-ANDROID-CODE-002.md) — build فقط دو backend profile و tenant config generated
  - [P11-ANDROID-CODE-003](tasks/P11-ANDROID-CODE-003.md) — name/icon/splash/color/applicationId/version از BuildIdentity
  - [P11-ANDROID-SEC-004](tasks/P11-ANDROID-SEC-004.md) — keystore/upload key policy، vault و access audit
  - [P11-ANDROID-CODE-005](tasks/P11-ANDROID-CODE-005.md) — release build type، R8/shrink، baseline profile و mapping retention
  - [P11-ANDROID-CODE-006](tasks/P11-ANDROID-CODE-006.md) — verified App Links و opaque payment result
  - [P11-ANDROID-SEC-007](tasks/P11-ANDROID-SEC-007.md) — secure token storage، backup policy و tenant switch purge
  - [P11-ANDROID-CODE-008](tasks/P11-ANDROID-CODE-008.md) — hosted checkout integration با provider capability
  - [P11-ANDROID-CODE-009](tasks/P11-ANDROID-CODE-009.md) — Product/Play policy routing و mixed basket
  - [P11-OBSERVABILITY-CODE-010](tasks/P11-OBSERVABILITY-CODE-010.md) — crash/performance/product telemetry adapter؛ Firebase Analytics/Crashlytics/Performance فقط در صورت انتخاب
  - [P11-ANDROID-CODE-011](tasks/P11-ANDROID-CODE-011.md) — account deletion داخل app و web URL
  - [P11-ANDROID-DOC-012](tasks/P11-ANDROID-DOC-012.md) — Data Safety، privacy، support و store declarations
  - [P11-ANDROID-OPS-013](tasks/P11-ANDROID-OPS-013.md) — operator build runbook و artifact fingerprint
  - [P11-QA-AUTO-014](tasks/P11-QA-AUTO-014.md) — unit/UI/deep-link/payment/process-death regression
  - [P11-QA-MANUAL-015](tasks/P11-QA-MANUAL-015.md) — API 24، میانی، 36؛ small/normal/tablet/low-memory
  - [P11-QA-MANUAL-016](tasks/P11-QA-MANUAL-016.md) — RTL/font 200%/TalkBack/light/dark
  - [P11-ANDROID-OPS-017](tasks/P11-ANDROID-OPS-017.md) — internal test با دو برند و دو package
  - [P11-ANDROID-BIZ-018](tasks/P11-ANDROID-BIZ-018.md) — beta ۳–۵ مشتری با حساب/هویت خودشان
  - [P11-ANDROID-OPS-019](tasks/P11-ANDROID-OPS-019.md) — closed/staged rollout در store هدف
  - [P11-OBSERVABILITY-BIZ-020](tasks/P11-OBSERVABILITY-BIZ-020.md) — crash-free/build success/checkout/support review
  - [P11-ANDROID-GATE-021](tasks/P11-ANDROID-GATE-021.md) — Gate Android commercial

## P12 — App Builder MVP و Automation

- هدف: 
- Taskها:
  - [P12-BUILDER-ADR-001](tasks/P12-BUILDER-ADR-001.md) — ADR control plane، isolation و non-goals
  - [P12-BUILDER-DATA-002](tasks/P12-BUILDER-DATA-002.md) — Project/Tenant/BuildJob/Artifact/Template/Entitlement model
  - [P12-BUILDER-CODE-003](tasks/P12-BUILDER-CODE-003.md) — WordPress pairing، signed request و preflight
  - [P12-BUILDER-CODE-004](tasks/P12-BUILDER-CODE-004.md) — wizard branding/feature/package/domain/store metadata
  - [P12-BUILDER-CODE-005](tasks/P12-BUILDER-CODE-005.md) — queue/job state/retry/cancel/timeout
  - [P12-BUILDER-OPS-006](tasks/P12-BUILDER-OPS-006.md) — ephemeral isolated runner و pinned toolchain/cache
  - [P12-BUILDER-SEC-007](tasks/P12-BUILDER-SEC-007.md) — vault/HSM policy برای signing و credential
  - [P12-BUILDER-CODE-008](tasks/P12-BUILDER-CODE-008.md) — generated customer config/resources بدون source fork
  - [P12-BUILDER-OPS-009](tasks/P12-BUILDER-OPS-009.md) — artifact storage، checksum، SBOM، expiry و malware policy
  - [P12-BUILDER-CODE-010](tasks/P12-BUILDER-CODE-010.md) — redacted live logs و standardized error categories
  - [P12-BUILDER-CODE-011](tasks/P12-BUILDER-CODE-011.md) — secure customer delivery portal/one-time link
  - [P12-BUILDER-CODE-012](tasks/P12-BUILDER-CODE-012.md) — template/backend/plugin compatibility matrix
  - [P12-BUILDER-OPS-013](tasks/P12-BUILDER-OPS-013.md) — template update/canary/rollback و rebuild policy
  - [P12-BUILDER-BIZ-014](tasks/P12-BUILDER-BIZ-014.md) — quota، build credit، setup/maintenance و grace period
  - [P12-BUILDER-OPS-015](tasks/P12-BUILDER-OPS-015.md) — metrics/alert/cost/queue SLO و incident runbook
  - [P12-QA-AUTO-016](tasks/P12-QA-AUTO-016.md) — end-to-end fake signing/build/artifact tests
  - [P12-SECURITY-SEC-017](tasks/P12-SECURITY-SEC-017.md) — independent threat review/pentest
  - [P12-BUILDER-BIZ-018](tasks/P12-BUILDER-BIZ-018.md) — operator-assisted alpha قبل از self-service
  - [P12-BUILDER-GATE-019](tasks/P12-BUILDER-GATE-019.md) — Gate Builder private beta

## P13 — بسته آموزشی/LMS

- هدف: قابلیت آموزش به‌صورت یک Add-on مستقل و پیش‌فرض خاموش ساخته شود؛ فعال‌کردن آن فقط با `features.lms = true` ممکن باشد و هیچ مسیر، API، منو، Seed یا permission آموزشی در محصولی که این قابلیت را ندارد ظاهر نشود.
- Taskها:
  - [P13-LMS-DISC-001](tasks/P13-LMS-DISC-001.md) — Scope نسخه اول: course/lesson/quiz/certificate؛ ثبت non-goalها
  - [P13-LMS-ADR-002](tasks/P13-LMS-ADR-002.md) — ADR مالکیت داده و مدل canonical آموزش
  - [P13-LMS-DATA-003](tasks/P13-LMS-DATA-003.md) — schema/migration برای Course،Section،Lesson،Enrollment،Progress
  - [P13-LMS-DATA-004](tasks/P13-LMS-DATA-004.md) — schema/migration برای Quiz،Question،Attempt،Certificate
  - [P13-LMS-SEC-005](tasks/P13-LMS-SEC-005.md) — نقش‌ها و capabilities مدرس/دانشجو/مدیر
  - [P13-LMS-CODE-006](tasks/P13-LMS-CODE-006.md) — catalog و جزئیات دوره در Plugin/API/Theme/Client
  - [P13-LMS-CODE-007](tasks/P13-LMS-CODE-007.md) — enrollment و entitlement رایگان/پولی/دستی
  - [P13-LMS-SEC-008](tasks/P13-LMS-SEC-008.md) — محافظت محتوای خصوصی و URL امضاشده کوتاه‌عمر
  - [P13-LMS-CODE-009](tasks/P13-LMS-CODE-009.md) — پخش/نمایش lesson با resume و completion policy
  - [P13-LMS-DATA-010](tasks/P13-LMS-DATA-010.md) — conflict policy برای progress چنددستگاهی
  - [P13-LMS-CODE-011](tasks/P13-LMS-CODE-011.md) — quiz engine: time،attempt limit،shuffle،score
  - [P13-LMS-SEC-012](tasks/P13-LMS-SEC-012.md) — پاسخ صحیح و score server-authoritative
  - [P13-LMS-CODE-013](tasks/P13-LMS-CODE-013.md) — assignment/project و upload در صورت تأیید Scope
  - [P13-LMS-CODE-014](tasks/P13-LMS-CODE-014.md) — صدور certificate و صفحه verify عمومی حداقلی
  - [P13-LMS-CODE-015](tasks/P13-LMS-CODE-015.md) — پنل مدیریت دوره، lesson،quiz و enrollment
  - [P13-LMS-CODE-016](tasks/P13-LMS-CODE-016.md) — notification رویدادهای ثبت‌نام/موعد/تکمیل
  - [P13-LMS-DATA-017](tasks/P13-LMS-DATA-017.md) — Seed Pack آموزشی فاز ۷ با schema نهایی همگام شود
  - [P13-LMS-CODE-018](tasks/P13-LMS-CODE-018.md) — policy آفلاین برای metadata و محتوای محافظت‌شده
  - [P13-OBSERVABILITY-CODE-019](tasks/P13-OBSERVABILITY-CODE-019.md) — eventهای view/enroll/start/complete/quiz/certificate
  - [P13-LMS-LEGAL-020](tasks/P13-LMS-LEGAL-020.md) — copyright،شرایط مدرس،refund و certificate disclaimer
  - [P13-QA-AUTO-021](tasks/P13-QA-AUTO-021.md) — unit/integration/contract tests دامنه LMS
  - [P13-QA-MANUAL-022](tasks/P13-QA-MANUAL-022.md) — UAT دانشجو،مدرس و مدیر روی PWA/Android/Theme
  - [P13-QA-MANUAL-023](tasks/P13-QA-MANUAL-023.md) — RTL،keyboard،screen reader،فونت ۲۰۰٪ و ویدئو
  - [P13-LMS-BIZ-024](tasks/P13-LMS-BIZ-024.md) — SKU و قیمت Add-on آموزشی + هزینه storage/support
  - [P13-LMS-BIZ-025](tasks/P13-LMS-BIZ-025.md) — pilot با ۲–۳ آموزشگاه/مدرس واقعی
  - [P13-LMS-DOC-026](tasks/P13-LMS-DOC-026.md) — راهنمای مدیر/مدرس/دانشجو و troubleshooting
  - [P13-LMS-GATE-027](tasks/P13-LMS-GATE-027.md) — Gate عرضه Add-on آموزشی

## P14 — بسته کلینیک/مشاوره و تست‌های روان‌شناختی

- هدف: 
- Taskها:
  - [P14-CLINIC-DISC-001](tasks/P14-CLINIC-DISC-001.md) — دامنه،کشور/بازار،non-goal و ادعاهای ممنوع
  - [P14-CLINIC-ADR-002](tasks/P14-CLINIC-ADR-002.md) — ADR تفکیک داده عمومی،حساب،رزرو،سلامت و یادداشت محرمانه
  - [P14-CLINIC-PRIVACY-003](tasks/P14-CLINIC-PRIVACY-003.md) — DPIA/ارزیابی حریم خصوصی و consent matrix
  - [P14-CLINIC-SEC-004](tasks/P14-CLINIC-SEC-004.md) — نقش/رابطه مراجع،مشاور،پذیرش،ناظر و مدیر
  - [P14-CLINIC-DATA-005](tasks/P14-CLINIC-DATA-005.md) — مدل/مهاجرت practitioner،availability،appointment
  - [P14-CLINIC-CODE-006](tasks/P14-CLINIC-CODE-006.md) — پروفایل و فرایند تأیید مشاور
  - [P14-CLINIC-CODE-007](tasks/P14-CLINIC-CODE-007.md) — رزرو اتمیک slot با hold/expiry
  - [P14-CLINIC-CODE-008](tasks/P14-CLINIC-CODE-008.md) — reschedule/cancel/no-show/refund policy
  - [P14-CLINIC-CODE-009](tasks/P14-CLINIC-CODE-009.md) — پرداخت و entitlement جلسه
  - [P14-CLINIC-CODE-010](tasks/P14-CLINIC-CODE-010.md) — لینک جلسه/تماس با provider abstraction
  - [P14-CLINIC-SEC-011](tasks/P14-CLINIC-SEC-011.md) — پیام/فایل امن در صورت تأیید Scope
  - [P14-CLINIC-DATA-012](tasks/P14-CLINIC-DATA-012.md) — جداسازی note بالینی از note قابل مشاهده مراجع
  - [P14-PSYCH-DATA-013](tasks/P14-PSYCH-DATA-013.md) — registry پرسش‌نامه: owner/license/version/norm/locale
  - [P14-PSYCH-CODE-014](tasks/P14-PSYCH-CODE-014.md) — اجرای تست،resume،submit و scoring server-side
  - [P14-PSYCH-CODE-015](tasks/P14-PSYCH-CODE-015.md) — قالب نتیجه،دامنه تفسیر و disclaimer بالینی
  - [P14-PSYCH-CODE-016](tasks/P14-PSYCH-CODE-016.md) — visibility policy نتیجه برای مراجع/مشاور
  - [P14-CLINIC-CODE-017](tasks/P14-CLINIC-CODE-017.md) — journal/mood/homework فقط در صورت Scope
  - [P14-CLINIC-SAFETY-018](tasks/P14-CLINIC-SAFETY-018.md) — crisis/emergency flow و محدودیت خدمت
  - [P14-MESSAGE-SEC-019](tasks/P14-MESSAGE-SEC-019.md) — پیامک/email/push کمینه و بدون جزئیات حساس
  - [P14-CLINIC-SEC-020](tasks/P14-CLINIC-SEC-020.md) — encryption at rest/in transit،key rotation و audit
  - [P14-CLINIC-PRIVACY-021](tasks/P14-CLINIC-PRIVACY-021.md) — export/correction/delete/retention/legal-hold workflow
  - [P14-CLINIC-OPS-022](tasks/P14-CLINIC-OPS-022.md) — incident playbook برای افشا/دسترسی اشتباه
  - [P14-CLINIC-DATA-023](tasks/P14-CLINIC-DATA-023.md) — Seed کلینیک/تست کاملاً synthetic و برچسب‌دار
  - [P14-QA-AUTO-024](tasks/P14-QA-AUTO-024.md) — concurrency/state/property tests رزرو و امتیازدهی
  - [P14-QA-AUTO-025](tasks/P14-QA-AUTO-025.md) — authorization matrix و negative API tests
  - [P14-QA-MANUAL-026](tasks/P14-QA-MANUAL-026.md) — UAT مراجع/مشاور/پذیرش/مدیر
  - [P14-SECURITY-SEC-027](tasks/P14-SECURITY-SEC-027.md) — privacy/security assessment مستقل
  - [P14-CLINIC-REVIEW-028](tasks/P14-CLINIC-REVIEW-028.md) — review بالینی پرسش‌نامه و خروجی‌ها
  - [P14-CLINIC-BIZ-029](tasks/P14-CLINIC-BIZ-029.md) — عرضه ابتدا enterprise/restricted pilot
  - [P14-CLINIC-BIZ-030](tasks/P14-CLINIC-BIZ-030.md) — pilot با ۲–۳ مرکز واجد شرایط
  - [P14-CLINIC-DOC-031](tasks/P14-CLINIC-DOC-031.md) — راهنمای نقش‌ها،حریم خصوصی،بحران و recovery
  - [P14-CLINIC-GATE-032](tasks/P14-CLINIC-GATE-032.md) — Gate عرضه محدود Clinic/Psych

## P15 — Backend مستقل Spring Boot

- هدف: پروفایل `SPRING` به‌عنوان محصول Backend مستقل، امن و قابل عملیات عرضه شود. این فاز عمداً بعد از WordPress/PWA/Android قرار دارد؛ وجود کد فعلی Spring به‌تنهایی دلیل سرمایه‌گذاری یا انتشار عمومی نیست.
- Taskها:
  - [P15-SPRING-BIZ-001](tasks/P15-SPRING-BIZ-001.md) — Go/No-Go اقتصادی و SKU/SLA/hosting model
  - [P15-SPRING-DISC-002](tasks/P15-SPRING-DISC-002.md) — baseline کد،dependency،endpoint،schema و gap inventory
  - [P15-SPRING-ADR-003](tasks/P15-SPRING-ADR-003.md) — ADR modular monolith،tenant model و bounded contextها
  - [P15-SPRING-API-004](tasks/P15-SPRING-API-004.md) — قرارداد API و error/pagination/idempotency استاندارد
  - [P15-SPRING-CODE-005](tasks/P15-SPRING-CODE-005.md) — bootstrap/manifest endpoint همان schema فاز ۳
  - [P15-SPRING-DATA-006](tasks/P15-SPRING-DATA-006.md) — PostgreSQL production profile و Flyway-only migration
  - [P15-SPRING-DATA-007](tasks/P15-SPRING-DATA-007.md) — constraints/index/transaction boundary و timezone policy
  - [P15-SPRING-SEC-008](tasks/P15-SPRING-SEC-008.md) — JWT access/refresh rotation،revocation و session/device policy
  - [P15-SPRING-SEC-009](tasks/P15-SPRING-SEC-009.md) — RBAC + ownership/relationship checks
  - [P15-SPRING-SEC-010](tasks/P15-SPRING-SEC-010.md) — validation،rate limit،CORS،CSRF policy و replay defense
  - [P15-SPRING-CODE-011](tasks/P15-SPRING-CODE-011.md) — order/payment/wallet state machine اتمیک
  - [P15-SPRING-CODE-012](tasks/P15-SPRING-CODE-012.md) — PaymentProviderهای تأییدشده فاز ۵
  - [P15-SPRING-CODE-013](tasks/P15-SPRING-CODE-013.md) — NotificationProvider و credential per tenant
  - [P15-SPRING-SEC-014](tasks/P15-SPRING-SEC-014.md) — object storage خصوصی و signed URL
  - [P15-SPRING-CODE-015](tasks/P15-SPRING-CODE-015.md) — feature enforcement برای Shop/LMS/Clinic
  - [P15-SPRING-OPS-016](tasks/P15-SPRING-OPS-016.md) — externalized config،secret manager و rotation
  - [P15-SPRING-OPS-017](tasks/P15-SPRING-OPS-017.md) — health/readiness،structured log،trace و metric
  - [P15-SPRING-OPS-018](tasks/P15-SPRING-OPS-018.md) — alert/SLO/runbook و capacity dashboard
  - [P15-SPRING-OPS-019](tasks/P15-SPRING-OPS-019.md) — backup رمز‌شده،PITR و retention
  - [P15-SPRING-OPS-020](tasks/P15-SPRING-OPS-020.md) — container non-root،pinned base،SBOM و image scan
  - [P15-SPRING-OPS-021](tasks/P15-SPRING-OPS-021.md) — staging/prod IaC یا runbook deterministic
  - [P15-QA-AUTO-022](tasks/P15-QA-AUTO-022.md) — unit/integration/Testcontainers/contract suite
  - [P15-QA-AUTO-023](tasks/P15-QA-AUTO-023.md) — load/soak/race و failure-injection
  - [P15-SECURITY-SEC-024](tasks/P15-SECURITY-SEC-024.md) — pentest و dependency/container review مستقل
  - [P15-QA-MANUAL-025](tasks/P15-QA-MANUAL-025.md) — golden flow با Android/PWA روی staging
  - [P15-SPRING-OPS-026](tasks/P15-SPRING-OPS-026.md) — deploy/rollback/restore/rotation/incident drill
  - [P15-SPRING-BIZ-027](tasks/P15-SPRING-BIZ-027.md) — pilot پولی با ۱–۳ مشتری
  - [P15-SPRING-DOC-028](tasks/P15-SPRING-DOC-028.md) — install،upgrade،API،ops و customer handoff docs
  - [P15-SPRING-GATE-029](tasks/P15-SPRING-GATE-029.md) — Gate Backend Production

## P16 — iOS

- هدف: خروجی iOS فقط بعد از اثبات تقاضا، با حساب و هویت حقوقی درست، پرداخت سازگار با نوع محصول و فرایند TestFlight/App Store قابل تکرار عرضه شود.
- Taskها:
  - [P16-IOS-BIZ-001](tasks/P16-IOS-BIZ-001.md) — Go/No-Go تقاضا،هزینه Mac/Account/Support
  - [P16-IOS-DISC-002](tasks/P16-IOS-DISC-002.md) — audit target فعلی،interop،dependency و build blockers
  - [P16-IOS-ADR-003](tasks/P16-IOS-ADR-003.md) — ADR lifecycle/navigation/native integration
  - [P16-IOS-CODE-004](tasks/P16-IOS-CODE-004.md) — iOS host و دو Backend Profile با BuildIdentity
  - [P16-IOS-OPS-005](tasks/P16-IOS-OPS-005.md) — Bundle ID،team،provisioning و signing ownership
  - [P16-IOS-SEC-006](tasks/P16-IOS-SEC-006.md) — Keychain token storage،backup/accessibility policy
  - [P16-IOS-CODE-007](tasks/P16-IOS-CODE-007.md) — Universal Links و callback opaque
  - [P16-IOS-LEGAL-008](tasks/P16-IOS-LEGAL-008.md) — طبقه‌بندی محصول و مسیر پرداخت طبق قواعد جاری Store
  - [P16-IOS-CODE-009](tasks/P16-IOS-CODE-009.md) — payment router/StoreKit یا hosted flow مطابق تصمیم
  - [P16-IOS-CODE-010](tasks/P16-IOS-CODE-010.md) — permission purpose strings و privacy manifest
  - [P16-IOS-CODE-011](tasks/P16-IOS-CODE-011.md) — account deletion/export و web support URL
  - [P16-IOS-CODE-012](tasks/P16-IOS-CODE-012.md) — social login policy؛ Sign in with Apple در صورت الزام
  - [P16-IOS-CODE-013](tasks/P16-IOS-CODE-013.md) — lifecycle،background،network و memory handling
  - [P16-OBSERVABILITY-CODE-014](tasks/P16-OBSERVABILITY-CODE-014.md) — crash/performance/product telemetry adapter
  - [P16-QA-AUTO-015](tasks/P16-QA-AUTO-015.md) — shared tests + iOS integration/UI smoke در CI
  - [P16-QA-MANUAL-016](tasks/P16-QA-MANUAL-016.md) — iPhone کوچک/بزرگ،iPad در صورت Scope،دو نسخه iOS
  - [P16-QA-MANUAL-017](tasks/P16-QA-MANUAL-017.md) — RTL،Dynamic Type،VoiceOver،dark mode و keyboard
  - [P16-IOS-OPS-018](tasks/P16-IOS-OPS-018.md) — archive/export/upload/TestFlight pipeline
  - [P16-IOS-DOC-019](tasks/P16-IOS-DOC-019.md) — privacy labels،screenshots،metadata،review notes
  - [P16-IOS-BIZ-020](tasks/P16-IOS-BIZ-020.md) — TestFlight با ۳–۵ کاربر/مشتری نماینده
  - [P16-IOS-OPS-021](tasks/P16-IOS-OPS-021.md) — review و staged release با stop/rollback plan
  - [P16-IOS-GATE-022](tasks/P16-IOS-GATE-022.md) — Gate iOS Production

## P17 — Desktop

- هدف: Desktop فقط برای use case اثبات‌شده—برای مثال پنل اپراتور یا دسترسی مشتری سازمانی— بسته‌بندی، امضا و توزیع شود. «قابل اجرا بودن Compose Desktop» معادل «محصول قابل فروش» نیست.
- Taskها:
  - [P17-DESKTOP-BIZ-001](tasks/P17-DESKTOP-BIZ-001.md) — Go/No-Go و persona/use case پولی
  - [P17-DESKTOP-DISC-002](tasks/P17-DESKTOP-DISC-002.md) — target فعلی،dependency/native API و blocker inventory
  - [P17-DESKTOP-ADR-003](tasks/P17-DESKTOP-ADR-003.md) — ADR OS matrix،distribution و update channel
  - [P17-DESKTOP-CODE-004](tasks/P17-DESKTOP-CODE-004.md) — BuildIdentity و دو Backend Profile
  - [P17-DESKTOP-SEC-005](tasks/P17-DESKTOP-SEC-005.md) — OS keychain/credential vault و session policy
  - [P17-DESKTOP-CODE-006](tasks/P17-DESKTOP-CODE-006.md) — deep link/single-instance/payment callback
  - [P17-DESKTOP-CODE-007](tasks/P17-DESKTOP-CODE-007.md) — external browser payment و server verification
  - [P17-DESKTOP-CODE-008](tasks/P17-DESKTOP-CODE-008.md) — file picker/download/cache با sandbox/path policy
  - [P17-DESKTOP-OPS-009](tasks/P17-DESKTOP-OPS-009.md) — installer/package برای OSهای Scope
  - [P17-DESKTOP-OPS-010](tasks/P17-DESKTOP-OPS-010.md) — code signing و notarization در صورت نیاز
  - [P17-DESKTOP-OPS-011](tasks/P17-DESKTOP-OPS-011.md) — signed auto-update،channel و rollback
  - [P17-OBSERVABILITY-CODE-012](tasks/P17-OBSERVABILITY-CODE-012.md) — crash log/symbol و telemetry consent
  - [P17-QA-AUTO-013](tasks/P17-QA-AUTO-013.md) — shared/integration/smoke tests روی OS matrix
  - [P17-QA-MANUAL-014](tasks/P17-QA-MANUAL-014.md) — install/upgrade/deep-link/offline/payment/update
  - [P17-QA-MANUAL-015](tasks/P17-QA-MANUAL-015.md) — RTL،keyboard-only،screen reader،DPIهای مختلف
  - [P17-DESKTOP-OPS-016](tasks/P17-DESKTOP-OPS-016.md) — download portal/checksum/release notes/support matrix
  - [P17-DESKTOP-BIZ-017](tasks/P17-DESKTOP-BIZ-017.md) — pilot قراردادی ۱–۳ مشتری
  - [P17-DESKTOP-DOC-018](tasks/P17-DESKTOP-DOC-018.md) — install/update/rollback/EOL/troubleshooting
  - [P17-DESKTOP-GATE-019](tasks/P17-DESKTOP-GATE-019.md) — Gate Desktop Production

