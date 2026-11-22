# ADR-038 — قرارداد مالکیت و مسیرهای محصولات مستقل

- تاریخ: 2026-11-22
- وضعیت: قرارداد هدف P04-PRODUCT-ADR-038؛ پیاده‌سازی و عرضه جداگانه سنجیده می‌شود.
- مرجع: [ADR-006](ADR-006-INDEPENDENT-PRODUCTS-AND-ENTITLEMENTS.md)، [تعریف محصولات](../../INDEPENDENT_PRODUCTS_SPEC_FA.md).

## قاعدهٔ مالکیت

مالک دادهٔ WordPress سایت مشتری است؛ میزبان Theme یا Plugin مالک انحصاری دادهٔ دامنه نیست. هر ZIP باید هستهٔ مشترک و ماژول‌های خریداری‌شدهٔ خودش را حمل کند. در هر سایت، یک هستهٔ سازگار، مسیر canonical ثبت، اعتبارسنجی، مجوز، schema و نوشتن را فراهم می‌کند. Theme و Plugin فقط ورودی UI/مدیریت و adapter میزبان‌اند. WooCommerce مرجع canonical دادهٔ کالا، سبد و سفارش و عملیات مالی آن است. کلاینت Spring دادهٔ سرور مستقل خودش را دارد؛ همگام‌سازی آن با WordPress در این قرارداد نیست.

`Theme-only` و `Plugin-only` هر دو باید خدمات خریداری‌شده را بدون محصول Carmilla دیگر عرضه کنند؛ افزونه روی قالب ثالث نمایش عمومی دارد. در `both` دو ورودی به همان داده و همان write service می‌رسند، نه دو ثبت CPT/REST/cron/callback. پس از خاموشی آخرین میزبان، داده باقی می‌ماند و خدمات Carmilla متوقف می‌شود. ترتیب انتخاب نسخه و bootstrap در P04-WPPLUGIN-ADR-002 تعیین می‌شود.

## ماتریس entity، مسیر و schema هدف

در ستون‌های میزبان، «هسته» یعنی kernel بسته‌شده در همان ZIP؛ این جدول ادعای وجود پیاده‌سازی فعلی نیست. تمام writeهای مدیریتی نیز باید capability، nonce/احراز هویت و entitlement مؤثر را طبق نوع عملیات کنترل کنند؛ دسترسی مستقیم به storage مسیر عمومی نیست.

| Entity / شناسهٔ محفوظ | مالک داده و schema authority | Read canonical | Write canonical | Theme-only / Plugin-only / both | SKU و Gate بعدی |
|---|---|---|---|---|---|
| نوشته، برگه، رسانه، کاربر | سایت / WordPress Core | WP API از adapter مشترک | WP Core با مجوز WordPress | هر دو میزبان می‌خوانند؛ هر دو نصب نیز همان رکورد را می‌بیند | پایه؛ P04 integration |
| کالا، دسته، سبد، سفارش، بازپرداخت | سایت / WooCommerce | Woo CRUD/Store API پشت adapter | Woo CRUD/Store API؛ callback مالی واحد | هر میزبان با Woo فعال؛ هر دو، یک سفارش | `commerce.core`؛ P04/P05 |
| story/banner/campaign و تنظیمات ظاهر | سایت / WP Core برای محتوا؛ Theme برای layout | هسته برای محتوا؛ Theme برای layout | WP Core/هسته برای محتوا؛ Theme settings برای layout | محتوا در هر دو؛ layout پوسته فقط در Theme؛ Plugin renderer مستقل | پایه/محتوا؛ P04 |
| دوره، درس، ثبت‌نام، پیشرفت، آزمون، گواهی؛ `cb_course` | سایت / هستهٔ مشترک | API آموزش مشترک | service آموزش مشترک، نه CPT میزبان | فهرست/درس/فرم در هر دو؛ both همان ID | `academy.core`؛ P04/P13 |
| ارائه‌دهنده، تقویم، slot، نوبت؛ `cb_therapist`, `cb_appointment` | سایت / هستهٔ مشترک | API نوبت مشترک | service رزرو اتمیک مشترک | صفحه و فرم هر دو؛ both همان رزرو | `clinic.booking`؛ P04/P14 |
| جلسهٔ مشاوره و اطلاعات خصوصی | سایت / هستهٔ مشترک با دسترسی محدود | API مجازِ مشاوره | service جدا از booking؛ دسترسی خصوصی | تنها در بستهٔ دارای مجوز؛ UI هر میزبان | کلید جدید در P04-ENTITLEMENT-DATA-039؛ P14 |
| پروندهٔ خصوصی | سایت / هستهٔ مشترک با دسترسی محدود | API خصوصی مستقل | service پرونده مستقل | هر دو میزبان فقط با مجوز و نقش لازم | کلید و سیاست در 039/P14 |
| کاتالوگ تست، پاسخ و نتیجه؛ `cb_psychtest` | سایت / هستهٔ مشترک با تفکیک دادهٔ حساس | API مجازِ تست | service تست؛ پاسخ/نتیجه خصوصی | مستقل از booking در هر میزبان | `psych.tests`؛ P04/P14 |
| پیام، پشتیبانی، import/export و تنظیم provider | سایت / هستهٔ مشترک؛ secret فقط PHP server | API عملیاتی مجاز | service مشترک با کنترل نقش و ممیزی | هر میزبانِ دارای ماژول؛ both یک عملیات | SKUهای مربوط در 039؛ P06/P07 |
| قابلیت موجود، entitlement، toggle و runtime manifest | سایت / هستهٔ مشترک؛ مرجع حق خرید با provenance | resolver مشترک؛ manifest فقط گزارش | مسیر مدیریتی مشترک با اعتبارسنجی خرید، وابستگی و نقش | هر دو پنل یک وضعیت سایت؛ نصب ZIP حق خرید نمی‌دهد | 039/040/041 |
| BuildSpec، درخواست، وضعیت و خروجی ساخت | سایت برای درخواست/تنظیم؛ سرویس ساخت برای job/artifact | API builder مشترک | adapter مشترک به runner ایزوله، idempotent | Theme یا Plugin به تنهایی مسیر کامل؛ both یک job/request | builder target جدا؛ P04/P12/P16/P17 |

شناسه‌های منتشرشده مانند `academy.core`، `clinic.booking`، `psych.tests` و نام CPTهای `cb_*` حفظ می‌شوند. کلیدهای تفکیک مشاوره/پرونده و alias یا migration نسخه‌دار در 039 تعیین می‌شوند؛ این سند نام تازه را به schema جاری تحمیل نمی‌کند. مهاجرت انتقالی در P04-WORDPRESS-DATA-042 با snapshot و forward-fix انجام می‌شود؛ خاموشی قابلیت یا میزبان حذف داده نیست.

## مرز میزبان، قابلیت و خروجی

| حالت | UI عمومی و مدیریت | API و write | App Builder |
|---|---|---|---|
| Theme-only | template و پنل Theme | kernel داخل Theme | پنل Theme → adapter مشترک → runner بیرونی |
| Plugin-only | renderer، فرم و پنل Plugin روی قالب ثالث | kernel داخل Plugin | پنل Plugin → همان adapter → runner بیرونی |
| Both | Theme می‌تواند view را override کند؛ پنل‌ها یک وضعیت | یک kernel سازگار و یک مسیر canonical | یک درخواست، یک job؛ provenance خرید حفظ می‌شود |
| آخرین میزبان خاموش | UI خدمات Carmilla متوقف | API/job جدید متوقف؛ داده سایت حفظ | درخواست جدید متوقف؛ artifact قبلی خودکار حذف نمی‌شود |

قابلیت مؤثر برابر است با اشتراک کد بسته‌شده، entitlement معتبر همان سایت/محصول، toggle مدیر، وابستگی و آمادگی انتشار. `false` در بسته یعنی کد اختصاصی آن ماژول نباید در ZIP باشد. Builder Android، iOS، Desktop و PWA هر کدام مجوز و ماژول مستقل دارند؛ Web مستقل نیز هدف تحویل است و نگاشت SKU دقیقش در 039 تعیین می‌شود. مجوز builder، حق اتصال API و حق قابلیت دامنه یکی نیستند. backend کلاینت فقط `WORDPRESS` یا `SPRING` است؛ سه حالت میزبان WordPress پروفایل جدا نیستند.

## پرسش‌های سپرده‌شده به کارت بعدی

| تصمیم باز | مسئول/کارت | شرط پذیرش |
|---|---|---|
| زمان candidate registration، انتخاب kernel و schema/API mismatch | معماری WordPress / P04-WPPLUGIN-ADR-002 | انتخاب مستقل از first-loaded و صرف شمارهٔ بالاتر؛ failure isolation |
| کلیدهای SKU، entitlement provenance، alias و تفکیک مشاوره/پرونده | قرارداد فروش و داده / P04-ENTITLEMENT-DATA-039 | هیچ خرید یا قابلیت از نصب ZIP یا toggle استنباط نشود |
| مسیر adoption دادهٔ CPT و option فعلی | مهاجرت WordPress / P04-WORDPRESS-DATA-042 | حفظ ID، snapshot و forward-fix |
| renderer واقعی و آزمون مستقل ZIP | میزبان‌ها و QA فاز P04 | Theme-only، Plugin-only و both با write واحد |

این قرارداد فقط ownership هدف را تصویب می‌کند. هیچ محصول، runner یا migration با این سند آمادهٔ فروش شمرده نمی‌شود.
