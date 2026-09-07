<div dir="rtl" align="right">

# ADR-006 — محصولات مستقل و مجوز قابلیت‌های Carmilla

- تاریخ: 2026-09-06
- وضعیت: جهت محصول بر اساس درخواست مالک ثبت شد؛ قرارداد تفصیلی و پیاده‌سازی در کارت‌های باقی‌مانده، تأیید انتشار نشده است.
- جایگزین تصمیم فعال: بندهای UI-only، Bridge-only ownership و write path در ADR-005؛ محتوای ADR-005 به‌عنوان تاریخچه حفظ می‌شود.
- مرجع: [تعریف کامل محصول](../../INDEPENDENT_PRODUCTS_SPEC_FA.md)، [صف](../../tasks.md)، `P04-PRODUCT-ADR-038` و `P04-WPPLUGIN-ADR-002`.

## تصمیم

سه خانواده خروجی مستقل داریم: Theme ZIP، Plugin ZIP و کلاینت‌های Android/iOS/Desktop/Web/PWA. سرور Kotlin/Spring مستقل و سرویس ساخت، دو جزء زیرساخت با مسئولیت متفاوت‌اند. WooCommerce پیش‌نیاز مجاز فروشگاه است؛ استقلال به معنی بی‌نیازی دو محصول Carmilla از یکدیگر است.

هر ZIP شامل میزبان مربوط، ماژول‌های انتخابی SKU و نسخه سازگار هسته مشترک است. سورس منطق domain، CPT، schema، REST، auth، permission، payment integration، تنظیمات قابلیت و اتصال اپ‌ساز در `wordpress/packages/carmilla-core/` به‌صورت مسیر هدف نگه‌داری می‌شود. این مسیر هدف است و وجود/تکمیل فعلی آن ادعا نمی‌شود. templateهای پوسته presentation-focused می‌مانند، ولی **کل محصول پوسته** خدمات کامل خریداری‌شده را از kernel داخلی خودش اجرا می‌کند.

افزونه علاوه بر API و wp-admin، نمایش عمومی پیش‌فرض مستقل از Carmilla Theme دارد. پوسته می‌تواند viewها را از طریق قرارداد مشخص override کند. هیچ نصب اجباری افزونه همراه یا MU-plugin برای فعال‌کردن پوسته مجاز نیست.

در co-install یک kernel سازگار و یک schema authority انتخاب می‌شود؛ ثبت entity، migration، cron، REST، callback و build دوباره انجام نمی‌شود. انتخاب نسخه با قرارداد capability/API/schema انجام می‌شود؛ صرف زودتر لودشدن یا انتخاب بیشترین شماره نسخه کافی نیست. جزئیات bootstrap زمان بارگذاری WordPress و failure isolation باید در ADR-002 تصویب و در تست واقعی اثبات شود.

## مالکیت و lifecycle

| بخش | مالک |
|---|---|
| داده‌های دامنه، شناسه‌ها و تاریخچه سفارش/دوره/نوبت | سایت مشتری |
| ثبت، validation، permission، schema و مسیر canonical نوشتن | kernel مشترک فعال |
| ظاهر و تنظیمات چیدمان وب | Theme Host |
| نمایش عمومی پیش‌فرض روی قالب دیگر | Plugin Host با view contract مشترک |
| وضعیت قابلیت و تنظیمات عملیاتی سایت | storage مشترک kernel، قابل مدیریت از هر میزبان |
| مجوز خرید/سایت/پروژه و حق ساخت | مرجع entitlement معتبر با provenance محصول |
| اجرای build و امضا | runner ایزوله خارج از WordPress |

غیرفعال‌سازی فیچر/میزبان داده را حذف نمی‌کند. ادامه خدمات پس از خاموشی یک میزبان فقط در صورت وجود میزبان دیگر با کد، schema و مجوز سازگار تضمین می‌شود. پس از خاموشی آخرین میزبان، خدمات متوقف و داده حفظ می‌شود. انتقال مجوز و داده باید صریح و قابل آزمون باشد.

## قرارداد خرید و فعال‌سازی

وضعیت مؤثر سمت سایت اشتراک قابلیت بسته‌شده، entitlement معتبر، انتخاب مدیر، closure وابستگی‌ها و آمادگی انتشار است. وضعیت مؤثر کلاینت با compiled ceiling و platform policy نیز محدود می‌شود. اعتبار خرید با toggle عمومی یا مخفی‌کردن UI جایگزین نمی‌شود. حق استفاده اپ‌ساز، پلتفرم build و قابلیت دامنه مستقل‌اند.

بسته‌ها از یک سورس و BuildSpec تولید می‌شوند؛ SKU شامل capabilityهای فروخته‌شده و وابستگی‌های اعلام‌شده است. مجوزها باید site/project/product binding، نسخه، سیاست offline/grace، upgrade و لغو را مشخص کنند. اجتماع مجوزهای دو محصول فقط برای همان سایت و طبق قرارداد provenance مجاز است؛ برنامه نباید از نبود مجوز، قطع شبکه یا نصب هم‌زمان نتیجه اعطای حق جدید بگیرد.

## کلاینت و اپ‌ساز

فقط `SPRING` و `WORDPRESS` پروفایل داده‌اند؛ Theme-only/Plugin-only/both، provider mode داخل WordPress هستند. BuildSpec برند، tenant، originهای مجاز، هویت اپ، SKU و سقف قابلیت را مستقل ثبت می‌کند. aliasهای قبلی با مسیر مهاجرت نگه داشته می‌شوند.

App Builder از هر دو میزبان تجربه مستقل درخواست تا دریافت خروجی دارد. P04 قرارداد و fake adapter را فراهم می‌کند؛ build واقعی Android/Web/PWA در P12، تکمیل iOS در P16 و Desktop در P17 Gate مستقل دارند. پروژه کلاینت متصل به Spring باید از portal/CLI مستقل بدون pairing وردپرس ساخته و تحویل شود. هیچ Gate اولیه‌ای وابسته به محصول نهایی آینده نمی‌شود که چرخه در صف بسازد.

## پیامد و کنترل تغییر

- استخراج دامنه و migration باید مرحله‌ای و با حفظ نام‌ها/شناسه‌های فعلی باشد؛ کپی دو پیاده‌سازی مستقل پذیرفته نیست.
- WordPress.org Theme Directory را نمی‌توان بدون بررسی به‌عنوان کانال توزیع نسخه دارای CPT معرفی کرد؛ کارت‌های کانال فروش مسئول تصمیم‌اند.
- DONEهای گذشته دوباره تیک نمی‌خورند. شواهد ناقص فاز سه در `P03-QA-REVIEW-023` بررسی و Gate آن با QA جدید بسته می‌شود.
- جزئیات schema مجوز، کلیدهای جدید و سیاست bootstrap هنوز خروجی کارت‌های اجرایی‌اند؛ این ADR مجوز deploy، فروش، migration واقعی یا تأیید دستی نیست.

</div>

## Entity Read/Write Matrix (ADR-038 Addition)

این ماتریکس وظیفه تفکیک مالکیت (Ownership) و مسیرهای خواندن/نوشتن (Read/Write Paths) را بر اساس تفکیک موجودیت‌های کاننیکال (Canonical) و سایت‌-محور (Site-owned) مشخص می‌کند.

| Entity (موجودیت) | Canonical Owner | Theme-only | Plugin-only (Bridge) | App Builder Role | Read Path (مسیر خواندن) | Write Path (مسیر نوشتن) |
|---|---|---|---|---|---|---|
| **محصولات (Products/SKU)** | Bridge/Woo | ❌ | ✅ (با Woo) | API Consumer | Bridge API / Woo API | wp-admin (Woo) / Bridge API |
| **تنظیمات پوسته (Theme Options)** | Theme | ✅ | ❌ | N/A | Theme Config API | Theme Customizer / wp-admin |
| **تنظیمات اپ (App Settings)** | Bridge | ❌ | ✅ | Configurator | Bridge API | App Builder Panel |
| **محتوای آکادمی (Courses/Lessons)** | Bridge | ❌ | ✅ | API Consumer | Bridge API | wp-admin (Bridge CPT) |
| **رزروها (Bookings/Appointments)** | Bridge | ❌ | ✅ | API Consumer | Bridge API | Bridge API / wp-admin |
| **نوشته‌ها و برگه‌ها (Posts/Pages)** | WordPress Core | ✅ (UI) | ✅ (API) | API Consumer | WP REST / Bridge API | wp-admin |
| **قالب‌های نمایشی (Templates)** | Theme | ✅ | ❌ | N/A | WP Core | Theme Editor |

### قواعد مرزبندی (Boundary Rules):
1. **کانونیکال بودن (Canonicality)**: افزونه (Bridge) صاحب (Owner) تمام دیتای بیزینسی (CPTهای آکادمی، کلینیک، سفارشات) است. پوسته (Theme) صرفاً صاحب دیتای نمایشی (UI/Templates) است.
2. **سناریوی Theme-only**: فقط امکان رندر مقالات و صفحات وبسایت عادی وجود دارد.
3. **سناریوی Plugin-only**: تمام APIها برای App و App Builder باز است، اما وب‌سایت خاموش است (Headless).
4. **سناریوی Both (Co-install)**: افزونه API می‌دهد، پوسته وب‌سایت را رندر می‌کند بدون تداخل در CPTهای یکدیگر.
5. **App Builder**: صرفاً تنظیمات خودش را روی Bridge (افزونه) می‌نویسد و از آن می‌خواند. پوسته هیچ اتصالی به App Builder ندارد.

## Plugin & Theme Co-install and Schema Authority (ADR-002 Addition)

این بخش معماری نحوه کار همزمان یا تکی پوسته (Carmilla Theme) و افزونه (Carmilla Bridge) در محیط وردپرس را مشخص می‌کند:

### 1. ساختار Kernel و Bootstrap
برای جلوگیری از هرگونه تداخل (Collision) یا دو بار لود شدن (Duplicate boot) در سناریوی Co-install، یک مکانیزم Shared Kernel تعریف می‌شود. 
هنگام لود وردپرس، افزونه یا پوسته‌ای که سریع‌تر لود شود (First-loaded)، کلاسی با عنوان Carmilla_Kernel را در حافظه ثبت می‌کند. محصول دوم ابتدا بررسی می‌کند class_exists('Carmilla_Kernel') و اگر وجود داشت، فرآیند Bootstrap خودش را متوقف کرده و به کرنل موجود متصل می‌شود.

### 2. اتوریتی اسکیما (Schema Authority)
افزونه (Bridge) صاحب مطلق دیتابیس، CPTها و Schemaهای مرتبط با اپلیکیشن و سرویس‌های پایه (فروش، نوبت‌دهی، آکادمی) است. 
- در حالت **Theme-only**: پوسته هیچ تیبل یا CPT کاستومی ایجاد نمی‌کند و فقط به REST API پیش‌فرض وردپرس و متا دیتای پست‌ها متکی است.
- در حالت **Plugin-only**: افزونه بدون رندر فرانت‌اند، تمامی CPTها و APIها را Register می‌کند تا کلاینت‌های هدلس (KMP) استفاده کنند.
- در حالت **Co-install**: افزونه Schemaها را مدیریت می‌کند و پوسته صرفاً iew contract (قرارداد نمایشی) را اجرا کرده و مقادیر را در Templateها رندر می‌کند.

### 3. استراتژی حل تضاد نسخه (Version Mismatch)
اگر نسخه پوسته و افزونه همخوانی نداشت (مثلاً Theme v1.0.0 و Plugin v2.0.0):
- همیشه نسخه بالاتر به عنوان **Version Authority** انتخاب می‌شود.
- اگر محصولی نسخه پایین‌تر داشت، از طریق قابلیت‌های پشتیبانی معکوس (Graceful Degradation) کار می‌کند اما به کاربر ادمین یک پیغام اخطار (Admin Notice) مبنی بر نیاز به آپدیت همگام نمایش داده می‌شود.
- در هیچ سناریویی، اختلاف نسخه نباید منجر به Fatal Error در سایت یا مسدود شدن شریان‌های حیاتی (مانند APIهای سبد خرید) شود.
