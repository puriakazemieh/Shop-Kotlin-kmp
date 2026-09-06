# خلاصه ساده فازهای Carmilla

نسخه فعال: ۶ سپتامبر ۲۰۲۶. [تعریف کامل محصول](INDEPENDENT_PRODUCTS_SPEC_FA.md) و [صف اجرا](tasks.md) مرجع‌اند. DONEهای قبلی تاریخی‌اند؛ اولین کار READY، P03-QA-REVIEW-023 است. WooCommerce مجاز است؛ Theme و Plugin هرکدام مستقل و کلاینت‌ها دوپروفایلی‌اند.

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

## P03 — دو پروفایل داده، BuildSpec مستقل و Manifest مؤثر انتها‌به‌انتها

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
  - [P03-QA-REVIEW-023](tasks/P03-QA-REVIEW-023.md) — تطبیق شواهد تاریخی Manifest و ثبت مبنای QA جایگزین
  - [P03-MANIFEST-OPS-021](tasks/P03-MANIFEST-OPS-021.md) — aliasهای legacy با deprecation/telemetry نگه داشته شوند
  - [P03-ARCH-CODE-024](tasks/P03-ARCH-CODE-024.md) — مدل نسخه‌دار ProductBuildSpec مستقل از برند
  - [P03-ARCH-CODE-024A](tasks/P03-ARCH-CODE-024A.md) — تولید deterministic تنظیمات و سقف قابلیت کلاینت
  - [P03-ARCH-CODE-024B](tasks/P03-ARCH-CODE-024B.md) — تزریق BuildSpec صریح در bootstrap همه targetها
  - [P03-MANIFEST-CODE-025](tasks/P03-MANIFEST-CODE-025.md) — ذخیره وضعیت مؤثر واکنشی و خروج کنترل‌شده از shadow
  - [P03-MANIFEST-CODE-025A](tasks/P03-MANIFEST-CODE-025A.md) — اتصال منو و route/deep link به وضعیت مؤثر
  - [P03-MANIFEST-CODE-025B](tasks/P03-MANIFEST-CODE-025B.md) — اعمال guard عملیاتی فروشگاه
  - [P03-MANIFEST-CODE-025C](tasks/P03-MANIFEST-CODE-025C.md) — اعمال guard عملیاتی آموزش
  - [P03-MANIFEST-CODE-025D](tasks/P03-MANIFEST-CODE-025D.md) — اعمال guard عملیاتی کلینیک/نوبت
  - [P03-MANIFEST-CODE-025E](tasks/P03-MANIFEST-CODE-025E.md) — اعمال guard عملیاتی تست روان‌شناسی
  - [P03-QA-AUTO-026](tasks/P03-QA-AUTO-026.md) — آزمون wiring واقعی Manifest و دو پروفایل
  - [P03-QA-MANUAL-027](tasks/P03-QA-MANUAL-027.md) — QA انسانی جایگزین Manifest پس از اتصال runtime
  - [P03-MANIFEST-GATE-022](tasks/P03-MANIFEST-GATE-022.md) — Gate Manifest

## P04 — پوسته و افزونه مستقل، فروش قابلیت‌ها و سازگاری نصب هم‌زمان

- هدف: Theme بدون Bridge همه featureهای موجود را ارائه دهد؛ Bridge نیز بدون Carmilla Theme روی قالب ثالث، سایت را به clientها متصل و App Builder را به‌صورت control plane مدیریت کند. هر دو ZIP یک Shared Core versioned را بسته‌بندی می‌کنند و در نصب هم‌زمان فقط یک kernel boot می‌شود.
- مرحله ۱ — قرارداد و foundation:
  - [P04-WPPLUGIN-ADR-001](tasks/P04-WPPLUGIN-ADR-001.md) — قرارداد دو محصول مستقل، مالکیت داده و کانال انتشار
  - [P04-PRODUCT-ADR-038](tasks/P04-PRODUCT-ADR-038.md) — قرارداد تفصیلی محصولات و نگاشت مالکیت جدید
  - [P04-WPPLUGIN-ADR-002](tasks/P04-WPPLUGIN-ADR-002.md) — قرارداد هسته مشترک، دو میزبان و انتخاب نسخه سازگار
  - [P04-ENTITLEMENT-DATA-039](tasks/P04-ENTITLEMENT-DATA-039.md) — کاتالوگ قابلیت و SKU با تفکیک حق خرید و toggle
  - [P04-WPPLUGIN-CODE-003](tasks/P04-WPPLUGIN-CODE-003.md) — نسخه schema و runner مهاجرت مشترک و قابل ادامه
  - [P04-WPPLUGIN-CODE-004](tasks/P04-WPPLUGIN-CODE-004.md) — bootstrap اولیه و بسته‌بندی هسته در هر دو ZIP
  - [P04-WPTHEME-CODE-005](tasks/P04-WPTHEME-CODE-005.md) — اتصال میزبان پوسته به هسته داخلی بدون Bridge
  - [P04-WPPLUGIN-CODE-006](tasks/P04-WPPLUGIN-CODE-006.md) — resolver مشترک قابلیت، پیش‌نیاز و آمادگی انتشار
  - [P04-ENTITLEMENT-CODE-040](tasks/P04-ENTITLEMENT-CODE-040.md) — resolver مجوز معتبر و قابلیت مؤثر در هسته
  - [P04-WPPLUGIN-CODE-007](tasks/P04-WPPLUGIN-CODE-007.md) — اتصال میزبان افزونه به هسته روی قالب‌های دیگر
- مرحله ۲ — قرارداد داده،امنیت و UI پایه:
  - [P04-WPPLUGIN-CODE-008](tasks/P04-WPPLUGIN-CODE-008.md) — قرارداد و adapter رسمی WooCommerce در هسته مشترک
  - [P04-WPPLUGIN-CODE-009](tasks/P04-WPPLUGIN-CODE-009.md) — مرز REST مشترک WordPress برای هر دو میزبان
  - [P04-WPPLUGIN-CODE-010](tasks/P04-WPPLUGIN-CODE-010.md) — registry نقش‌ها و مجوز عملیات در هسته مشترک
  - [P04-WPPLUGIN-CODE-011](tasks/P04-WPPLUGIN-CODE-011.md) — راه‌اندازی و preflight مستقل هر محصول طبق SKU
  - [P04-WPPLUGIN-CODE-012](tasks/P04-WPPLUGIN-CODE-012.md) — چرخه فعال‌سازی دو میزبان و پاک‌سازی صریح داده سایت
  - [P04-WPPLUGIN-CODE-013](tasks/P04-WPPLUGIN-CODE-013.md) — زیرساخت مشترک export،erase و retention بدون وابستگی میزبان
  - [P04-WPPLUGIN-CODE-014](tasks/P04-WPPLUGIN-CODE-014.md) — API تنظیمات مشترک سایت با کنترل دسترسی و audit
  - [P04-ENTITLEMENT-CODE-041](tasks/P04-ENTITLEMENT-CODE-041.md) — پنل مشترک قابلیت‌های خریداری‌شده در هر دو میزبان
  - [P04-WPPLUGIN-CODE-015](tasks/P04-WPPLUGIN-CODE-015.md) — سازگاری Woo HPOS و Cart/Checkout Blocks در دو محصول
  - [P04-WPTHEME-CODE-016](tasks/P04-WPTHEME-CODE-016.md) — template hierarchy و responsive
  - [P04-WPTHEME-CODE-017](tasks/P04-WPTHEME-CODE-017.md) — accessibility
  - [P04-WPTHEME-CODE-025](tasks/P04-WPTHEME-CODE-025.md) — Elementor Canvas/Full Width و برگه‌ها
  - [P04-WPPLUGIN-CODE-018](tasks/P04-WPPLUGIN-CODE-018.md) — ترجمه و escaping مرزهای مشترک و میزبان‌های مستقل
  - [P04-WPPLUGIN-CODE-045](tasks/P04-WPPLUGIN-CODE-045.md) — زیرساخت نمایش عمومی افزونه روی قالب ثالث
- مرحله ۳ — انتقال featureها و استقلال دو محصول:
  - [P04-WORDPRESS-CODE-026](tasks/P04-WORDPRESS-CODE-026.md) — انتقال نوشته، برگه و رسانه به هسته مشترک
  - [P04-WORDPRESS-CODE-026A](tasks/P04-WORDPRESS-CODE-026A.md) — انتقال کاتالوگ و محصول به adapter مشترک Woo
  - [P04-WORDPRESS-CODE-026B](tasks/P04-WORDPRESS-CODE-026B.md) — انتقال سبد و سفارش به مسیر مشترک Woo
  - [P04-WORDPRESS-CODE-027](tasks/P04-WORDPRESS-CODE-027.md) — انتقال کاتالوگ دوره و محتوای درس به هسته مشترک
  - [P04-WORDPRESS-CODE-027A](tasks/P04-WORDPRESS-CODE-027A.md) — انتقال ثبت‌نام و پیشرفت آموزش با view مشترک
  - [P04-WORDPRESS-CODE-027B](tasks/P04-WORDPRESS-CODE-027B.md) — انتقال آزمون آموزشی و گواهی موجود
  - [P04-WORDPRESS-CODE-028](tasks/P04-WORDPRESS-CODE-028.md) — انتقال معرفی متخصص و زمان‌های قابل ارائه به هسته مشترک
  - [P04-WORDPRESS-CODE-028A](tasks/P04-WORDPRESS-CODE-028A.md) — انتقال مسیر ثبت و مشاهده نوبت موجود
  - [P04-WORDPRESS-CODE-029](tasks/P04-WORDPRESS-CODE-029.md) — انتقال تعریف،اجرای تست و نتیجه خصوصی PsychTest
  - [P04-WORDPRESS-CODE-029A](tasks/P04-WORDPRESS-CODE-029A.md) — انتقال پشتیبانی و تیکت با نمایش دو میزبان
  - [P04-WORDPRESS-CODE-029B](tasks/P04-WORDPRESS-CODE-029B.md) — انتقال تعاملات محتوا و محصول
  - [P04-WPTHEME-CODE-030](tasks/P04-WPTHEME-CODE-030.md) — یکپارچه‌سازی UI و مدیریت آماده‌شده پوسته مستقل
  - [P04-WPPLUGIN-CODE-031](tasks/P04-WPPLUGIN-CODE-031.md) — یکپارچه‌سازی frontend و مدیریت آماده افزونه روی قالب ثالث
  - [P04-WORDPRESS-DATA-042](tasks/P04-WORDPRESS-DATA-042.md) — پذیرش داده موجود و جابه‌جایی میزبان بدون حذف
  - [P04-WORDPRESS-CODE-032](tasks/P04-WORDPRESS-CODE-032.md) — co-install arbitration و version compatibility
  - [P04-WPPLUGIN-CODE-033](tasks/P04-WPPLUGIN-CODE-033.md) — قرارداد مشترک اتصال سایت و pairing اپ‌ساز برای دو میزبان
  - [P04-WORDPRESS-CODE-033A](tasks/P04-WORDPRESS-CODE-033A.md) — چرخه درخواست ساخت مشترک با runner آزمایشی
  - [P04-WPTHEME-CODE-046](tasks/P04-WPTHEME-CODE-046.md) — پنل اپ‌ساز مستقل در پوسته
  - [P04-WPPLUGIN-CODE-047](tasks/P04-WPPLUGIN-CODE-047.md) — پنل اپ‌ساز مستقل افزونه روی قالب دیگر
  - [P04-CI-CODE-043](tasks/P04-CI-CODE-043.md) — سازنده ZIP بر اساس SKU و فهرست قابلیت‌ها
  - [P04-ENTITLEMENT-CODE-044](tasks/P04-ENTITLEMENT-CODE-044.md) — ارتقای خرید و به‌روزرسانی سازگار هر SKU
- مرحله ۴ — package،QA و Gate:
  - [P04-CI-CODE-019](tasks/P04-CI-CODE-019.md) — CI بسته‌های مستقل با ورودی مانیفست و بررسی محتوای ZIP
  - [P04-QA-AUTO-048](tasks/P04-QA-AUTO-048.md) — آزمون منفی مجوز، بسته و تغییر میزبان از ZIP
  - [P04-QA-AUTO-020](tasks/P04-QA-AUTO-020.md) — رگرسیون مستقل و هم‌زمان دو میزبان با SKU واقعی
  - [P04-QA-MANUAL-021](tasks/P04-QA-MANUAL-021.md) — UAT پوسته تنها برای قابلیت‌ها و پنل بیلدر بسته‌شده
  - [P04-WPPLUGIN-MANUAL-034](tasks/P04-WPPLUGIN-MANUAL-034.md) — UAT افزونه مستقل با صفحات واقعی روی قالب ثالث
  - [P04-WORDPRESS-MANUAL-035](tasks/P04-WORDPRESS-MANUAL-035.md) — UAT دو محصول با مانیفست‌ها و نسخه‌های متفاوت
  - [P04-QA-MANUAL-022](tasks/P04-QA-MANUAL-022.md) — بازبینی نمایش قابلیت‌های بسته‌شده و وضعیت‌های خطا
  - [P04-WPPLUGIN-DOC-023](tasks/P04-WPPLUGIN-DOC-023.md) — مستند دو ZIP، مانیفست ساخت، تنظیمات و اتصال کلاینت
  - [P04-WPTHEME-GATE-036](tasks/P04-WPTHEME-GATE-036.md) — Gate زیرساخت پوسته مستقل و بسته‌های انتخابی
  - [P04-WPPLUGIN-GATE-024](tasks/P04-WPPLUGIN-GATE-024.md) — Gate زیرساخت افزونه مستقل و نمایش روی قالب ثالث
  - [P04-WORDPRESS-GATE-037](tasks/P04-WORDPRESS-GATE-037.md) — Gate سازگاری دو ZIP مستقل و نصب هم‌زمان

## P05 — Payment Platform، زرین‌پال، BNPL و بانک مستقیم

- هدف: پرداخت provider-agnostic، idempotent و قابل reconciliation شود. WooCommerce مرجع Order و کلاینت فقط hosted checkout و authoritative status را مصرف کند.
- Taskها:
  - [P05-PAYMENT-ADR-001](tasks/P05-PAYMENT-ADR-001.md) — قرارداد پرداخت مشترک دو میزبان و ماتریس قابلیت provider
  - [P05-PAYMENT-CODE-002](tasks/P05-PAYMENT-CODE-002.md) — Payment Core مشترک Theme و Plugin با مسیر نوشتن واحد
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
  - [P05-QA-AUTO-021](tasks/P05-QA-AUTO-021.md) — رگرسیون پرداخت در دو بسته مستقل و نصب هم‌زمان
  - [P05-QA-MANUAL-022](tasks/P05-QA-MANUAL-022.md) — QA دستی checkout و بازپرداخت هر میزبان مستقل
  - [P05-SECURITY-SEC-023](tasks/P05-SECURITY-SEC-023.md) — review مستقل payment threat/replay/secret/log
  - [P05-PAYMENT-GATE-024](tasks/P05-PAYMENT-GATE-024.md) — Gate هسته پرداخت و زرین‌پال برای هر دو محصول
  - [P05-PAYMENT-GATE-025](tasks/P05-PAYMENT-GATE-025.md) — Gate مستقل DigiPay
  - [P05-PAYMENT-GATE-026](tasks/P05-PAYMENT-GATE-026.md) — Gate مستقل SnappPay
  - [P05-PAYMENT-GATE-027](tasks/P05-PAYMENT-GATE-027.md) — Gate مستقل PSP مستقیم منتخب

## P06 — SMS، Email، Generic HTTP و Secret Management

- هدف: هر سایت API key/URL/SMTP خودش را در پنل امن تنظیم کند؛ Theme و app هیچ secretی دریافت نکنند.
- Taskها:
  - [P06-MESSAGE-ADR-001](tasks/P06-MESSAGE-ADR-001.md) — قرارداد پیام‌رسانی در Shared Core هر دو محصول
  - [P06-MESSAGE-DATA-002](tasks/P06-MESSAGE-DATA-002.md) — تنظیمات امن پیام‌رسانی با مالکیت سایت و مجوز قابلیت
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
  - [P06-QA-AUTO-013](tasks/P06-QA-AUTO-013.md) — تست provider، queue و خاموشی قابلیت در سه حالت میزبان
  - [P06-QA-MANUAL-014](tasks/P06-QA-MANUAL-014.md) — QA تنظیمات و ارسال آزمایشی از هر محصول مستقل
  - [P06-MESSAGE-DOC-015](tasks/P06-MESSAGE-DOC-015.md) — راهنمای تنظیم، rotation، troubleshooting و disclosure
  - [P06-MESSAGE-GATE-016](tasks/P06-MESSAGE-GATE-016.md) — Gate پیام‌رسانی مستقل Theme و Plugin

## P07 — Seed Pack، Import/Export و Customer Migration

- هدف: داده دمو بر اساس toggleها به‌شکل idempotent ساخته و داده مجاز سایت برای مشتری مشخص، نسخه‌دار و امن منتقل شود.
- Taskها:
  - [P07-SEED-ADR-001](tasks/P07-SEED-ADR-001.md) — قرارداد Seed Pack با Shared Core و SKU مستقل
  - [P07-SEED-CODE-002](tasks/P07-SEED-CODE-002.md) — انتقال importer به Shared Core داخل هر دو ZIP
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
  - [P07-QA-AUTO-023](tasks/P07-QA-AUTO-023.md) — تست Seed و Migration با ماتریس میزبان و SKU
  - [P07-QA-MANUAL-024](tasks/P07-QA-MANUAL-024.md) — staging migration کامل + delta + rollback
  - [P07-SEED-DOC-025](tasks/P07-SEED-DOC-025.md) — راهنمای pack authoring، license و migration runbook
  - [P07-SEED-GATE-026](tasks/P07-SEED-GATE-026.md) — Gate A: Seed/Import Core برای base+shop و PWA
  - [P07-MIGRATION-GATE-027](tasks/P07-MIGRATION-GATE-027.md) — Gate B: Customer Migration Service

## P08 — Web و PWA مستقل با دو پروفایل داده

- هدف: Compose Web به PWA نصب‌پذیر و production-ready تبدیل شود؛ WordPress Theme سطح public/SEO و PWA سطح app در `/app/` یا origin کنترل‌شده باشد.
- Taskها:
  - [P08-PWA-ADR-001](tasks/P08-PWA-ADR-001.md) — معماری استقرار مستقل Web/PWA و دو backend
  - [P08-PWA-CODE-002](tasks/P08-PWA-CODE-002.md) — خروجی مستقل Web و PWA از source set و BuildSpec
  - [P08-PWA-CODE-003](tasks/P08-PWA-CODE-003.md) — پیکربندی trusted وب با backend، برند و SKU مستقل
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
  - [P08-QA-AUTO-015](tasks/P08-QA-AUTO-015.md) — تست مستقل Web/PWA در دو profile و سه host WordPress
  - [P08-QA-MANUAL-016](tasks/P08-QA-MANUAL-016.md) — Chrome Android، Edge/Chrome/Firefox و Safari iOS behavior
  - [P08-QA-MANUAL-017](tasks/P08-QA-MANUAL-017.md) — keyboard/screen reader/RTL/zoom 200%
  - [P08-PWA-OPS-018](tasks/P08-PWA-OPS-018.md) — staging/production deploy، cache bust و rollback runbook
  - [P08-PWA-GATE-019](tasks/P08-PWA-GATE-019.md) — Gate مستقل Web/PWA با محدوده backend تأییدشده

## P09 — Regression کامل، Observability و Closed Beta مرحله‌ای

- هدف: WordPress/Theme/PWA RC در محیط‌های واقعی ولی کنترل‌شده سنجیده شود. Beta ابزار پیدا‌کردن blocker است، نه جایگزین QA داخلی.
- Taskها:
  - [P09-QA-DOC-001](tasks/P09-QA-DOC-001.md) — ماتریس رگرسیون محصول، SKU، میزبان و backend
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
  - [P09-QA-GATE-018](tasks/P09-QA-GATE-018.md) — Gate رگرسیون نسخه با استقلال محصولات و بدهی QA

## P10 — انتشار محدود WordPress/PWA، Marketplace و Partner Pilot

- هدف: اولین SKUهای قابل پشتیبانی بدون انتظار برای Android/Spring/iOS/Desktop به بازار برسند.
- Taskها:
  - [P10-BUSINESS-BIZ-001](tasks/P10-BUSINESS-BIZ-001.md) — marketplace اول با scoring قرارداد انتخاب شود
  - [P10-BUSINESS-BIZ-002](tasks/P10-BUSINESS-BIZ-002.md) — تعریف SKU مستقل Theme، Plugin و کلاینت با قابلیت قابل خرید
  - [P10-BUSINESS-BIZ-003](tasks/P10-BUSINESS-BIZ-003.md) — قیمت‌گذاری قابلیت، اپ‌ساز و هزینه تحویل هر SKU
  - [P10-WPTHEME-DOC-004](tasks/P10-WPTHEME-DOC-004.md) — readme/changelog/license/attribution/screenshot
  - [P10-WPPLUGIN-DOC-005](tasks/P10-WPPLUGIN-DOC-005.md) — install/onboarding/provider/import/upgrade/troubleshooting docs
  - [P10-BUSINESS-DOC-006](tasks/P10-BUSINESS-DOC-006.md) — ماتریس عمومی قابلیت، مجوز و سازگاری هر محصول
  - [P10-BUSINESS-DOC-007](tasks/P10-BUSINESS-DOC-007.md) — ویدئوی نصب، PWA و payment setup با داده demo
  - [P10-BUSINESS-OPS-008](tasks/P10-BUSINESS-OPS-008.md) — demo site و downloadable artifact بدون PII/secret
  - [P10-CI-OPS-009](tasks/P10-CI-OPS-009.md) — ساخت RC متناسب SKU با هویت و provenance مستقل
  - [P10-BUSINESS-OPS-010](tasks/P10-BUSINESS-OPS-010.md) — support runbook، macro، escalation، SLA و refund triage
  - [P10-BUSINESS-BIZ-011](tasks/P10-BUSINESS-BIZ-011.md) — submission marketplace اول
  - [P10-PROGRAM-OPS-012](tasks/P10-PROGRAM-OPS-012.md) — انتشار محدود فقط ترکیب‌های محصول و SKU تأییدشده
  - [P10-BUSINESS-BIZ-013](tasks/P10-BUSINESS-BIZ-013.md) — limited launch ظرفیت‌محور
  - [P10-OBSERVABILITY-BIZ-014](tasks/P10-OBSERVABILITY-BIZ-014.md) — چهار هفته review فروش/activation/refund/ticket/margin
  - [P10-BUSINESS-EXPERIMENT-015](tasks/P10-BUSINESS-EXPERIMENT-015.md) — یک pricing/landing experiment کنترل‌شده
  - [P10-BUSINESS-BIZ-016](tasks/P10-BUSINESS-BIZ-016.md) — shortlist ۳–۵ partner و یک compatibility pilot
  - [P10-BUSINESS-BIZ-017](tasks/P10-BUSINESS-BIZ-017.md) — marketplace دوم فقط بعد از شرط‌های ورود
  - [P10-BUSINESS-GATE-018](tasks/P10-BUSINESS-GATE-018.md) — Gate فروش مستقل خانواده‌های محصول با feature boundary
  - [P10-PROGRAM-OPS-019](tasks/P10-PROGRAM-OPS-019.md) — فقط پس از Pass شدن Task 018، نسخه مستقل `1.0.0` componentهای تأییدشده منتشر شود

## P11 — Android مستقل با دو پروفایل و تحویل مدیریت‌شده

- هدف: Android برای WordPress با هویت و signing مشتری، پرداخت امن و تحویل تکرارپذیر عرضه شود. ابتدا service/operator-assisted، نه self-service عمومی.
- Taskها:
  - [P11-ANDROID-DISC-001](tasks/P11-ANDROID-DISC-001.md) — package/signing/store inventory فاز صفر نهایی شود
  - [P11-ANDROID-CODE-002](tasks/P11-ANDROID-CODE-002.md) — Android مستقل با دو BackendProfile و config تولیدشده
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
  - [P11-ANDROID-OPS-013](tasks/P11-ANDROID-OPS-013.md) — runbook ساخت مستقل Android و هویت artifact مشتری
  - [P11-QA-AUTO-014](tasks/P11-QA-AUTO-014.md) — unit/UI/deep-link/payment/process-death regression
  - [P11-QA-MANUAL-015](tasks/P11-QA-MANUAL-015.md) — API 24، میانی، 36؛ small/normal/tablet/low-memory
  - [P11-QA-MANUAL-016](tasks/P11-QA-MANUAL-016.md) — RTL/font 200%/TalkBack/light/dark
  - [P11-ANDROID-OPS-017](tasks/P11-ANDROID-OPS-017.md) — آزمون داخلی Android با دو برند و hostهای مستقل
  - [P11-ANDROID-BIZ-018](tasks/P11-ANDROID-BIZ-018.md) — beta ۳–۵ مشتری با حساب/هویت خودشان
  - [P11-ANDROID-OPS-019](tasks/P11-ANDROID-OPS-019.md) — closed/staged rollout در store هدف
  - [P11-OBSERVABILITY-BIZ-020](tasks/P11-OBSERVABILITY-BIZ-020.md) — crash-free/build success/checkout/support review
  - [P11-ANDROID-GATE-021](tasks/P11-ANDROID-GATE-021.md) — Gate تجاری Android مستقل برای backend و SKU مشخص

## P12 — اپ‌ساز قابل خرید از هر دو میزبان؛ خروجی واقعی Android و Web/PWA

- هدف: 
- Taskها:
  - [P12-BUILDER-ADR-001](tasks/P12-BUILDER-ADR-001.md) — قرارداد Builder دو میزبان و ورودی مستقل پروژه
  - [P12-BUILDER-DATA-002](tasks/P12-BUILDER-DATA-002.md) — مدل پروژه، BuildJob و entitlement با منشأ محصول
  - [P12-BUILDER-CODE-003](tasks/P12-BUILDER-CODE-003.md) — pairing مستقل هر WordPress host و احراز پروژه
  - [P12-BUILDER-CODE-004](tasks/P12-BUILDER-CODE-004.md) — wizard ساخت با target و قابلیت‌های واقعاً خریداری‌شده
  - [P12-BUILDER-CODE-005](tasks/P12-BUILDER-CODE-005.md) — queue/job state/retry/cancel/timeout
  - [P12-BUILDER-OPS-006](tasks/P12-BUILDER-OPS-006.md) — runner ایزوله با قرارداد target و toolchain ثابت
  - [P12-BUILDER-SEC-007](tasks/P12-BUILDER-SEC-007.md) — vault/HSM policy برای signing و credential
  - [P12-BUILDER-CODE-008](tasks/P12-BUILDER-CODE-008.md) — تولید config و منابع مشتری از BuildSpec بدون fork
  - [P12-BUILDER-CODE-020](tasks/P12-BUILDER-CODE-020.md) — runner واقعی Web و PWA از BuildSpec
  - [P12-BUILDER-CODE-021](tasks/P12-BUILDER-CODE-021.md) — runner واقعی Android از template انتشار
  - [P12-BUILDER-OPS-009](tasks/P12-BUILDER-OPS-009.md) — artifact storage، checksum، SBOM، expiry و malware policy
  - [P12-BUILDER-CODE-010](tasks/P12-BUILDER-CODE-010.md) — redacted live logs و standardized error categories
  - [P12-BUILDER-CODE-011](tasks/P12-BUILDER-CODE-011.md) — secure customer delivery portal/one-time link
  - [P12-BUILDER-CODE-012](tasks/P12-BUILDER-CODE-012.md) — ماتریس سازگاری template، kernel، backend و target
  - [P12-BUILDER-OPS-013](tasks/P12-BUILDER-OPS-013.md) — template update/canary/rollback و rebuild policy
  - [P12-BUILDER-BIZ-014](tasks/P12-BUILDER-BIZ-014.md) — حقوق استفاده، اعتبار build و تمدید هر محصول
  - [P12-BUILDER-OPS-015](tasks/P12-BUILDER-OPS-015.md) — metrics/alert/cost/queue SLO و incident runbook
  - [P12-QA-AUTO-016](tasks/P12-QA-AUTO-016.md) — آزمون قرارداد و build واقعی Android/Web/PWA
  - [P12-QA-MANUAL-022](tasks/P12-QA-MANUAL-022.md) — تحویل واقعی اپ‌ساز از هر دو محصول مستقل
  - [P12-SECURITY-SEC-017](tasks/P12-SECURITY-SEC-017.md) — independent threat review/pentest
  - [P12-BUILDER-BIZ-018](tasks/P12-BUILDER-BIZ-018.md) — operator-assisted alpha قبل از self-service
  - [P12-BUILDER-GATE-019](tasks/P12-BUILDER-GATE-019.md) — Gate Builder اولیه با خروجی واقعی Android/Web/PWA

## P13 — بسته آموزشی/LMS

- هدف: قابلیت آموزش به‌صورت یک Add-on مستقل و پیش‌فرض خاموش ساخته شود؛ فعال‌کردن آن فقط با `features.lms = true` ممکن باشد و هیچ مسیر، API، منو، Seed یا permission آموزشی در محصولی که این قابلیت را ندارد ظاهر نشود.
- Taskها:
  - [P13-LMS-DISC-001](tasks/P13-LMS-DISC-001.md) — Scope نسخه اول: course/lesson/quiz/certificate؛ ثبت non-goalها
  - [P13-LMS-ADR-002](tasks/P13-LMS-ADR-002.md) — مالکیت canonical آموزش و مجوز بسته مستقل
  - [P13-LMS-DATA-003](tasks/P13-LMS-DATA-003.md) — schema/migration برای Course،Section،Lesson،Enrollment،Progress
  - [P13-LMS-DATA-004](tasks/P13-LMS-DATA-004.md) — schema/migration برای Quiz،Question،Attempt،Certificate
  - [P13-LMS-SEC-005](tasks/P13-LMS-SEC-005.md) — نقش‌ها و capabilities مدرس/دانشجو/مدیر
  - [P13-LMS-CODE-006](tasks/P13-LMS-CODE-006.md) — فهرست و جزئیات دوره در دو محصول و کلاینت مستقل
  - [P13-LMS-CODE-007](tasks/P13-LMS-CODE-007.md) — enrollment و entitlement رایگان/پولی/دستی
  - [P13-LMS-SEC-008](tasks/P13-LMS-SEC-008.md) — محافظت محتوای خصوصی و URL امضاشده کوتاه‌عمر
  - [P13-LMS-CODE-009](tasks/P13-LMS-CODE-009.md) — پخش/نمایش lesson با resume و completion policy
  - [P13-LMS-CODE-028](tasks/P13-LMS-CODE-028.md) — پخش واقعی درس در Web/PWA
  - [P13-LMS-DATA-010](tasks/P13-LMS-DATA-010.md) — conflict policy برای progress چنددستگاهی
  - [P13-LMS-CODE-011](tasks/P13-LMS-CODE-011.md) — quiz engine: time،attempt limit،shuffle،score
  - [P13-LMS-SEC-012](tasks/P13-LMS-SEC-012.md) — پاسخ صحیح و score server-authoritative
  - [P13-LMS-CODE-013](tasks/P13-LMS-CODE-013.md) — assignment/project و upload در صورت تأیید Scope
  - [P13-LMS-CODE-014](tasks/P13-LMS-CODE-014.md) — صدور certificate و صفحه verify عمومی حداقلی
  - [P13-LMS-CODE-015](tasks/P13-LMS-CODE-015.md) — مدیریت آموزش در هر دو میزبان و UI مستقل افزونه
  - [P13-LMS-CODE-016](tasks/P13-LMS-CODE-016.md) — notification رویدادهای ثبت‌نام/موعد/تکمیل
  - [P13-LMS-DATA-017](tasks/P13-LMS-DATA-017.md) — Seed Pack آموزشی فاز ۷ با schema نهایی همگام شود
  - [P13-LMS-CODE-018](tasks/P13-LMS-CODE-018.md) — policy آفلاین برای metadata و محتوای محافظت‌شده
  - [P13-OBSERVABILITY-CODE-019](tasks/P13-OBSERVABILITY-CODE-019.md) — eventهای view/enroll/start/complete/quiz/certificate
  - [P13-LMS-LEGAL-020](tasks/P13-LMS-LEGAL-020.md) — copyright،شرایط مدرس،refund و certificate disclaimer
  - [P13-QA-AUTO-021](tasks/P13-QA-AUTO-021.md) — suite آموزش با SKU و parity میزبان‌ها
  - [P13-QA-MANUAL-022](tasks/P13-QA-MANUAL-022.md) — UAT دانشجو و مدرس در دو ZIP و کلاینت‌های آماده
  - [P13-QA-MANUAL-023](tasks/P13-QA-MANUAL-023.md) — RTL،keyboard،screen reader،فونت ۲۰۰٪ و ویدئو
  - [P13-LMS-BIZ-024](tasks/P13-LMS-BIZ-024.md) — قیمت و حقوق بسته آموزش برای هر خانواده محصول
  - [P13-LMS-BIZ-025](tasks/P13-LMS-BIZ-025.md) — pilot با ۲–۳ آموزشگاه/مدرس واقعی
  - [P13-LMS-DOC-026](tasks/P13-LMS-DOC-026.md) — راهنمای مدیر/مدرس/دانشجو و troubleshooting
  - [P13-LMS-GATE-027](tasks/P13-LMS-GATE-027.md) — Gate عرضه Add-on آموزشی

## P14 — بسته کلینیک/مشاوره و تست‌های روان‌شناختی

- هدف: 
- Taskها:
  - [P14-CLINIC-DISC-001](tasks/P14-CLINIC-DISC-001.md) — دامنه،کشور/بازار،non-goal و ادعاهای ممنوع
  - [P14-CLINIC-ADR-002](tasks/P14-CLINIC-ADR-002.md) — قرارداد قابلیت‌های جداگانه نوبت، مشاوره و داده خصوصی
  - [P14-CLINIC-PRIVACY-003](tasks/P14-CLINIC-PRIVACY-003.md) — DPIA/ارزیابی حریم خصوصی و consent matrix
  - [P14-CLINIC-SEC-004](tasks/P14-CLINIC-SEC-004.md) — نقش/رابطه مراجع،مشاور،پذیرش،ناظر و مدیر
  - [P14-CLINIC-DATA-005](tasks/P14-CLINIC-DATA-005.md) — مدل نوبت‌دهی مشترک با حفظ هویت و تفکیک SKU
  - [P14-CLINIC-CODE-033](tasks/P14-CLINIC-CODE-033.md) — تفکیک قابلیت نوبت‌دهی از خدمات و اطلاعات مشاوره
  - [P14-CLINIC-CODE-006](tasks/P14-CLINIC-CODE-006.md) — نمایش و مدیریت مشاور در Theme و Plugin مستقل
  - [P14-CLINIC-CODE-007](tasks/P14-CLINIC-CODE-007.md) — رزرو اتمیک با hold و expiry در هر دو محصول مستقل
  - [P14-CLINIC-CODE-008](tasks/P14-CLINIC-CODE-008.md) — reschedule/cancel/no-show/refund policy
  - [P14-CLINIC-CODE-009](tasks/P14-CLINIC-CODE-009.md) — پرداخت و entitlement جلسه
  - [P14-CLINIC-CODE-010](tasks/P14-CLINIC-CODE-010.md) — جلسه مشاوره به‌عنوان قابلیت جدا با provider abstraction
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
  - [P14-QA-AUTO-025](tasks/P14-QA-AUTO-025.md) — ماتریس مجوز نقش، رابطه، SKU و میزبان خدمات
  - [P14-QA-MANUAL-026](tasks/P14-QA-MANUAL-026.md) — UAT نوبت و مشاوره در دو بسته مستقل
  - [P14-SECURITY-SEC-027](tasks/P14-SECURITY-SEC-027.md) — privacy/security assessment مستقل
  - [P14-CLINIC-REVIEW-028](tasks/P14-CLINIC-REVIEW-028.md) — review بالینی پرسش‌نامه و خروجی‌ها
  - [P14-CLINIC-BIZ-029](tasks/P14-CLINIC-BIZ-029.md) — عرضه ابتدا enterprise/restricted pilot
  - [P14-CLINIC-BIZ-030](tasks/P14-CLINIC-BIZ-030.md) — pilot با ۲–۳ مرکز واجد شرایط
  - [P14-CLINIC-DOC-031](tasks/P14-CLINIC-DOC-031.md) — راهنمای نقش‌ها،حریم خصوصی،بحران و recovery
  - [P14-CLINIC-GATE-032](tasks/P14-CLINIC-GATE-032.md) — Gate عرضه محدود Clinic/Psych

## P15 — سرور Spring مستقل و کلاینت‌های بدون نیاز به WordPress

- هدف: پروفایل `SPRING` به‌عنوان محصول Backend مستقل، امن و قابل عملیات عرضه شود. این فاز عمداً بعد از WordPress/PWA/Android قرار دارد؛ وجود کد فعلی Spring به‌تنهایی دلیل سرمایه‌گذاری یا انتشار عمومی نیست.
- Taskها:
  - [P15-SPRING-BIZ-001](tasks/P15-SPRING-BIZ-001.md) — مدل استقرار و اقتصاد backend مستقل Spring
  - [P15-SPRING-DISC-002](tasks/P15-SPRING-DISC-002.md) — baseline کد،dependency،endpoint،schema و gap inventory
  - [P15-SPRING-ADR-003](tasks/P15-SPRING-ADR-003.md) — ADR modular monolith،tenant model و bounded contextها
  - [P15-SPRING-API-004](tasks/P15-SPRING-API-004.md) — قرارداد مشترک API Spring و WordPress با parity واقعی
  - [P15-SPRING-CODE-005](tasks/P15-SPRING-CODE-005.md) — Manifest سرور مستقل با schema و entitlement مشترک
  - [P15-SPRING-DATA-006](tasks/P15-SPRING-DATA-006.md) — PostgreSQL production profile و Flyway-only migration
  - [P15-SPRING-DATA-007](tasks/P15-SPRING-DATA-007.md) — constraints/index/transaction boundary و timezone policy
  - [P15-SPRING-SEC-008](tasks/P15-SPRING-SEC-008.md) — JWT access/refresh rotation،revocation و session/device policy
  - [P15-SPRING-SEC-009](tasks/P15-SPRING-SEC-009.md) — RBAC + ownership/relationship checks
  - [P15-SPRING-SEC-010](tasks/P15-SPRING-SEC-010.md) — validation،rate limit،CORS،CSRF policy و replay defense
  - [P15-SPRING-CODE-011](tasks/P15-SPRING-CODE-011.md) — order/payment/wallet state machine اتمیک
  - [P15-SPRING-CODE-012](tasks/P15-SPRING-CODE-012.md) — PaymentProviderهای تأییدشده فاز ۵
  - [P15-SPRING-CODE-013](tasks/P15-SPRING-CODE-013.md) — NotificationProvider و credential per tenant
  - [P15-SPRING-SEC-014](tasks/P15-SPRING-SEC-014.md) — object storage خصوصی و signed URL
  - [P15-SPRING-CODE-015](tasks/P15-SPRING-CODE-015.md) — enforcement قابلیت و مجوز در service، job و API سرور
  - [P15-SPRING-OPS-016](tasks/P15-SPRING-OPS-016.md) — externalized config،secret manager و rotation
  - [P15-SPRING-OPS-017](tasks/P15-SPRING-OPS-017.md) — health/readiness،structured log،trace و metric
  - [P15-SPRING-OPS-018](tasks/P15-SPRING-OPS-018.md) — alert/SLO/runbook و capacity dashboard
  - [P15-SPRING-OPS-019](tasks/P15-SPRING-OPS-019.md) — backup رمز‌شده،PITR و retention
  - [P15-SPRING-OPS-020](tasks/P15-SPRING-OPS-020.md) — container non-root،pinned base،SBOM و image scan
  - [P15-SPRING-OPS-021](tasks/P15-SPRING-OPS-021.md) — staging/prod IaC یا runbook deterministic
  - [P15-QA-AUTO-022](tasks/P15-QA-AUTO-022.md) — suite سرور مستقل با Testcontainers و قرارداد provider
  - [P15-QA-AUTO-023](tasks/P15-QA-AUTO-023.md) — load، soak، race و failure-injection سرور مستقل
  - [P15-SECURITY-SEC-024](tasks/P15-SECURITY-SEC-024.md) — pentest و dependency/container review مستقل
  - [P15-QA-MANUAL-025](tasks/P15-QA-MANUAL-025.md) — پذیرش backend واقعی Spring با کلاینت مستقل
  - [P15-SPRING-OPS-026](tasks/P15-SPRING-OPS-026.md) — deploy/rollback/restore/rotation/incident drill
  - [P15-SPRING-BIZ-027](tasks/P15-SPRING-BIZ-027.md) — pilot پولی با ۱–۳ مشتری
  - [P15-SPRING-DOC-028](tasks/P15-SPRING-DOC-028.md) — install،upgrade،API،ops و customer handoff docs
  - [P15-SPRING-GATE-029](tasks/P15-SPRING-GATE-029.md) — Gate Backend Production
  - [P15-BUILDER-CODE-030](tasks/P15-BUILDER-CODE-030.md) — پروژه ساخت مستقل کلاینت با backend Spring

## P16 — iOS مستقل و اتصال اپ‌ساز

- هدف: خروجی iOS فقط بعد از اثبات تقاضا، با حساب و هویت حقوقی درست، پرداخت سازگار با نوع محصول و فرایند TestFlight/App Store قابل تکرار عرضه شود.
- Taskها:
  - [P16-IOS-BIZ-001](tasks/P16-IOS-BIZ-001.md) — بودجه، حساب و دامنه تحویل iOS مستقل
  - [P16-IOS-DISC-002](tasks/P16-IOS-DISC-002.md) — audit target فعلی،interop،dependency و build blockers
  - [P16-IOS-ADR-003](tasks/P16-IOS-ADR-003.md) — ADR lifecycle/navigation/native integration
  - [P16-IOS-CODE-004](tasks/P16-IOS-CODE-004.md) — iOS مستقل با BuildSpec و دو BackendProfile
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
  - [P16-QA-AUTO-015](tasks/P16-QA-AUTO-015.md) — تست shared و iOS integration با artifact release
  - [P16-QA-MANUAL-016](tasks/P16-QA-MANUAL-016.md) — iPhone کوچک/بزرگ،iPad در صورت Scope،دو نسخه iOS
  - [P16-QA-MANUAL-017](tasks/P16-QA-MANUAL-017.md) — RTL،Dynamic Type،VoiceOver،dark mode و keyboard
  - [P16-IOS-OPS-018](tasks/P16-IOS-OPS-018.md) — pipeline archive و تحویل مستقل iOS قابل مصرف Builder
  - [P16-IOS-DOC-019](tasks/P16-IOS-DOC-019.md) — privacy labels،screenshots،metadata،review notes
  - [P16-IOS-BIZ-020](tasks/P16-IOS-BIZ-020.md) — TestFlight با ۳–۵ کاربر/مشتری نماینده
  - [P16-IOS-OPS-021](tasks/P16-IOS-OPS-021.md) — review و staged release با stop/rollback plan
  - [P16-IOS-GATE-022](tasks/P16-IOS-GATE-022.md) — Gate خروجی مستقل iOS و Builder target مربوط
  - [P16-BUILDER-CODE-024](tasks/P16-BUILDER-CODE-024.md) — اتصال runner iOS به اپ‌ساز دو میزبان و portal مستقل

## P17 — Desktop مستقل و اتصال اپ‌ساز

- هدف: Desktop فقط برای use case اثبات‌شده—برای مثال پنل اپراتور یا دسترسی مشتری سازمانی— بسته‌بندی، امضا و توزیع شود. «قابل اجرا بودن Compose Desktop» معادل «محصول قابل فروش» نیست.
- Taskها:
  - [P17-DESKTOP-BIZ-001](tasks/P17-DESKTOP-BIZ-001.md) — بودجه و دامنه سیستم‌عامل‌های Desktop مستقل
  - [P17-DESKTOP-DISC-002](tasks/P17-DESKTOP-DISC-002.md) — target فعلی،dependency/native API و blocker inventory
  - [P17-DESKTOP-ADR-003](tasks/P17-DESKTOP-ADR-003.md) — ADR OS matrix،distribution و update channel
  - [P17-DESKTOP-CODE-004](tasks/P17-DESKTOP-CODE-004.md) — Desktop مستقل با BuildSpec و دو BackendProfile
  - [P17-DESKTOP-SEC-005](tasks/P17-DESKTOP-SEC-005.md) — OS keychain/credential vault و session policy
  - [P17-DESKTOP-CODE-006](tasks/P17-DESKTOP-CODE-006.md) — deep link/single-instance/payment callback
  - [P17-DESKTOP-CODE-007](tasks/P17-DESKTOP-CODE-007.md) — external browser payment و server verification
  - [P17-DESKTOP-CODE-008](tasks/P17-DESKTOP-CODE-008.md) — file picker/download/cache با sandbox/path policy
  - [P17-DESKTOP-CODE-021](tasks/P17-DESKTOP-CODE-021.md) — پخش واقعی محتوای آموزشی در Desktop
  - [P17-DESKTOP-OPS-009](tasks/P17-DESKTOP-OPS-009.md) — بسته‌های مستقل Desktop و artifact contract برای Builder
  - [P17-DESKTOP-OPS-010](tasks/P17-DESKTOP-OPS-010.md) — code signing و notarization در صورت نیاز
  - [P17-DESKTOP-OPS-011](tasks/P17-DESKTOP-OPS-011.md) — signed auto-update،channel و rollback
  - [P17-OBSERVABILITY-CODE-012](tasks/P17-OBSERVABILITY-CODE-012.md) — crash log/symbol و telemetry consent
  - [P17-QA-AUTO-013](tasks/P17-QA-AUTO-013.md) — آزمون بسته release Desktop و backendهای واقعی
  - [P17-QA-MANUAL-014](tasks/P17-QA-MANUAL-014.md) — install/upgrade/deep-link/offline/payment/update
  - [P17-QA-MANUAL-015](tasks/P17-QA-MANUAL-015.md) — RTL،keyboard-only،screen reader،DPIهای مختلف
  - [P17-DESKTOP-OPS-016](tasks/P17-DESKTOP-OPS-016.md) — download portal/checksum/release notes/support matrix
  - [P17-DESKTOP-BIZ-017](tasks/P17-DESKTOP-BIZ-017.md) — pilot قراردادی ۱–۳ مشتری
  - [P17-DESKTOP-DOC-018](tasks/P17-DESKTOP-DOC-018.md) — install/update/rollback/EOL/troubleshooting
  - [P17-DESKTOP-GATE-019](tasks/P17-DESKTOP-GATE-019.md) — Gate Desktop مستقل و هدف ساخت هر دو میزبان
  - [P17-BUILDER-CODE-020](tasks/P17-BUILDER-CODE-020.md) — اتصال runnerهای Desktop به اپ‌ساز

## P18 — پذیرش نهایی سه خانواده محصول

- هدف: اثبات استقلال واقعی Theme/Plugin،حذف/افزودن کد در ZIP با manifest و چهار کلاینت با دو backend؛ بدون پیاده‌سازی تکراری.
- [P18-QA-AUTO-001](tasks/P18-QA-AUTO-001.md) — اجرای ماتریس نهایی بسته و کلاینت با آزمون‌های موجود
- [P18-QA-MANUAL-002](tasks/P18-QA-MANUAL-002.md) — پذیرش نهایی پوسته مستقل و چهار بیلدر انتخابی
- [P18-QA-MANUAL-003](tasks/P18-QA-MANUAL-003.md) — پذیرش نهایی افزونه مستقل و چهار بیلدر انتخابی
- [P18-QA-MANUAL-004](tasks/P18-QA-MANUAL-004.md) — پذیرش چهار کلاینت با Spring و سه حالت WordPress
- [P18-PRODUCT-GATE-005](tasks/P18-PRODUCT-GATE-005.md) — پذیرش کامل سه خانواده محصول طبق درخواست مالک
