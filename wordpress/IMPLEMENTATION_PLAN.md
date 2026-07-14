# پلن جامع پیاده‌سازی وردپرس — پوسته + پلاگین/اتصال اپ (پلتفرم کارمیلا)

> مبنای این پلن: برنچ **`develop`** (جدیدترین و کامل‌ترین؛ شاملِ همه‌ی عمودی‌ها + ری‌دیزاین ادمین/پروفایل/جزئیات + پاسِ ریسپانسیو + فیچرهای تازه‌ی دوره/نوبت/تست/درخواستِ دوره + سفارش‌های دیجیتال). برنچ‌های `eyraci` و `admin-profile-pages-redesign-w3uk6x` زیرمجموعه‌ی `develop`‌اند.

---

## ۱) هدف و معماری

کاربر یک پلتفرم **چندعمودی/چندبرند** دارد:
- **کلاینت** `Shop-Kotlin-kmp` — Compose Multiplatform (Android/iOS/Desktop/JS) با عمودی‌های **فروشگاه، آکادمی (دوره)، کلینیک (مشاوره/نوبت)، تست روان‌شناسی، مقایسه، باندل، درخواستِ دوره** + سیستم White-Label (`BrandConfig`/`BrandFeatures`/`ApiConfig.baseUrlOverride`).
- **سرور** `Shop-Kotlin-Spring-Boot` — Kotlin/Spring Boot 4 + JPA + PostgreSQL.
- دیزاین «کارمیلا» توکن‌محور در `core/designSystem` (منشأ HTML در ریپو نیست؛ پوسته از توکن‌ها بازسازی می‌شود).

**دو خواسته (دو مسیر مستقل و افزودنی):**
1. **قالب وردپرسی** — بازسازی دیزاین کارمیلا برای استفاده‌ی مستقیم در وردپرس.
2. **پلاگین وردپرسی + اتصال اپ** — پلاگینی که داده‌ی وردپرس (فروشگاه/مقاله/دوره/…) را با همان قرارداد API اپ عرضه کند، و یک خروجی از همان اپ Compose که به‌جای سرور خودی به وردپرس وصل شود (خواندن + مدیریت + تجارت کامل).

### تصمیم‌های قطعی
- **منبع اصلی داده = وردپرس**؛ موتور فروشگاه = **WooCommerce**.
- عمودی‌های آکادمی/کلینیک/تست/درخواست‌دوره = **CPT سفارشی در پلاگین** (نه فورک LMS ثالث) تا خروجی دقیقاً با DTOهای فعلی اپ یکی بماند.
- محصولات دیجیتال (دوره/تست/نوبت) = WooCommerce **virtual** (بدون حمل‌ونقل/آدرس).
- پوسته = بازسازی از توکن‌های `core/designSystem`.

### اصل حاکم: غیرمخرب بودن
- **کد کاتلین موجود لمس نمی‌شود.** قالب یک پروژه‌ی PHP جداست. اتصال اپ فقط یک **برند/فلیور `wp` + ماژول شبکه‌ی قابل‌تعویض** اضافه می‌کند؛ بیلد «اپ ← سرور Spring Boot» دقیقاً مثل الان کار می‌کند.

### وضعیت فعلی (انجام‌شده)
- **پلاگین `carmilla-bridge` فاز ۱** ✅ — JWT auth، محصول (WooCommerce)، مقاله (نگاشت Gutenberg↔BlogBlock)، CPTهای story/banner/campaign، آپلود مدیا؛ تست دود PHP سبز.
- **پایه‌ی پوسته `carmilla-theme`** ✅ — توکن‌ها→CSS (روشن/تاریک، RTL، Vazirmatn)، سه عرض محتوا 640/840/1200، گرید تطبیقی، `theme.json`، بوت‌استرپ؛ با اسکرین‌شات تأیید شد.

---

## ۲) مدل داده‌ی مشترک در وردپرس (پایه‌ی هر دو مسیر)

| موجودیت (سرور/اپ) | مقصد در وردپرس |
|---|---|
| محصول/تنوع/دسته/موجودی/قیمت‌تخفیف | **WooCommerce** (product/variations/`product_cat`/stock/sale_price) |
| brand + attributes محصول | متای `cb_brand`, `cb_attributes` + اسپک‌کارت per دسته (ساعت/عطر) |
| مقاله (بلاک‌محور) | پست وردپرس + نگاشت دوطرفه‌ی Gutenberg ↔ `BlogBlockDto` |
| استوری/بنر/کمپین | CPT `cb_story` / `cb_banner` / `cb_campaign` |
| **آکادمی** (Course/Section/Lesson/Enrollment/Progress/Quiz/Certificate/Waitlist/Project) | CPT `cb_course` + متا (courseType/format/level/capacity/seatsTaken) + جداول enrollment/progress/quiz در پلاگین |
| **کلینیک** (Therapist/Slot/Appointment/SessionCredit) | CPT `cb_therapist` + جداول slot/appointment + قفل اتمیک رزرو + اسلاتِ تقویمی |
| **تست روان‌شناسی** (سرور-محور، خریدنی) | CPT `cb_psychtest` + متا (questions/ranges) + جدول attempt؛ خرید از طریق `productSlug` |
| **درخواست دوره** (جدید) | CPT `cb_course_request` + جدول like |
| **محصول دیجیتال** (دوره/تست/نوبت) | WooCommerce **virtual** (سفارش بدون حمل‌ونقل/آدرس) |
| سفارش/سبد/پرداخت/کیف‌پول | WooCommerce (Store API + orders) + درگاه **ZarinPal** + کیف‌پول |
| «خرید → ثبت‌نام دوره / اعتبار جلسه / دسترسی تست» | Hook روی تکمیل سفارش WooCommerce |
| فیچرهای فروشگاهی افزوده (migrations 019–038) | رفرال، سفارش تکرارشونده، عضویت، «موجود شد»، مرجوعی، تیکت پشتیبانی، تاریخچه‌ی وضعیت سفارش — فاز بعدی |
| برند / White-Label | هر برند = یک سایت وردپرس جدا (DB/دیپلوی مستقل) یا Multisite؛ پالت برند در پوسته |

### قرارداد Endpoint (namespace `carmilla/v1`، baseUrl = `/wp-json/carmilla/v1/`)
مسیرها **آینه‌ی مسیرهای فعلی اپ روی `develop`** می‌مانند تا اپ کمترین تغییر را ببیند:
- فروشگاه/بلاگ: `api/products`, `api/blogs`
- آکادمی: `api/courses`, `api/courses/{slug}`, `api/academy/my-courses`, `POST api/academy/courses/{id}/enroll`, progress/quiz/certificate
- کلینیک: `api/therapists`, `api/therapists/{slug}`, `api/clinic/my-appointments`, `POST api/clinic/appointments`
- تست: `api/psych-tests`, `api/psych-tests/{slug}`, `api/my-psych-tests`, `.../{userTestId}/questions`, `.../submit`، ادمین `api/admin/psych-tests`
- درخواست دوره: `api/course-requests`, `api/course-requests/mine`, `POST api/course-requests`, `.../{id}/like`، ادمین `api/admin/course-requests`
- احراز هویت: `api/auth/login|register|refresh`, `api/users/me`

---

## ۳) سیستم ریسپانسیو (باید عیناً رعایت شود — مطابق `core/designSystem/WindowSize.kt`)

- **بریک‌پوینت‌ها:** Compact `<600` / Medium `600–840` / Expanded `≥840`.
- **سه عرضِ محتوا (`ContentWidth`):** readable **640px** (فرم/جزئیات محصول/تنظیمات/احراز هویت)، medium **840px** (لیست تک‌ستونه/سبد/پروفایل)، wide **1200px** (گرید/داشبورد/خانه). در پوسته: `--content-readable/medium/wide` + کلاس‌های `.container--readable/medium/wide` ✅.
- **`responsiveMaxWidth`/`ResponsiveContainer`:** وسط‌چین با پدینگِ متقارن روی نمایشگر پهن؛ روی موبایل no-op.
- **گرید تطبیقی:** ۲/۳/۴ ستون (`adaptiveGridColumns`).
- **پوسته‌ی ناوبری:** نوار پایینِ موبایل ↔ نوار کناری (`SideNavRail`) روی `≥840`.
- **قیمت‌ها:** ارقام فارسی + جداکننده‌ی هزارگان «٬» + واحد «تومان» (معادل `core/designSystem/util/PriceFormat.kt`).

---

## ۴) مسیر ۱ — قالب وردپرسی «کارمیلا»

پروژه‌ی مستقل PHP در `wordpress/carmilla-theme/`. هیچ فایل کاتلینی تغییر نمی‌کند.

**فاز ۱ — پایه (انجام‌شده ✅):** `tokens.css` + `base.css` + `theme.json` + بوت‌استرپ (`style.css`/`functions.php`/`header.php`/`footer.php`/`index.php`).

**فاز ۲ — سلسله‌مراتب قالب** (نگاشت صفحه‌ی اپ → قالب؛ عرضِ محتوا در پرانتز):
- `front-page.php` ← `ProductsOverviewScreen`: استوری، هیرو، دسته، گرید محصول، کمپین تایمردار، بنر، تیزر بلاگ، بازدید اخیر (**wide**).
- `woocommerce/` overrides: آرشیو فروشگاه/دسته با گرید ۲/۳/۴ (**wide**)؛ `single-product.php` ← `DetailsScreen` بازطراحی‌شده — تنوع، اسپک‌کارت per دسته، نظر+پرسش با پاسخ درون‌خطی و **تصویرِ نظر** (**readable**).
- `single.php`/`archive.php` ← `BlogDetailScreen`/`BlogListScreen` (detail **readable**، list **wide**).
- **قالب‌های CPT عمودی‌ها:** `single-cb_course.php` + آرشیو ← `CourseDetail/CourseLearn/CourseList` بازطراحی‌شده (detail **readable**)؛ `single-cb_therapist.php` ← `TherapistDetail/List`؛ قالب تست ← `PsychTestList/TakeTest`؛ قالب درخواستِ دوره (لیست + فرم ثبت + لایک).
- سبد/تسویه ← WooCommerce استایل‌شده (**medium**)؛ حساب/پروفایل ← `ProfileScreen` بازطراحی‌شده + ورودی عمودی‌ها (**medium**)؛ سفارش ← `OrderDetailScreen` با نمای فاکتور و پشتیبانیِ سفارشِ دیجیتال (بدون حمل‌ونقل).
- `ComparisonScreen` ← جدولِ تمام‌عرض (استثناء).

**فاز ۳ — کامپوننت‌های partial** معادل کارمیلا: `PrimaryButton`, `CarmillaBadge`, `StoryRing`, product/blog/course/therapist card، فیلترچیپ، `BlogContentRenderer`، هدر/نوار کناری.

**فاز ۴ — White-Label:** پالت برند از `theme.json` + CSS variables؛ هر برند یک سایت/پیکربندی.

**مرجع:** `core/designSystem/.../{Colors,Typography,Shape,Dimens,AppTheme,WindowSize,util/PriceFormat}.kt`؛ صفحات: `details/DetailsScreen.kt`، `profile/ProfileScreen.kt`، `academy/{detail,learn,list}/*`، `psychtest/*`، `courserequest/*`؛ `docs/RESPONSIVE_COVERAGE.md` + `docs/DESIGN_IMPLEMENTATION_PLAN.md`.

---

## ۵) مسیر ۲ — پلاگین وردپرسی + اتصال اپ

### ۵-۱) پلاگین «Carmilla Bridge» (بک‌اند)
پایه (فروشگاه+بلاگ+JWT) ساخته شده؛ اضافه می‌شود:
- **تجارت کامل:** نگاشت WooCommerce Store API (سبد/تسویه/سفارش) به DTO فعلی اپ + **ZarinPal** + کیف‌پول. اقلام دوره/تست/نوبت = **virtual**.
- **آکادمی:** CPT دوره/درس + endpointهای بالا + Hook «خرید→ثبت‌نام».
- **کلینیک:** CPT درمانگر + جداول slot/appointment (اسلاتِ تقویمی، قفل اتمیک) + اعتبار جلسه + ادمین.
- **تست روان‌شناسی (کامل):** CPT تست + CRUD ادمین + انجام/تفسیر؛ خرید با `productSlug`.
- **درخواست دوره:** endpointهای ثبت/لیست/لایک + ادمین.
- **فاز بعد:** رفرال/تکرارشونده/عضویت/«موجود شد»/مرجوعی/پشتیبانی/تاریخچه‌ی سفارش.

### ۵-۲) اتصال اپ (خروجی روی وردپرس) — غیرمخرب، برنچ فلیور از `develop`
- **برند/فلیور جدید `wp`** با `BrandConfig(apiBaseUrl = "https://<site>/wp-json/carmilla/v1/")` (بازاستفاده از `ApiConfig.baseUrlOverride` و `initKoin(brand)`).
- **ماژول شبکه‌ی قابل‌تعویض:** جایی که قرارداد وردپرس با DTO فرق دارد، `WordPressXxxApiImpl` در یک `wordpressNetworkModule` بایند می‌شود؛ بقیه بازاستفاده. **سرور Spring Boot دست‌نخورده.**
- نتیجه: `assembleWpDebug` → «اپ ← وردپرس»؛ `assembleCarmilaDebug` → «اپ ← سرور فعلی».

**مرجع:** `core/network/.../common/{PlatformConfig,HttpClientFactory,ResultHandler,ApiConfig}.kt`، `core/network/.../di/networkModule.kt`، `core/designSystem/.../brand/Brand.kt`، `composeApp/build.gradle.kts`، `composeApp/.../shop/App.kt`.

---

## ۶) استراتژی شاخه
- **مبنا = `develop`.** برنچ کاری وردپرس (`claude/wordpress-plugin-theme-plan-ruouji`) با `develop` مرج/به‌روز است و تغییرات آن به `develop` برمی‌گردد.
- برنچ فلیور `wp` اپ از `develop` زده شود (فقط افزودنی).

## ۷) تأیید (Verification)
- **پلاگین:** `curl`/Postman روی `/wp-json/carmilla/v1/…` (JWT، CRUD محصول/مقاله/دوره/درمانگر/تست/درخواست‌دوره، رزرو با قفل ظرفیت، Hook خرید)؛ `php wordpress/carmilla-bridge/tests/smoke.php`.
- **پوسته:** فعال‌سازی روی وردپرس تست؛ مقایسه‌ی چشمی هر صفحه با دیزاین (رنگ/تایپ/RTL/دارک) در ۳ بریک‌پوینت؛ عبور تسویه‌ی WooCommerce (فیزیکی و دیجیتال).
- **اپ:** `assembleWpDebug` یا دسکتاپ `-Dbrand=wp`؛ لود محصول/مقاله/دوره، ساخت محتوا از اپ و دیده‌شدن در وردپرس؛ بیلد `carmila` بدون تغییر کار کند (اثبات غیرمخرب‌بودن).

## ۸) فازبندی پیشنهادی و ریسک‌ها
1. فروشگاه + بلاگ (آماده) → 2. پوسته: قالب‌ها → 3. آکادمی (پلاگین) → 4. کلینیک/تست/درخواست‌دوره → 5. تجارت کامل + ZarinPal + کیف‌پول → 6. فیچرهای افزوده.
- **بزرگ‌ترین ریسک‌ها:** انتقال کامل تجارت/کیف‌پول/ZarinPal؛ عمودی‌های بدون معادل استاندارد وردپرس (CPT سفارشی، حجم بالا).
- ویدیو/DRM دوره‌ها، پیامک واقعی، ویدیوکال = تصمیم‌های زیرساختیِ جدا (فعلاً لینک مستقیم/بیرونی، مثل خود پلتفرم).
- منبع HTML اصلی دیزاین در ریپو نیست؛ ارسال `export-screens` سرعت/دقت پوسته را بالا می‌برد.
