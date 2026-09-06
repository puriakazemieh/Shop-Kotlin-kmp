<div dir="rtl" align="right">

# شواهد بازبرنامه‌ریزی محصولات مستقل

- تاریخ: 2026-09-06
- درخواست: تحلیل دو مخزن و اصلاح صف باقی‌مانده برای Theme مستقل، Plugin مستقل، کلاینت‌های مستقل، فروش فیچر و App Builder مستقل هر میزبان.
- تصریح مالک محصول: WooCommerce مجاز است؛ فقط دو محصول Carmilla به یکدیگر وابسته نباشند.
- baseline مخزن KMP: `6499f9b0`؛ شناسه کامل و وضعیت صف در [baseline.json](baseline.json).
- روش: بررسی ساختاری/استاتیک، تطبیق کدهای کلیدی با قراردادها و کل صف، سه بررسی موازی و بازخوانی مستقیم شواهد اصلی؛ تغییر فقط مستندات.
- مسیر واقعی سرور: `D:/Android/AndroidStudioProjects/ShopServer/Shop`؛ کد backend در مخزن تو‌در‌تو است. تغییرات موجود ریشه ShopServer و فایل‌های untracked مشتری دست‌نخورده‌اند.

## یافته‌های تأییدشده و اثر بر صف

| شاهد در baseline | مشاهده | تصمیم اجرایی |
|---|---|---|
| `docs/architecture/adr/ADR-005-INDEPENDENT-PRODUCTS-OWNERSHIP.md:22` و بند Write Paths | پوسته UI-only و افزونه تنها مالک خدمات؛ در تعارض با RTK و P04-ADR-002 | ADR006 مرجع آینده؛ متن قدیمی با برچسب تاریخی؛ قرارداد تفصیلی038 |
| `wordpress/carmilla-bridge/carmilla-bridge.php:29`؛ `wordpress/carmilla-theme/inc/post-types.php:21`؛ `wordpress/carmilla-bridge/includes/class-cb-cpt.php:25` | ثبت/بارگذاری دامنه در دو میزبان و وابستگی به ترتیب | Shared Core، bootstrap/version و انتقال sliceهای جدا؛ migration/adoption042 |
| `wordpress/carmilla-bridge/includes/class-cb-manifest-controller.php:32` و `:74` | checkbox همه فیچرها از option؛ سند مجوز خرید مستقل در این مسیر نیست | قرارداد SKU039، resolver040، پنل041؛ این مشاهده ادعای شکست همه مجوزهای دیگر پروژه نیست |
| `wordpress/carmilla-bridge/includes/class-cb-cpt.php:62`؛ `wordpress/carmilla-theme/inc/cpt-public.php:19` | عمومی‌شدن نمایش برخی CPTها به Theme متکی است | frontend shell045 و viewهای هر vertical در کارت انتقال؛031 integration واقعی روی قالب ثالث |
| `wordpress/carmilla-theme/assets/js/booking.js:18` و `functions.php:207` | فرم رزرو به API مشترکی درخواست می‌دهد که provider کامل آن در افزونه است | slice028A مسیر صفحه→API→داده را در Theme-only آزمایش کند |
| `composeApp/src/commonMain/kotlin/com/kazemieh/shop/App.kt:133` و `:134` | backend از brand.id و tenant ثابت انتخاب می‌شود | BuildSpec024، generator024A و bootstrap024B |
| همان فایل `:159` تا `:170`؛ `core/config/capabilities/src/commonMain/kotlin/com/kazemieh/config/capabilities/FeatureFlagShadowMode.kt:42` | guard از local source و shadow خروجی legacy می‌دهد؛ wiring runtime با وجود helperها اثبات نمی‌شود | store025، menu025A و guard عملیاتی025B…E، تست wiring026 و QA027 |
| `composeApp/src/commonMain/kotlin/com/kazemieh/shop/GeneratedLocalFeatureManifest.kt:18`؛ `core/config/capabilities/src/commonMain/kotlin/com/kazemieh/config/capabilities/CompiledFeaturePolicy.kt:10` | قابلیت‌های generated و ceiling پایه ثابت‌اند | تولید بسته از BuildSpec معتبر؛ remote حق گسترش ceiling ندارد |
| `core/config/capabilities/src/commonMain/kotlin/com/kazemieh/config/capabilities/UrlResolvers.kt:35`؛ جست‌وجوی source سرور برای client-manifest/FeatureManifest/FEATURE_DISABLED | کلاینت Spring manifest می‌خواهد، پیاده‌سازی متناظر در جست‌وجو مشاهده نشد | حفظ/اصلاح P15-CODE005/015؛ fixture P03 مدرک production Spring نیست |
| `.github/workflows/wordpress-package.yml:15` و `:51` | ZIP کل پوشه و فقط target theme/plugin/both | assembler043 + CI019 با SKU، inventory و checksum؛ update044 |
| `.github/workflows/wordpress-integration.yml:66` | CI موجود هر دو را فعال می‌کند؛ smoke standalone به‌تنهایی آزمون نصب مستقل نیست | QA048/020 سه mode و SKU از ZIP واقعی |
| `.github/workflows/build-all.yml` و `.github/workflows/release.yml`؛ `androidApp/build.gradle.kts` | مسیرها و خروجی‌های توسعه/unsigned/JAR لزوماً artifact تجاری نیستند | P11/P16/P17 hardening موجود؛ target runnerهای P12/P16/P17؛ gate واقعی per target |
| `feature/details/src/jsMain/kotlin/com/kazemieh/details/VideoPlayer.js.kt:13`؛ `feature/details/src/jvmMain/kotlin/com/kazemieh/details/VideoPlayer.jvm.kt:17` | پخش وب و JVM placeholder است | P13-LMS-CODE028 و P17-DESKTOP-CODE021؛ LMS روی این targetها تا شاهد playback کامل نیست |

برای اصلاح‌های معماری ریسک انتقال MED/HIGH و اطمینان مشاهده HIGH است؛ کارهای کلی به sliceهای حداکثر M شکسته شدند. هزینه زمانی دقیق به baseline زمان اجرا وابسته است؛ زمان تحویل یا موفقیت build در این بازبینی تخمین قطعی داده نشد.

## وضعیت‌های تاریخی که نباید مجوز Gate باشند

`P03-QA-MANUAL-020` در صف و Final status برابر DONE است، اما completion و summary خودش نتیجه را ناقص/غیرقابل قبول می‌دانند. Master هنوز unchecked است. همچنین `P04-WPPLUGIN-ADR-001` DONE است ولی ADR005 Pending Review و completion آن نیازمند review انسانی است و dependency فاز قبل هنوز TODO است.

تاریخچه DONE و Evidence اصلی بازنویسی نشد. کارت `P03-QA-REVIEW-023` تنها READY برای تطبیق صریح ادعا/شاهد است و **فقط خروجی بازبینی سند** دارد؛ تکمیل آن تأیید QA قدیمی نیست. QA انسانی تازه `P03-QA-MANUAL-027` به Gate022 وصل شده و قرارداد جدید P04 نیز کارت038/ADR002 دارد. هیچ checkbox تازه‌ای DONE نشده است.

## مواردی که به‌عنوان نقص جدید ثبت نشدند

- WooCommerce پیش‌نیاز تأییدشده است؛ بازنویسی موتور فروشگاه پیشنهاد نشد.
- source مشترک در دو ZIP با استقلال runtime سازگار است؛ کپی دو پیاده‌سازی جدا پیشنهاد نشد.
- shadow mode در کلاس خودش رفتار عمدی دارد؛ مشکل برای هدف جدید، استفاده از آن در wiring فعال و باقی‌ماندن مصرف‌کننده legacy است.
- findings امنیتی ممیزی ژوئیه بدون شاهد تازه، نقص فعلی اعلام نشدند.
- ناسازگاری با قواعد مخزن رسمی Theme به مارکت‌های تجاری تعمیم داده نشد؛ بررسی کانال فروش در تسک‌های تجاری باقی است.
- هسته بسته‌بندی‌شده، third backend، companion plugin نصب خودکار یا اجرای Gradle/Xcode روی WordPress نیست.

## تغییرات و بازتولید بررسی

- [plan-mutations.json](plan-mutations.json): کارت‌های تازه، وابستگی‌های جدید و عنوان فازها.
- [p04-card-updates.json](p04-card-updates.json): اصلاح محتوایی کارت‌های موجود فاز چهار.
- [other-card-updates.json](other-card-updates.json): اصلاح‌های فازهای دیگر و کنترل‌ها.
- [change-index.json](change-index.json): فهرست نهایی کارت‌های افزوده/اصلاح‌شده.
- [clarification-updates.json](clarification-updates.json): تصریح دوباره مالک درباره حذف واقعی کد در ZIP و چهار بیلدر مستقل.
- [final-acceptance-tasks.json](final-acceptance-tasks.json): پنج کارت پذیرش نهایی فاز P18.
- [validation.json](validation.json): شمارش، لینک، graph، حفظ تاریخچه و نتیجه فرمان‌ها.
- [validate_plan.py](validate_plan.py): بررسی مجدد یکپارچگی مستندات بدون تغییر کارت‌ها.
- [build_preview.py](build_preview.py): تولید نسخه HTML راست‌چین از سند فارسی.

اسکریپت‌های موقت بازنویسی پس از استفاده حذف شدند؛ JSONها تاریخچه تصمیم/نگاشت تغییرند و نباید از آن‌ها صف فعال را بازتولید و overwrite کرد. مرجع اجرای آینده، خود کارت‌های فعلی و قرارداد فعال است.

## نتیجه نهایی این درخواست

- ۴۵ کارت جدید اضافه شد؛ ۱۳۸ کارت موجود از نظر محتوا یا metadata وابستگی به‌روزرسانی شد. پنج سند اصلی/صف/خلاصه/تصمیم تاریخی نیز هماهنگ شدند.
- صف شامل ۵۴۷ کارت است: ۱۰۱ DONE تاریخی حفظ‌شده، ۴۴۵ TODO و یک READY. هیچ اجرای محصول تازه DONE نشده است.
- graph وابستگی بدون cycle، شناسه/لینک کارت‌های جدید و هماهنگی وضعیت کارهای باقی‌مانده تأیید شدند؛ `git diff --check` موفق بود.
- ۱۸ ناسازگاری metadata تاریخی در کارت‌های DONE پیدا و در validation.json جدا ثبت شد؛ بازبینی آن‌ها به کارت READY ارجاع داده شد. نتیجه PASS اعتبارسنج مستندات، این شواهد قدیمی را تأیید نمی‌کند.
- نسخه HTML با `lang=fa` و `dir=rtl`، شش جدول و مثال JSON مستقل ساخته و ساختارش بررسی شد. ادعای QA تصویری مرورگر انجام نشده است.
- ۶۰ فایل جدید همین درخواست طبق فرمان کاربر در Git stage شدند؛ فایل قبلی untracked کاربر مانند P09-QA-MANUAL-006B جزو آن‌ها نیست. فایل‌های tracked اصلاح‌شده همچنان unstaged هستند و commit ایجاد نشد. جزئیات در [ثبت staging](git-staging.json) است.

## محدوده و آزمون انجام‌نشده

این نوبت Gradle، PHP/WP integration، runner build، برنامه‌های موبایل و تست دستی محصول اجرا نشدند؛ تغییر رفتار نرم‌افزار هم انجام نشده است. ممیزی خط‌به‌خط تمام فایل‌ها، penetration test، آزمایش کارایی و ممیزی مالی/حقوقی انجام نشده‌اند. شواهد runtime گذشته فقط برای تشخیص وضعیت تاریخی خوانده شدند و به‌عنوان نتیجه تازه بازاستفاده نشدند.

QA این درخواست مربوط به **مستندات** است: شناسه‌ها و لینک‌ها، dependency بدون cycle و بدون گم‌شدن کارت، یک READY، وضعیت تاریخی، هماهنگی queue/card/master، scope صرفاً docs و حفظ فایل‌های کاربر. QA UI/network/migration هر تغییر آینده در کارت همان کار تعریف شده است و تا تأیید واقعی انسان AWAITING_MANUAL_QA خواهد بود.

</div>
