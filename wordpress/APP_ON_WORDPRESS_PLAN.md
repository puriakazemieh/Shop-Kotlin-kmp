# پلن جامع: اپلیکیشن اندروید کارمیلا روی وردپرس (بدون سرور اختصاصی)

> **هدف کاربر:** از همین کدِ کاتلین (Compose Multiplatform) یک **اپلیکیشن اندروید** خروجی بگیریم که به‌جای اتصال به سرورِ Spring Boot خودمان، به یک **پلاگین وردپرس** وصل شود؛ به‌طوری‌که هر کسی که یک سایت وردپرسی دارد، فقط با **نصب آن پلاگین** بتواند تمام دیتای سایتش — محصولات، دوره‌ها، تست‌ها، نوبت‌ها، مقاله‌ها، سبد/سفارش، کیف‌پول و … — را مستقیم داخل اپ اندرویدی ببیند و مدیریت کند.
>
> این سند نقشه‌ی راه کامل و فاز‌به‌فاز آن کار است. **اصل حاکم: کدِ کاتلینِ موجود لمس نمی‌شود؛ همه‌چیز افزودنی است** (یک فلیورِ جدیدِ `wp` + توسعه‌ی پلاگین). بیلدِ فعلیِ «اپ ← سرور Spring Boot» دقیقاً مثل الان کار می‌کند.

---

## ۱) معماری کلی

```
┌────────────────────────┐        HTTPS + JWT         ┌──────────────────────────────┐
│   اپ اندروید (Compose) │  ───────────────────────▶  │   سایت وردپرس + WooCommerce    │
│   فلیورِ  wp            │   /wp-json/carmilla/v1/…   │   + پلاگین «Carmilla Bridge»   │
│                        │  ◀───────────────────────  │                              │
│  BrandConfig(          │        همان DTOهای اپ       │  محصولات ← WooCommerce        │
│   apiBaseUrl = سایت )  │                            │  مقاله‌ها ← Post + Gutenberg   │
└────────────────────────┘                            │  دوره/تست/نوبت ← CPT + جداول  │
                                                      │  سبد/سفارش/پرداخت ← Woo+درگاه  │
                                                      └──────────────────────────────┘
```

- **منبع داده = وردپرس.** موتور فروشگاه = **WooCommerce**. عمودی‌های آکادمی/کلینیک/تست = **CPT سفارشی + جداول اختصاصیِ پلاگین** (نه فورکِ LMS ثالث) تا با قراردادِ APIِ فعلیِ اپ یکی بماند.
- **قراردادِ ثابت:** اپ همان مسیرهای نسبی‌ای را که الان با سرور Spring Boot حرف می‌زند صدا می‌زند (`api/products`, `api/courses`, …)؛ فقط `baseUrl` عوض می‌شود. پلاگین این مسیرها را زیرِ namespaceِ `carmilla/v1` سِرو می‌کند: آدرسِ نهایی `https://<site>/wp-json/carmilla/v1/api/...`.
- **چرا این شکل؟** چون اپ از قبل یک لایه‌ی شبکه‌ی تمیز با `ApiConfig.baseUrlOverride` و سیستمِ برند/فلیور دارد؛ کافی است یک برندِ `wp` با `apiBaseUrl` اضافه کنیم و جایی که شکلِ داده‌ی وردپرس با DTO فرق دارد، یک `Impl` جایگزین در یک ماژولِ Koinِ اختیاری بایند کنیم.

---

## ۲) وضعیت فعلی (چه چیزی از قبل آماده است)

| مؤلفه | محل | وضعیت |
|---|---|---|
| پلاگینِ پایه «Carmilla Bridge» v0.1.0 | `wordpress/carmilla-bridge/` | ✅ فاز ۱ |
| احراز هویت JWT (`api/auth/login`,`register`,`forgot/reset`,`otp`) | `includes/class-cb-auth-controller.php`, `class-cb-jwt.php` | ✅ |
| کاتالوگ خواندنی (`api/products`,`api/products/{slug}`,`api/categories`,`api/banners`,`api/campaigns/active`) | `class-cb-catalog-controller.php` | ✅ فاز ۱ |
| بلاگ خواندن/نوشتن + نگاشت بلاک Gutenberg↔`BlogBlockDto` | `class-cb-blog-controller.php`, `class-cb-blocks.php` | ✅ |
| آپلود رسانه | `class-cb-media-controller.php` | ✅ |
| CPTهای story/banner/campaign | `class-cb-cpt.php` | ✅ |
| زیرساختِ برند/فلیور در اپ (`BrandConfig.apiBaseUrl`, `ApiConfig.baseUrlOverride`, `BrandRegistry`) | `core/designSystem/.../brand/Brand.kt`, `core/network/.../common/ApiConfig.kt` | ✅ (فقط برندِ `wp` را باید افزود) |
| قالبِ وردپرسِ «کارمیلا» (مسیر A، مستقل از این پلن) | `wordpress/carmilla-theme/` | ✅ v0.6.1 (merge‌شده در develop) |

**پس این پلن = تکمیلِ پلاگین (فاز ۲ به بعد) + افزودنِ فلیورِ `wp` به اپ.** فاز ۱ (فروشگاه پایه + بلاگ + JWT) از قبل هست.

---

## ۳) قراردادِ API — ۱۹۳ مسیر که پلاگین باید سِرو کند

اپ در `core/network` مجموعاً **۱۹۳ مسیرِ متمایز** دارد (روی برنچ `develop`). آن‌ها را در ۱۴ دامنه گروه‌بندی می‌کنیم و منبعِ وردپرسیِ هرکدام را مشخص می‌کنیم. ستونِ «وضعیت» = آنچه در پلاگین باید ساخته شود.

### الف) احراز هویت و پروفایل — `AuthApi`, `ProfileApi`, `AddressApi`
`api/auth/login|register|forgot-password|reset-password|reset-password-with-otp|send-login-otp|login-with-otp`
- منبع: کاربرانِ وردپرس + JWT. **فاز ۱ آماده.** باقی‌مانده: OTP واقعی (SMS)، پروفایل (`api/profile…`)، آدرس‌ها (`api/addresses…` → متای کاربر یا WooCommerce customer address).

### ب) کاتالوگ فروشگاه — `CatalogApi`, `InteractionApi`
`api/products`, `api/products/{slug}`, `api/products/{id}/frequently-bought-together`, `api/categories`, `api/banners`, `api/campaigns/active`, `api/stories`, `api/reviews…`, `api/questions…`, `api/price-alerts`, `api/stock-notifications`
- منبع: **WooCommerce** (product/variation/`product_cat`/gallery/attributes/stock/sale_price) + متای `cb_brand`/`cb_attributes` برای اسپک‌کارتِ per دسته.
- نظرات = WooCommerce reviews (+ متای تصویرِ نظر)؛ پرسش‌وپاسخ = کامنتِ نوعِ `cb_qna`؛ «موجود شد» = `api/stock-notifications`؛ هشدارِ قیمت = `api/price-alerts` (متای کاربر).
- **فاز ۱: لیست/جزئیات محصول آماده.** باقی‌مانده: reviews/questions/FBT/price-alerts/stock-notifications.

### ج) سبد و تسویه — `CartApi`
`api/cart`, `api/cart/items`, `api/cart/items/{variantId}`, `.../adjust`, `.../save-for-later`, `.../move-to-cart`, `api/cart/discount`
- منبع: **WooCommerce Store API / سشنِ سبد** با نگاشت به DTOهای فعلیِ اپ (نه شِمای خامِ Store API). save-for-later = آیتم‌های نگه‌داشته در متای کاربر.

### د) سفارش و پرداخت و کیف‌پول — `OrderApi`, `PaymentApi`, `WalletApi`, `ReturnRequestApi`, `RecurringOrderApi`
`api/orders`, `api/orders/{id}`, `.../cancel|reorder|track`, `api/payment/request`, `api/wallet/balance|transactions|top-up|withdraw`, `api/return-requests…`, `api/recurring-orders…`
- منبع: سفارش‌های **WooCommerce** + درگاهِ **ZarinPal** (callback → تغییرِ وضعیتِ سفارش). کیف‌پول = جدولِ اختصاصیِ پلاگین یا افزونه‌ی wallet. رهگیری (`/track`) = تاریخچه‌ی وضعیتِ سفارش. مرجوعی/تکراری = CPT/جدولِ اختصاصی.
- **محصولاتِ دیجیتال (دوره/تست/نوبت) = WooCommerce virtual** ⇒ سفارش بدونِ حمل‌ونقل/آدرس (مطابقِ `fix(orders): adapt order detail to digital`).

### ه) آکادمی — `AcademyApi`, `AdminAcademyApi`
خواندن: `api/courses`, `api/courses/{slug}`, `api/academy/my-courses`, `api/academy/courses/{id}/enroll|waitlist|refund-request|quiz|project…`, `api/academy/lessons/{id}/progress|quiz|questions`, `api/academy/placement-quiz…`, `api/academy/certificates`, `api/courses/certificates/verify/{certNumber}`, `api/academy/project/{submissionId}/comments`
- منبع: CPT `cb_course` + `cb_lesson` + متا (courseType/format/level/capacity/seatsTaken)؛ **enrollment/progress/quiz-attempt/certificate/waitlist در جداولِ اختصاصیِ پلاگین**. Hookِ «تکمیلِ سفارش → ثبت‌نامِ دوره».

### و) کلینیک (مشاوره/نوبت) — `ClinicApi`, `AdminClinicApi`
`api/therapists`, `api/therapists/{slug}`, `api/clinic/appointments`, `api/clinic/my-appointments`, `.../cancel|receipt`, `api/clinic/mood-checkins|journal|homework`, `api/clinic/therapist-match/questions|submit`, `api/clinic/therapists/{id}/messages|messaging-status`, `api/clinic/switch-requests…`
- منبع: CPT `cb_therapist` + **جداولِ slot/appointment با قفلِ اتمیک روی رزرو** + اعتبار جلسه (خرید→credit). یادداشت/مود/تمرین = متای کاربر یا جدول. پیام‌رسانی = کامنتِ `cb_msg`.

### ز) تست روان‌شناسی — `PsychTestApi`, `AdminPsychTestApi`
`api/psych-tests`, `api/psych-tests/{slug}`, `api/my-psych-tests`, `.../{userTestId}/questions|submit`
- منبع: CPT `cb_psychtest` + متا (questions/ranges) + جدولِ attempt. خرید از طریقِ `productSlug` (لینک به محصولِ WooCommerce). **امتیازها/تفسیر سمتِ سرور، به کلاینت لو نمی‌رود.**

### ح) درخواستِ دوره — `CourseRequestApi`
`api/course-requests`, `.../mine`, `POST api/course-requests`, `.../{id}/like`
- منبع: CPT `cb_course_request` + جدولِ like.

### ط) عضویت/باشگاه، معرفی، پشتیبانی، علاقه‌مندی، اخیراً دیده‌شده، باندل — `MembershipApi`, `ReferralApi`, `SupportApi`, `FavoriteApi`, `RecentlyViewedApi`, `BundleApi`
`api/memberships/mine|subscribe`, `api/referrals/mine`, `api/support/tickets…`, `api/favorites…`, `api/recently-viewed…`, `api/bundles`, `api/bundles/{slug}`
- منبع: متای کاربر (favorites/recently-viewed/referral)، جدول/CPT برای membership و tickets. باندل = محصولِ گروهیِ WooCommerce.

### ی) ادمین (مدیریت از داخلِ اپ) — `AdminApi` و سایر `Admin*Api`
حدود **۱۰۵ مسیرِ `api/admin/*`**: محصولات/تنوع/تصویر/ویدیو/دسته/تخفیف/آپشن، سفارش‌ها، آمار (`api/admin/stats`)، کیف‌پول/برداشت، بلاگ، دوره‌ها (۱۸)، درمانگرها (۲۳)، تست‌ها (۷)، باندل، استوری، سازمان/صندلی، مرجوعی، درخواستِ دوره، نظرات/سؤالات.
- منبع: همان WooCommerce/CPT/جداول، اما با **permissionِ ادمین** (نقشِ `manage_woocommerce`/`manage_options`). این بخش، اپ را به یک **پنلِ مدیریتِ کاملِ سایت** تبدیل می‌کند. **آخرین فاز** (سنگین‌ترین از نظرِ حجم).

> جمع: **۸۸ مسیرِ کاربری + ۱۰۵ مسیرِ ادمین = ۱۹۳**. لیستِ کاملِ منبع در `wordpress/carmilla-bridge/README.md` نگه‌داری می‌شود و با هر فاز به‌روز می‌گردد.

---

## ۴) نگاشتِ داده (موجودیتِ اپ → مقصد در وردپرس)

| موجودیت | مقصد در وردپرس |
|---|---|
| محصول/تنوع/دسته/موجودی/قیمت‌تخفیف | WooCommerce (product/variations/`product_cat`/stock/sale_price) |
| برند/attributesِ محصول | متای محصول (`cb_brand`,`cb_attributes`) |
| مقاله (بلاک‌محور) | پستِ وردپرس + نگاشتِ Gutenberg↔`BlogBlockDto` |
| استوری/بنر/کمپین | CPT `cb_story`/`cb_banner`/`cb_campaign` (✅ فاز ۱) |
| دوره/بخش/درس/ثبت‌نام/پیشرفت/کوییز/گواهی | CPT `cb_course`+`cb_lesson` + جداولِ اختصاصی |
| درمانگر/اسلات/نوبت/اعتبارِ جلسه | CPT `cb_therapist` + جداولِ slot/appointment (قفلِ اتمیک) |
| تستِ روان‌شناسی/سؤال/بازه/attempt | CPT `cb_psychtest` + متا + جدولِ attempt |
| درخواستِ دوره + لایک | CPT `cb_course_request` + جدولِ like |
| سفارش/سبد/پرداخت | WooCommerce + ZarinPal |
| کیف‌پول/تراکنش | جدولِ اختصاصیِ پلاگین (یا افزونه‌ی wallet) |
| مرجوعی/سفارشِ تکراری/عضویت/تیکت/معرفی | CPT/جدولِ اختصاصی + متای کاربر |
| «خرید→ثبت‌نام/اعتبار/دسترسی» | Hookِ `woocommerce_order_status_completed` |
| برند/White-Label | هر سایت = یک نصبِ مستقل (پلاگین روی هر سایت) |

---

## ۵) احراز هویت و امنیت

- **JWT** (فاز ۱ آماده): هدرِ `Authorization: Bearer …`؛ کلیدِ امضا از `wp_salt()` یا ثابتِ `CB_JWT_SECRET` در `wp-config.php`. توکنِ refresh در پاسخِ login.
- **permission callbackها:** خواندنِ عمومی = بدونِ احراز؛ نوشتنِ کاربری = `require_login`؛ مسیرهای `api/admin/*` = `require_admin` (`manage_woocommerce`/`manage_options`).
- **OTP:** برای SMS واقعی نیازمندِ درگاهِ پیامکِ کاربر است (تنظیمِ افزونه)؛ در نبودِ آن، fallback به ایمیل/کدِ تستی.
- **نرخ‌گیری و nonce** برای مسیرهای حساس؛ اعتبارسنجیِ ورودی در هر controller.

---

## ۶) سمتِ اپ کاتلین — فلیورِ `wp` (کاملاً غیرمخرب)

روی برنچِ همین کار (`claude/android-app-on-wordpress-plan`، مبتنی بر `develop`):

1. **افزودنِ برندِ `wp`** در `core/designSystem/.../brand/Brand.kt`:
   ```kotlin
   val WpBrand = BrandConfig(
       id = "wp", appName = "کارمیلا (وردپرس)",
       colors = CarmilaBrandColors,
       apiBaseUrl = "https://<site>/wp-json/carmilla/v1/",  // یا از BuildConfig/تنظیماتِ کاربر
       features = BrandFeatures(academy = true, clinic = true, psychTests = true, /* … */)
   )
   ```
   و ثبت در `BrandRegistry`. `ApiConfig.baseUrlOverride` از قبل این را می‌گیرد.
2. **فلیورِ `wp`** در `composeApp/build.gradle.kts` (کنارِ `carmila`/`atris`/…): `buildConfigField("String","BRAND","\"wp\"")` + `applicationIdSuffix=".wp"`. **قابلیتِ ورودِ آدرسِ سایت در زمانِ اجرا** (صفحه‌ی اولِ اپ: «آدرسِ سایتِ وردپرسِ خود را وارد کنید») تا هر صاحبِ سایتی بتواند اپِ آماده را به سایتِ خودش وصل کند — بدونِ نیاز به build مجدد.
3. **ماژولِ شبکه‌ی قابل‌تعویض** (`wordpressNetworkModule`): فقط جایی که شکلِ پاسخِ وردپرس با DTO فرق دارد، یک `WordPressXxxApiImpl` می‌سازیم و در فلیورِ `wp` جایگزینِ `XxxApiImpl` می‌کنیم؛ بقیه بازاستفاده. **پیاده‌سازیِ سرورِ Spring Boot و بیلدِ `carmila` دست‌نخورده می‌مانند.**
4. خروجی: `assembleWpDebug`/`assembleWpRelease` ⇒ «اپ ← وردپرس»؛ `assembleCarmilaDebug` ⇒ «اپ ← سرورِ فعلی» (بدونِ تغییر — اثباتِ غیرمخرب‌بودن).

**مرجع:** `core/network/.../di/networkModule.kt`, `core/network/.../common/{ApiConfig,PlatformConfig,HttpClientFactory}.kt`, `composeApp/.../shop/App.kt` (`initKoin(brand)`).

---

## ۷) فازبندیِ پیاده‌سازیِ پلاگین

| فاز | دامنه | خروجی | ریسک |
|---|---|---|---|
| **۱ (✅ انجام‌شده)** | JWT + کاتالوگِ خواندنی + بلاگ + رسانه + CPTها | لیست/جزئیاتِ محصول و مقاله در اپ | کم |
| **۲** | **تجارتِ کامل:** سبد + تسویه + سفارش + پرداخت (ZarinPal) + کیف‌پول + آدرس/پروفایل + reviews/questions | خرید سرتاسری از داخلِ اپ | **بالا** (پرداخت) |
| **۳** | **آکادمی:** CPT دوره/درس + enroll/progress/quiz/certificate/placement/project + Hookِ خرید→ثبت‌نام | دوره‌ها کاملاً کار می‌کنند | متوسط |
| **۴** | **کلینیک + تست:** درمانگر/اسلات/نوبت با قفلِ اتمیک + مود/ژورنال/تمرین + تستِ روان‌شناسی + خرید→دسترسی | نوبت‌دهی و تست کار می‌کنند | متوسط/بالا |
| **۵** | **افزوده‌ها:** درخواستِ دوره، عضویت/باشگاه، معرفی، پشتیبانی، مرجوعی، سفارشِ تکراری، علاقه‌مندی، اخیراً‌دیده، باندل، «موجود شد» | برابریِ کاملِ فیچرها | کم/متوسط |
| **۶** | **ادمینِ کامل (۱۰۵ مسیرِ `api/admin/*`)** | مدیریتِ کاملِ سایت از داخلِ اپ | بالا (حجم) |

هر فاز = یک PRِ جدا با تستِ دود.

---

## ۸) تجارت و پرداخت (نکاتِ کلیدیِ فاز ۲)

- **سبد:** نگاشتِ WooCommerce cart-session به DTOِ اپ (نه شِمای خامِ Store API) تا کلاینت تغییری لازم نداشته باشد.
- **پرداخت:** `api/payment/request` → ساختِ سفارشِ WooCommerce + هدایت به **ZarinPal**؛ callbackِ درگاه → `woocommerce_order_status_*`. برای اپ، پرداخت داخلِ WebView/مرورگرِ سیستم باز می‌شود و با deep-link برمی‌گردد.
- **کیف‌پول:** جدولِ `wp_cb_wallet` (balance/transactions) + `top-up` از طریقِ همان درگاه.
- **دیجیتال:** محصولِ دوره/تست/نوبت را **virtual** بسازید تا سفارش، حمل‌ونقل/آدرس نخواهد.

---

## ۹) عمودی‌ها — جداولِ اختصاصیِ پلاگین

برای داده‌ی رابطه‌ایِ سنگین (enrollment، progress، slot/appointment، attempt، wallet) به‌جای پستمِتای کند، **جداولِ اختصاصی** با `dbDelta` در فعال‌سازیِ پلاگین ساخته می‌شوند:
`wp_cb_enrollment`, `wp_cb_progress`, `wp_cb_quiz_attempt`, `wp_cb_certificate`, `wp_cb_appointment`, `wp_cb_slot`, `wp_cb_session_credit`, `wp_cb_psych_attempt`, `wp_cb_wallet`, `wp_cb_wallet_txn`.
- **رزروِ نوبت** با `SELECT … FOR UPDATE`/قفلِ سطری تا دو کاربر یک اسلات را نگیرند.
- **اعتبارِ جلسه**: Hookِ خرید credit می‌دهد؛ رزرو مصرف، لغو بازمی‌گرداند.

---

## ۱۰) رسانه و تصاویر

- تصاویرِ محصول/دوره/مقاله از کتابخانه‌ی رسانه‌ی وردپرس با URLِ کامل (و اندازه‌های ثبت‌شده) سِرو می‌شوند.
- آپلود از داخلِ اپ (ادمین) = `api/admin/*/media/upload` → `wp_handle_upload` (فاز ۱ برای بلاگ آماده).

---

## ۱۱) تست و تأیید

- **پلاگین:** تستِ دودِ مستقلِ PHP در `wordpress/carmilla-bridge/tests/smoke.php` (فعلاً JWT + بلاک PASS) — با هر فاز موردِ جدید افزوده می‌شود؛ `php -l` روی همه‌ی فایل‌ها؛ `curl`/Postman روی `/wp-json/carmilla/v1/…`.
- **اپ:** `assembleWpDebug` یا دسکتاپ با `-Dbrand=wp`؛ چک: محصول/مقاله/دوره/تست لود شود، سبد/تسویه‌ی WooCommerce عبور کند، از پنلِ ادمینِ اپ محتوا ساخته و روی سایت دیده شود. **بیلدِ `carmila` بدونِ تغییر کار کند (اثباتِ غیرمخرب‌بودن).**
- **سازگاری:** DTOها با تستِ سریال‌سازی مقابلِ نمونه‌پاسخِ پلاگین چک می‌شوند تا شکسته نشوند.

---

## ۱۲) بسته‌بندی و توزیع

1. **پلاگین:** `wordpress/carmilla-bridge/` → زیپ → «افزونه‌ها ← افزودن ← بارگذاری» در هر سایتِ وردپرس. نیازمندِ WooCommerce برای بخشِ فروشگاه.
2. **اپ:** یک APK/AAB از فلیورِ `wp` که در اولین اجرا **آدرسِ سایت** را می‌پرسد؛ همان APK برای هر صاحبِ سایتی کار می‌کند (بدونِ build مجدد). برای برندِ اختصاصی، فلیورِ جدید با رنگ/آیکن/`apiBaseUrl` ثابت.
3. مستندِ نصبِ گام‌به‌گام برای کاربرِ نهایی (نصبِ پلاگین → ساختِ کلیدِ JWT → واردکردنِ آدرس در اپ).

---

## ۱۳) ریسک‌ها و تصمیم‌های باز

- **بزرگ‌ترین ریسک:** انتقالِ کاملِ تجارت + کیف‌پول + ZarinPal به WooCommerce (فاز ۲) و عمودی‌های آکادمی/کلینیک/تست که معادلِ استانداردِ وردپرس ندارند (حجمِ بالای CPT/جدول).
- **پرداختِ درون‌اپ اندروید:** سیاستِ Google Play برای کالای فیزیکی/خدمات، درگاهِ وب را مجاز می‌داند؛ برای محتوای دیجیتالِ درون‌اپ باید بررسی شود.
- **OTP/SMS و ویدیوکالِ کلینیک** وابسته به سرویسِ خارجیِ صاحبِ سایت‌اند (مثلِ پلتفرمِ فعلی).
- **ویدیوی دوره‌ها:** لینکِ مستقیم/بیرونی (بدونِ DRM) در فازِ اول.
- تصمیم: تستِ روان‌شناسی سرور-محورِ کامل است؛ منطقِ امتیازدهی باید در PHP بازتولید شود (نه در کلاینت).

---

## ۱۴) نقشه‌ی راهِ خلاصه

```
فاز ۱  ✅  JWT + کاتالوگ + بلاگ + رسانه            (انجام‌شده)
فاز ۲  ▶  تجارتِ کامل (سبد/سفارش/پرداخت/کیف‌پول)  + فلیورِ wp در اپ
فاز ۳     آکادمی (دوره/درس/کوییز/گواهی)
فاز ۴     کلینیک (نوبت با قفل) + تستِ روان‌شناسی
فاز ۵     افزوده‌ها (عضویت/معرفی/پشتیبانی/مرجوعی/…)
فاز ۶     ادمینِ کامل (۱۰۵ مسیرِ مدیریتی)
```

هر فاز مستقل، افزودنی و با PR و تستِ جدا. کدِ کاتلینِ سرور-محور در تمامِ فازها دست‌نخورده می‌ماند.

---

_این سند مرجعِ برنامه‌ریزیِ «اپ روی وردپرس» است و با پیشرفتِ هر فاز به‌روزرسانی می‌شود. مرجعِ لیستِ کاملِ endpointها: کدِ `core/network` (۱۹۳ مسیر) و `wordpress/carmilla-bridge/README.md`._
