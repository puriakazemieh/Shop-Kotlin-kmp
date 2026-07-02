# پلن جامع پیاده‌سازی دیزاین «کارمیلا» (کلاینت + سرور)

این سند، **نقشه‌ی راهِ اجرایی و مرحله‌به‌مرحله** برای رساندنِ اپِ کلاینت (`Shop-Kotlin-kmp`) و
سرور (`Shop-Kotlin-Spring-Boot`) به تطابقِ کاملِ دیزاینِ «ترمه/کارمیلا» است.
مرجعِ دیزاین: پوشه‌ی `export-screens` (۱۶ صفحه‌ی HTML مستقل + هندآفِ Markdown).

> **چطور از این سند استفاده کنم؟**
> این سند یک چک‌لیستِ زنده است. هر بار که خواستید، از بالای «فازهای اجرا» شروع کنید،
> یک گام را بردارید، تیک بزنید و کامیت کنید. هر گام مشخص می‌کند **کدام فایل‌ها**
> عوض می‌شوند، **معیارِ پذیرش** چیست، و آیا **کارِ سرور** لازم دارد یا نه.

**علائم:** ✅ انجام‌شده · 🟡 نیمه‌کاره · ⬜ مانده · 🔌 نیازمندِ کارِ سرور
**شاخه‌ی کار (هر دو ریپو):** `claude/project-review-implementation-plan-eyraci`

---

## ۰) خلاصه‌ی مدیریتی

پس از خواندنِ عمیقِ هر دو ریپو و مقایسه‌ی مو‌به‌موی آن‌ها با ۱۶ صفحه‌ی دیزاین:

- **ری‌دیزاین تقریباً ۹۰–۹۵٪ انجام شده است.** توکن‌های دیزاین یک‌به‌یک منطبق‌اند،
  تمِ روشن/تیره + فونتِ وزیرمتن + چیدمانِ RTL کامل است، و تقریباً همه‌ی ۱۶ صفحه پیاده و
  به بک‌اندِ **واقعی** (نه mock) وصل شده‌اند.
- **سرور تقریباً کاملِ فیچرها را پوشش می‌دهد** (کاتالوگ، واریانت، موجودی، نظر/پرسش،
  علاقه‌مندی، سبد + saved-for-later، کد تخفیف، سفارش، کیف‌پول + برداشتِ شبا، پرداختِ زرین‌پال،
  بلاگ، استوری، کمپین/بنر، داشبوردِ آمار، و کلِ CRUD ادمین).
- بنابراین کارِ باقی‌مانده **کوچک، مشخص و لبه‌ای** است؛ عمدتاً چند میکروفیچرِ صفحه‌ی محصول،
  صفحه‌ی جستجوی مستقل، چتِ پشتیبانی، و چند پالشِ نهایی.

جدولِ فازها در بخشِ «فازهای اجرا» آمده است.

---

## ۱) تطابقِ توکن‌های دیزاین — ✅ کامل

جدولِ زیر مقادیرِ استخراج‌شده از HTML را با `core/designSystem/…/Colors.kt`
(و `Typography.kt`, `Shape.kt`, `Dimens.kt`) مقایسه می‌کند:

| توکن | دیزاین (light / dark) | کلاینت | وضعیت |
|---|---|---|---|
| accent | `#20305C` / `#6E8AE0` | `AccentLight` / `AccentDark` | ✅ |
| accent-soft | `#EAEDF6` / `#1e2840` | `AccentSoftLight` / `AccentSoftDark` | ✅ |
| gold | `#B08D57` / `#C8A36A` | `GoldLight` / `GoldDark` | ✅ |
| bg | `#F6F5F1` / `#0F1320` | `BgLight` / `BgDark` | ✅ |
| surface | `#FFFFFF` / `#181D2C` | `SurfaceLight` / `SurfaceDark` | ✅ |
| ink / ink-soft | `#192038` / `#6B7184` | `InkLight` / `InkSoftLight` | ✅ |
| line | `#E7E4DD` / `#2A3142` | `LineLight` / `LineDark` | ✅ |
| ok / sale / star | `#1F9D6B` / `#D8453B` / `#E7A93B` | `Ok*` / `Sale*` / `Star*` | ✅ |
| فونت | Vazirmatn | `Res.font.typeface_fa` + `AppTypography()` | ✅ |
| شعاع | 8 / 11 / 13 / 16 / 22px | `Radius.xs/sm/button/md/lg` = 8/11/13/16/22dp | ✅ |
| فاصله | — | `Spacing.xs..xxl` = 4/8/12/16/24/32dp | ✅ |

> **نتیجه:** لایه‌ی توکن هیچ کارِ باقی‌مانده‌ای ندارد. هر رنگ/شعاع جدید باید از همین توکن‌ها بیاید.

---

## ۲) حسابرسیِ صفحه‌به‌صفحه (۱۶ صفحه)

| # | صفحه‌ی دیزاین | فایلِ کلاینت | وضعیت | شکافِ باقی‌مانده |
|---|---|---|---|---|
| ۰۱ | خانه | `feature/catalog/…/ProductsOverviewScreen.kt` | ✅ | استوری، هیرو، دسته‌ها، کمپینِ تایمردار، بنر، تیزرِ بلاگ، بازدیدِ اخیر، trust badges — همه هستند |
| ۰۲ | دسته‌بندی/لیست | `feature/catalog/…/CategorySearchScreen.kt` | 🟡 | پنلِ فیلترِ BottomSheet (قیمت/رنگ/سایز/موجودی) کامل شود |
| ۰۳ | جستجو | `Screen.Search` (تعریف‌شده، بی‌روت) | ⬜ | صفحه‌ی جستجوی مستقل + جستجوهای اخیر/پرطرفدار + حالتِ خالی |
| ۰۴ | جزئیات محصول | `feature/details/…/DetailsScreen.kt` | 🟡 | راهنمای سایز/نظر/پرسش هست؛ **«مفید بود»، «موجود شد خبرم کن»، فیت مدل، تخمین ارسال** نیست |
| ۰۵ | سبد خرید | `feature/cart/…/CartScreen.kt` | ✅ | saved-for-later، کد تخفیف، خلاصه‌ی قیمت |
| ۰۶ | تسویه | `feature/cart/…/checkout/CheckoutScreen.kt` | ✅ | آدرس، کیف‌پول، زرین‌پال/در محل (استپرِ ۳مرحله‌ای اختیاری) |
| ۰۷ | علاقه‌مندی | `feature/profile/…/FavoritesScreen.kt` | ✅ | گریدِ کارت + حالتِ خالی |
| ۰۸ | بلاگ/مجله | `feature/blog/…/BlogListScreen.kt` | ✅ | — |
| ۰۹ | جزئیات مقاله | `feature/blog/…/BlogDetailScreen.kt` | ✅ | رندرِ بلوکی + مرتبط |
| ۱۰ | پروفایل | `feature/profile/…/ProfileScreen.kt` | ✅ | فهرستِ تنظیماتِ اختصاصی (اختیاری) |
| ۱۱ | سفارش‌ها | `feature/orders/…/list/OrderListScreen.kt` | ✅ | بَجِ وضعیتِ فارسی |
| ۱۲ | جزئیات/رهگیری سفارش | `feature/orders/…/detail` + `…/tracking` | 🟡 | تایم‌لاین کلاینت‌ساید هست؛ تایم‌استمپِ واقعیِ سرور نیست |
| ۱۳ | آدرس‌ها | داخلِ `ProfileScreen` (`AddressBottomSheet`) | ✅ | مدیریتِ کامل؛ صفحه‌ی مستقل لازم نیست |
| ۱۴ | کیف پول | `feature/profile/…/WalletScreen.kt` | ✅ | موجودی، شارژ، برداشتِ شبا، تراکنش |
| ۱۵ | ورود/ثبت‌نام | `feature/auth/*` | 🟡 | ورود برندشده؛ ثبت‌نام/فراموشی/بازنشانی نیازِ پالشِ برند |
| ۱۶ | پنل ادمین | `feature/admin/*` | 🟡 | همه‌ی صفحات هست؛ داشبوردِ آماریِ غنی‌تر (سرورش آماده) |
| — | چت پشتیبانی | `feature/support/…/ContactUsScreen.kt` | ⬜ | الان فرم است؛ دیزاین چتِ حبابی می‌خواهد (نیاز سرور) |

> این جدول با `grep` روی کد تأیید شده است — مثلاً `SizeGuideDialog`، `StoryCircleRow`،
> `HomeHero`، `PromoBanners`، `BlogTeaserCard`، `RecentlyViewed`، `TrustBadges` واقعاً در کد هستند.

---

## ۳) شکاف‌های سمت سرور (تأییدشده که وجود ندارند)

| # | قابلیتِ گمشده | برای کدام صفحه | فاز |
|---|---|---|---|
| S1 | رأی «مفید بود» روی نظر (`helpfulCount` + endpoint) | جزئیات محصول | A |
| S2 | «موجود شد خبرم کن» (اشتراکِ موجودی + endpoint + تریگر) | جزئیات محصول | A |
| S3 | دامنه‌ی تیکت/پیامِ پشتیبانی | چت پشتیبانی | C |
| S4 | تاریخچه‌ی زمان‌دارِ وضعیتِ سفارش (تایم‌استمپِ هر مرحله) | رهگیری | D |
| S5 | (اختیاری) `soldCount` در `ProductSummary` برای نشان‌ها | کارت/جزئیات | E |

> `brand` روی `ProductEntity` **از قبل هست** (نیازی به کارِ سرور برای برند نیست).
> آدرس و کیف‌پول از قبل سمتِ سرور کامل‌اند.

---

## ۴) فازهای اجرا (مرحله‌به‌مرحله)

ترتیبِ پیشنهادی از بالا به پایین است. هر گام مستقل است و می‌تواند یک کامیت جدا باشد.

### فاز A — تکمیلِ صفحه‌ی محصول (بیشترین ارزشِ دیزاین)

- [x] **A1 🔌 سرور — رأی «مفید بود»**
  - `catalog/persistence/entity/ProductReviewEntity.kt`: افزودنِ `helpfulCount: Int = 0`.
  - entity جدید `ProductReviewHelpfulEntity(reviewId, userId)` با قیدِ یکتای `(reviewId, userId)`.
  - `catalog/api/ReviewController.kt`: `POST /api/reviews/{reviewId}/helpful` (toggle) →
    برگرداندنِ `ReviewResponse` با `helpfulCount` و `helpfulByMe`.
  - migration در `db/init/` (فایلِ شماره‌دارِ بعدی) + مپ در `ReviewResponse`/mapper.
  - **پذیرش:** رأیِ تکراریِ یک کاربر شمارش را دوباره بالا نبرد؛ toggle درست کار کند.

- [x] **A2 کلاینت — دکمه‌ی «مفید بود»**
  - `core/network`: متد `markReviewHelpful(reviewId)` در `InteractionApi`(+Impl).
  - `core/domain`: مدلِ `Review` + `helpfulCount/helpfulByMe`، usecaseِ `MarkReviewHelpfulUseCase`.
  - `core/data`: مپ در `InteractionRepositoryImpl`.
  - `feature/details/…/component/ReviewItem.kt`: دکمه/شمارنده‌ی «👍 مفید بود (n)»، آپدیتِ خوش‌بینانه.
  - `feature/details/…/DetailsViewModel.kt`: intent + state.
  - **پذیرش:** تپ روی دکمه شمارنده را عوض کند و پس از رفرش پایدار بماند.

- [x] **A3 🔌 سرور — «موجود شد خبرم کن»**
  - entity `StockNotificationEntity(userId, variantId, createdAt, notified=false)`.
  - `POST /api/products/{productId}/notify-me?variantId=…` → ثبتِ اشتراک (اگر ناموجود بود).
  - در `AdminCatalogService.setInventory`/`adjustInventory`: هنگامِ گذرِ واریانت از ناموجود به موجود،
    `StockNotificationService.onVariantRestocked` اشتراک‌های `notified=false` را علامت می‌زند
    (فعلاً فقط علامت‌گذاری؛ ارسالِ SMS/ایمیل اختیاری/بعدی).
  - **مسیرِ نهایی:** `POST /api/stock-notifications` (به‌جای زیرمسیرِ عمومیِ `/api/products/**` تا احراز هویت تضمین شود).
  - migration `019_add_stock_notifications.sql`.
  - **پذیرش:** ثبتِ اشتراکِ تکراری خطا ندهد؛ رکورد ساخته شود. ✓

- [x] **A4 کلاینت — دکمه‌ی «موجود شد خبرم کن»**
  - api/usecase مشابهِ A2 (`RequestBackInStockUseCase`).
  - `DetailsScreen.kt`: وقتی واریانتِ انتخابی ناموجود است، به‌جای «افزودن به سبد»
    دکمه‌ی «موجود شد خبرم کن» نشان بده؛ پس از ثبت، حالتِ «ثبت شد» + Snackbar.
  - **پذیرش:** برای واریانتِ ناموجود دکمه ظاهر و پس از تپ، وضعیت عوض شود.

- [x] **A5 کلاینت — فیت مدل + تخمین زمان ارسال**
  - «اطلاعاتِ فیتِ مدل»: از `product.attributes` (JSON موجود) بخوان؛ اگر کلیدِ فیت بود نشان بده.
  - «تخمینِ زمانِ ارسال»: کلاینت‌ساید از شهرِ آدرسِ پیش‌فرض (`GetDefaultAddressUseCase`) یک بازه‌ی روز نشان بده.
  - محلِ درج: `DetailsScreen.kt`، نزدیکِ بلوکِ نشان‌های خدمات.
  - **پذیرش:** با آدرسِ پیش‌فرض، متنِ تخمین نمایش داده شود؛ بدونِ آدرس، fallbackِ عمومی.

### فاز B — تجربه‌ی جستجوی مستقل

- [x] **B1 کلاینت — صفحه‌ی جستجو**
  - `feature/catalog`: `SearchScreen.kt` + `SearchViewModel` (استفاده از همان `GetProductsUseCase` با پارامترِ `q`).
  - `core/navigation/…/AppNavigation.kt`: وصل‌کردنِ `composable<Screen.Search>` + ورودی از هدرِ خانه.
  - اجزاء: سرچ‌بار، گریدِ نتیجه (`MainProductCard`)، حالتِ خالی (`InfoCard`)، «جستجوهای اخیر» (ذخیره‌ی محلی با `multiplatform-settings`).
  - **پذیرش:** تایپ + جستجو نتیجه بدهد؛ خالی، حالتِ خالی نشان دهد؛ اخیرها ذخیره/بازیابی شوند.

### فاز C — چت پشتیبانی

- [ ] **C1 🔌 سرور — دامنه‌ی پشتیبانی**
  - ماژول/پکیجِ `support`: `SupportTicketEntity(userId,subject,status,createdAt)` +
    `SupportMessageEntity(ticketId,senderRole,body,createdAt)`.
  - endpointها: `POST /api/support/tickets`, `GET /api/support/tickets`,
    `GET /api/support/tickets/{id}/messages`, `POST /api/support/tickets/{id}/messages`.
  - ادمین: `GET /api/admin/support/tickets` + پاسخ‌دادن. migration + DTO/mapper.
  - **پذیرش:** کاربر تیکت بسازد و پیام رد و بدل شود؛ ادمین ببیند و پاسخ دهد.

- [ ] **C2 کلاینت — رابطِ چت**
  - api/usecase/repository برای پشتیبانی در `core/*`.
  - `feature/support/…/ContactUsScreen.kt`: تبدیل به لیستِ حبابیِ پیام (فرستنده/گیرنده) + نوارِ ورودی پایین.
  - (اختیاری) صفحه‌ی ادمینِ پشتیبانی زیرِ `feature/admin`.
  - **پذیرش:** ارسالِ پیام و نمایشِ تاریخچه به‌سبکِ چت.

### فاز D — ارتقای رهگیری سفارش (اختیاری)

- [ ] **D1 🔌 سرور — تاریخچه‌ی وضعیت**
  - `OrderStatusHistoryEntity(orderId,status,at)`؛ در سرویسِ تغییرِ وضعیت یک رکورد ثبت کن.
  - افشا در `GET /api/orders/{id}/track` (لیستِ مرحله‌ها با زمان).
  - **پذیرش:** هر تغییرِ وضعیت یک رکوردِ زمان‌دار بسازد.

- [ ] **D2 کلاینت — بایندِ تایم‌لاینِ واقعی**
  - `feature/orders/…/tracking/OrderTrackingScreen.kt`: به‌جای استنتاجِ کلاینت‌ساید،
    تایم‌استمپ‌های واقعیِ سرور را نشان بده (fallback به منطقِ فعلی اگر خالی بود).
  - **پذیرش:** تایم‌لاین زمان‌های واقعی را نشان دهد.

### فاز E — پالشِ نهایی (فقط کلاینت مگر ذکر شود)

- [ ] **E1 — پنلِ فیلترِ دسته**
  - `CategorySearchScreen.kt`: BottomSheetِ فیلتر (قیمت [بازه]، رنگ، سایز، فقط‌موجود) با `CarmillaFilterChip`.
  - از پارامترهای موجودِ `GET /api/products` (`minPrice,maxPrice,inStock,options`) استفاده کن.
  - **پذیرش:** اعمالِ فیلتر، لیست را واقعاً فیلتر کند.

- [ ] **E2 — برندینگِ صفحاتِ auth**
  - `feature/auth`: صفحاتِ ثبت‌نام/فراموشی/بازنشانی هم‌سبکِ صفحه‌ی ورود (لوگوباجِ «ک»، کارتِ سفید روی کِرِم).

- [ ] **E3 — داشبوردِ آماریِ ادمین**
  - `feature/admin/products/…/AdminPanelScreen.kt`: کارت‌های آمار از `GET /api/admin/stats`
    (درآمد/سفارش/محصول/مشتری) + نمودارِ فروشِ ۷روزه.

- [ ] **E4 — ممیزیِ نهاییِ اجزاء مشترک**
  - بازبینیِ هدر/نوارِ ناوبریِ پایین/Toast(`ContentWithMessageBar`)/Modal در برابرِ اسپک (اکثراً منطبق؛ فقط اصلاحِ جزئی).

### فاز F — تأیید نهایی

- [ ] بیلد و اجرای مقابلِ سرور، سپس QA بصریِ هر صفحه در برابرِ `export-screens/NN-*.html`.

---

## ۵) نگاشتِ صفحه ↔ روت (برای مرجع)

روت‌های تعریف‌شده در `core/common/…/Screen.kt` و `AppNavigation.kt`:
`HomeGraph`(خانه/کاتالوگ/سبد)، `ProductDetail`، `CategorySearch`، `Search`(بی‌روت)،
`Checkout`، `PaymentCompleted`، `Favorites`، `Wallet`، `Profile`، `MyOrders`،
`OrderDetail`، `OrderTracking`، `BlogList`، `BlogDetail`، `ContactUs`، `Settings`،
`AuthGraph`(Login/Register/Forgot/Reset)، و `AdminPanel` + زیرصفحه‌های ادمین.

---

## ۶) نحوه‌ی تأیید بصری (روی دستگاهِ شما)

محیطِ ابری قادر به build نیست؛ تأیید روی دستگاهِ شما:

- **دسکتاپ (سریع‌ترین):** `gradlew.bat :composeApp:run`
- **وب:** `gradlew.bat :composeApp:jsBrowserDevelopmentRun`
- **اندروید:** از Android Studio
- **پیش‌نیاز:** JDK 17+ و اینترنت (برای وابستگی‌ها).
- **برای داده‌ی واقعی:** سرورِ Spring Boot را بالا بیاورید و `BASE_URL` را در
  `core/network/…/PlatformConfig.*.kt` تنظیم کنید.
- **مرجعِ QA:** هر صفحه را با فایلِ HTML متناظر در `export-screens` کنار هم بگذارید.

> **یادآوریِ امنیتی (از روادمپِ قبلی):** توکنِ زرین‌پال چون در تاریخچه‌ی git بوده باید در پنل rotate شود.

---

_این سند جایگزینِ `docs/CARMILLA_REDESIGN_ROADMAP.md` نیست؛ آن سند تاریخچه‌ی کارِ انجام‌شده را دارد و
این سند نقشه‌ی کارِ باقی‌مانده را. هر دو کنارِ هم معتبرند._
