# راهنمای وایت‌لیبل و چند‌فروشگاهی (Multi-Brand / Multi-Vertical)

این سند دو چیز را پوشش می‌دهد:
1. **وایت‌لیبل‌کردن** همین فروشگاه تا از یک کدبِیس، چند فروشگاه (کارمیلا، عطر، ساعت، …) با برندینگ/رنگ/دامنه‌ی متفاوت بسازیم و **برای هر پلتفرم خروجیِ جدا** بگیریم.
2. پاسخ به این سؤال که برای پروژه‌های **شبیه ولی متفاوت** (آموزشگاه، روانشناسی/نوبت‌دهی) بهتر است پروژه‌ی جدا بزنیم یا همین را توسعه دهیم — و راه‌حلِ ماژولار.

---

## بخش ۱ — وایت‌لیبل (چند برند از یک کدبِیس)

### زیرساختِ پیاده‌سازی‌شده
- **`core/designSystem/brand/Brand.kt`**: `BrandConfig` (اسم، پالت، currency، `apiBaseUrl`، `BrandFeatures`) + `BrandColors`/`BrandPalette` + رجیستریِ برندها.
  - سه برندِ نمونه: `CarmilaBrand` (پیش‌فرض)، `AtrisBrand` (عطر)، `ChronosBrand` (ساعت).
- **`AppTheme(brandColors = …)`**: تم از پالتِ برندِ فعال ساخته می‌شود (کارمیلا بدونِ تغییرِ بصری).
- **`ApiConfig.baseUrlOverride`** (در `core/network`): اگر برند `apiBaseUrl` داشته باشد، BASE_URL را override می‌کند.
- **`initKoin(brand)`**: `BrandConfig` را در Koin ثبت می‌کند و override آدرس را ست می‌کند؛ `App()` رنگِ برند را اعمال می‌کند.
- **اندروید — Product Flavors**: `carmila` / `atris` / `chronos` هر کدام `applicationId` و برچسبِ لانچرِ جدا + `BuildConfig.BRAND`. `ShopApplication` برند را از `BuildConfig.BRAND` انتخاب می‌کند.
- **دسکتاپ**: انتخابِ برند با `-Dbrand=<id>`.

### هفت مرحله‌ی وایت‌لیبل — وضعیت
| # | مرحله | وضعیت |
|---|-------|-------|
| ۱ | ماژول/لایه‌ی برند (`BrandConfig` + feature flags) | ✅ انجام شد (`core/designSystem/brand`) |
| ۲ | تمِ override‌پذیر per brand | ✅ انجام شد (`AppTheme(brandColors)`) |
| ۳ | اسکیمای محصولِ داده‌محور (attributes/optionها) | ✅ از قبل بود؛ صفحه‌ی محصول جنریک است |
| ۴ | externalize کردنِ BASE_URL و assets per brand | ✅ `ApiConfig` + `apiBaseUrl` برند |
| ۵ | بیلد فلِیور/خروجیِ per brand (همه‌ی پلتفرم‌ها) | 🟡 اندروید+دسکتاپ انجام شد؛ وب/iOS طبق زیر |
| ۶ | محتوا (دسته/استوری/بلاگ/بنر) per store | ✅ ادمین‌محور؛ فقط seedِ متفاوت per دیتابیس |
| ۷ | CI/CD با matrix روی برندها | ⬜ اسکریپت/پایپلاین طبق زیر (روی محیطِ شما) |

### چطور برندِ جدید اضافه کنم؟
1. در `Brand.kt` یک `BrandColors` و `BrandConfig` جدید بساز و به `BrandRegistry.all` اضافه کن.
2. (اختیاری) `apiBaseUrl` و `BrandFeatures` را ست کن (مثلاً `wallet = false`).
3. برای اندروید یک `productFlavor` جدید در `composeApp/build.gradle.kts` بساز (`applicationIdSuffix` + `buildConfigField BRAND`).
4. برچسبِ لانچر: یک `strings.xml` در `composeApp/src/<flavor>/res/values/` با `app_name`.
5. آیکن per brand: آیکن‌ها را در `composeApp/src/<flavor>/res/drawable*/` بگذار (هم‌نامِ آیکنِ main تا override شود).

### گرفتنِ خروجی per برند و per پلتفرم
هدف: پوشه‌ای مثل `dist/carmila/`, `dist/atris/` که خروجی‌های هر برند داخلش باشد.

**اندروید (APK/AAB جدا به‌ازای هر برند):**
```
./gradlew :composeApp:assembleCarmilaRelease
./gradlew :composeApp:assembleAtrisRelease
./gradlew :composeApp:assembleChronosRelease
# خروجی به‌صورتِ خودکار per flavor اینجاست:
#   composeApp/build/outputs/apk/carmila/release/
#   composeApp/build/outputs/apk/atris/release/
```
برای جمع‌کردن در `dist/`:
```
mkdir -p dist/carmila/android && cp composeApp/build/outputs/apk/carmila/release/*.apk dist/carmila/android/
mkdir -p dist/atris/android   && cp composeApp/build/outputs/apk/atris/release/*.apk   dist/atris/android/
```

**دسکتاپ (JVM):**
```
./gradlew :composeApp:run -Dbrand=atris                     # اجرا با برندِ عطر
./gradlew :composeApp:packageDistributionForCurrentOs        # بسته‌ی نصبی
# سپس خروجی build/compose/binaries/... را به dist/<brand>/desktop/ کپی کن
```
> برای بسته‌ی نصبیِ per-brand با اسم/آیکنِ متفاوت، می‌توان از یک Gradle property (`-Pbrand=`) برای تنظیمِ `packageName`/`mainClass args` استفاده کرد؛ ساده‌ترین راه اجرای run با `-Dbrand=` است.

**وب (JS/Wasm):**
```
./gradlew :composeApp:jsBrowserDistribution
# خروجی: composeApp/build/dist/js/productionExecutable/  →  cp به dist/<brand>/web/
```
> انتخابِ برندِ وب: در `webMain/main.kt` می‌توان برند را از یک متغیرِ سراسریِ JS یا از دامنه/کوئری خواند؛
> فعلاً پیش‌فرض `carmila` است. برای هر برند یک بیلدِ جدا با تنظیمِ آن متغیر بگیرید.

**iOS:**
- در Xcode برای هر برند یک **Scheme/Target** یا **xcconfig** بساز که یک متغیر (`BRAND`) ست کند و در `MainViewController` آن را به `initKoin(brand = BrandRegistry.byId(brand))` بده.
- خروجی‌ها (`.ipa`) را در `dist/<brand>/ios/` بگذار.

**ساختارِ پیشنهادیِ خروجی:**
```
dist/
  carmila/ { android/  desktop/  web/  ios/ }
  atris/   { android/  desktop/  web/  ios/ }
  chronos/ { ... }
```

### آیا «ماژولِ اپِ جدا per برند» بهتر است یا Flavor؟
- **Product Flavor** (پیاده‌شده) استانداردترین راه است: از یک ماژول، چند اپِ نصب‌شدنیِ جدا با `applicationId`/اسم/آیکن/برندِ متفاوت. کمترین نگه‌داری، بدونِ کدِ تکراری.
- **ماژولِ اپِ جدا per برند** فقط وقتی ارزش دارد که تیم‌ها/چرخه‌ی انتشارِ کاملاً جدا لازم باشد. در آن حالت یک ماژولِ نازکِ `apps/<brand>` بساز که فقط `BrandConfig` و entry point دارد و به همه‌ی `feature:*`/`core:*` وابسته است. (توصیه: تا وقتی لازم نشده، Flavor کافی است.)

---

## بخش ۲ — پروژه‌های شبیه ولی متفاوت (آموزشگاه، روانشناسی)

### تصمیم: پروژه‌ی جدا یا توسعه‌ی همین؟
قانونِ سرانگشتی:
- **فقط برند/محتوا فرق دارد** → همان وایت‌لیبل (Flavor). *(بخش ۱)*
- **۲۰–۴۰٪ فیچرِ جدید ولی هسته مشترک است** (آموزشگاه، روانشناسی) → **همین مونوریپو را به «پلتفرمِ ماژولار» توسعه بده** (توصیه‌شده). پروژه‌ی جدا نزن.
- **> ۶۰٪ متفاوت یا دامنه‌ی کاملاً بی‌ربط** → پروژه‌ی جدا، ولی همچنان `core:*` (identity/cart/order/payment/wallet) را به‌عنوان کتابخانه share کن.

**چرا برای آموزشگاه/روانشناسی «توسعه‌ی همین» بهتر است؟**
حدودِ ۷۰٪ کار مشترک است: احراز هویت، سبد، سفارش، پرداخت (زرین‌پال)، کیف‌پول، آدرس، تخفیف، بلاگ، پشتیبانی، پنل ادمین. تفاوت‌ها فقط چند ماژولِ دامنه‌ایِ جدید است.

### نگاشتِ دامنه
| مفهومِ فروشگاه | آموزشگاه | روانشناسی |
|---|---|---|
| محصول | دوره | خدمت/جلسه‌ی مشاوره |
| خرید/سفارش | ثبت‌نام در دوره | رزروِ نوبت |
| صفحه‌ی محصول | صفحه‌ی دوره | صفحه‌ی مشاور/خدمت |
| — (جدید) | پیشرفتِ دوره + پخشِ ویدیو | تقویمِ نوبت‌دهی + ویدیوکال |

### راه‌حلِ ماژولار — همان «۷ مرحله» با این رویکرد
1. **هسته را به‌عنوان کتابخانه‌ی مشترک تثبیت کن.** الان هم `core:*` و بیشترِ `feature:*` قابلِ استفاده‌ی مجدد هستند. کاری لازم نیست جز اینکه وابستگی‌ها را «هسته → عمودی» نگه‌داری (عمودی به هسته وابسته باشد، نه برعکس).
2. **بالای `BrandConfig` یک مفهومِ `Vertical` اضافه کن.** مثلاً `enum Vertical { SHOP, ACADEMY, THERAPY }` در `BrandConfig` + `BrandFeatures` که مشخص می‌کند کدام nav/ماژول‌ها فعال‌اند. (BrandConfig از قبل ساخته شده؛ فقط یک فیلد اضافه می‌شود.)
3. **مدلِ محصول را عمومی کن:** یک `ProductType { PHYSICAL, DIGITAL, COURSE, SERVICE }` روی محصول (سرور: یک ستون؛ کلاینت: یک فیلد). این باعث می‌شود **سبد/سفارش/پرداخت بدونِ تغییر** برای دوره و نوبت هم کار کنند.
4. **ماژول‌های فیچرِ عمودیِ جدید بساز** (کنارِ ماژول‌های فعلی، وابسته به هسته):
   - آموزشگاه: `feature:courses` (دوره‌های من)، `feature:learning` (پخشِ درس/ویدیو + `LessonProgress`).
   - روانشناسی: `feature:appointments` (تقویم + رزرو)، `feature:videocall` (ویدیوکال با WebRTC یا سرویسِ ثالث مثل Agora/Twilio/LiveKit).
5. **NavGraph را ترکیب‌پذیر کن:** هر عمودی روت‌های خودش را ثبت کند و بر اساسِ `Vertical`ِ برندِ فعال به `AppNavHost` اضافه شود (الگوی فعلیِ `composable<...>` حفظ می‌شود).
6. **per محصول یک خروجی بگیر:** دقیقاً مثلِ بخش ۱ (Flavor/ماژولِ اپ). مثلاً فلِیورهای `carmila` (SHOP)، `academy` (ACADEMY)، `therapy` (THERAPY) — هر کدام `BrandConfig` با `vertical` و ماژول‌های لازم.
7. **سرور:** همان معماریِ ماژولارِ فعلی (هر دامنه یک پکیج). ماژول‌های دامنه‌ایِ جدید کنارِ مشترک‌ها اضافه کن:
   - آموزشگاه: `course` (Course/Section/Lesson)، `enrollment` (Enrollment/LessonProgress)، آپلود/استریمِ ویدیو.
   - روانشناسی: `service`, `availability` (Slotها)، `appointment`, `session` (لینکِ ویدیوکال). پرداخت/کیف‌پول/سفارش همان.
   - در صورتِ سرویس‌دهی به چند مشتری از یک سرور، `tenant_id` به entityها اضافه کن (چندمستأجری).

### چه چیزی را share و چه چیزی را جدا نگه‌داریم؟
- **share (بدونِ تغییر):** identity/auth، cart، order، payment، wallet، address، discount، admin، blog، support، designSystem/brand، network، navigation host.
- **عمودی‌سازی (ماژولِ جدید):** courses/learning یا appointments/videocall + مدل‌های دامنه‌ای‌شان.
- **پیکربندی (نه کد):** برند، رنگ، محتوا، feature flags، `Vertical`.

> جمع‌بندی: **پروژه‌ی جدا نزن**؛ همین مونوریپو را با ماژول‌های عمودی رشد بده. هسته یک‌بار نگه‌داری می‌شود و هر عمودی فقط تفاوت‌های خودش را می‌آورد. اگر روزی یک عمودی خیلی بزرگ/مستقل شد، همان ماژول‌ها را به یک ریپوی جدا منتقل کن (چون از قبل ماژولار است، هزینه‌اش کم است).

### عمودیِ آموزشگاه — پیاده‌سازی‌شده ✅ (نمونه‌ی عملی)
به‌عنوانِ نمونه، عمودیِ آموزشگاه واقعاً پیاده شد:
- **سرور** (`com.kazemieh.shop.academy`): `Course/Section/Lesson/Enrollment/LessonProgress` + APIها:
  - عمومی: `GET /api/courses`، `GET /api/courses/{slug}` (ویدیوها فقط برای کاربرِ ثبت‌نام‌شده/درسِ پیش‌نمایش).
  - کاربر: `GET /api/academy/my-courses`، `POST /api/academy/courses/{id}/enroll`، `GET /api/academy/courses/{id}/progress`، `POST /api/academy/lessons/{id}/progress`.
  - ادمین: `/api/admin/courses` (CRUD + section/lesson). migration `022_add_academy.sql`.
- **کلاینت** (`:feature:academy`): صفحاتِ «دوره‌ها»، «دوره‌های من»، «جزئیات دوره» (ثبت‌نام + نوارِ پیشرفت) و «یادگیری» (پخشِ ویدیو با بازاستفاده از `VideoPlayer` ماژولِ details + علامت‌زدنِ تکمیل + درصد پیشرفت). ورودی‌ها در صفحه‌ی «مشخصات»: «دوره‌های آموزشی» (کاتالوگ، برای همه) و «دوره‌های من» (کاربرِ لاگین). حالتِ خالیِ «دوره‌های من» هم دکمه‌ی «مشاهده‌ی دوره‌ها» دارد.
- **خرید → ثبت‌نامِ خودکار (پیاده‌شد ✅):** با پرداختِ سفارش (رفتنِ سفارش به وضعیتِ `PROCESSING` — چه از درگاه، چه کیف‌پولِ کامل)، `OrderService` سرویسِ `CourseAccessService.grantAccessForProducts` را صدا می‌زند و کاربر را به‌طورِ خودکار در دوره‌هایی که به آن محصولات لینک شده‌اند (`Course.productId`) ثبت‌نام می‌کند (idempotent). فلوی «خرید کن → تماشا کن → پیشرفت ببین» کامل است.
- **داده‌ی نمونه:** از پنل ادمین (`POST /api/admin/courses` → افزودن section و lesson) یک دوره بساز؛ برای فروش، `productId` را به یک محصولِ فروشگاه لینک کن.

### عمودیِ مشاوره/روان‌شناسی (نوبت‌دهی) — پیاده‌سازی‌شده ✅
عمودیِ دومِ نمونه، کلینیکِ مشاوره است (رزرو نوبت + تماسِ تصویری):
- **سرور** (`com.kazemieh.shop.clinic`): `Therapist/AvailabilitySlot/Appointment` (+`AppointmentStatus`) + APIها:
  - عمومی: `GET /api/therapists`، `GET /api/therapists/{slug}` (به‌همراهِ بازه‌های آزادِ آینده با برچسبِ آماده‌ی روز/ساعت).
  - کاربر: `GET /api/clinic/my-appointments`، `POST /api/clinic/appointments` (رزرو با قفلِ ردیفِ اتمیک روی بازه)، `POST /api/clinic/appointments/{id}/cancel`.
  - ادمین: `/api/admin/therapists` (CRUD)، `POST .../{id}/slots` (افزودنِ بازه)، `POST .../appointments/{id}/confirm` (تأیید + ثبتِ لینکِ اتاقِ تماس)، `.../complete`. migration `023_add_clinic.sql`.
- **کلاینت** (`:feature:clinic`): «مشاوره و روان‌شناسی» (لیستِ درمانگرها)، «جزئیات درمانگر» (بایو + انتخابِ بازه + رزرو)، «نوبت‌های من» (بَجِ وضعیتِ فارسی + «ورود به جلسه» با بازکردنِ لینکِ اتاق از طریقِ `LocalUriHandler` + «لغو نوبت»). ورودی‌ها در «مشخصات»: «مشاوره و روان‌شناسی» (برای همه) و «نوبت‌های من» (کاربرِ لاگین).
- **تماسِ تصویری:** لینکِ اتاق (مثلاً Jitsi/Google Meet) توسطِ ادمین هنگامِ تأیید ثبت می‌شود و کاربر با دکمه‌ی «ورود به جلسه» آن را در مرورگر/اپِ تماس باز می‌کند. برای WebRTCِ درون‌اپ، همین `videoRoomUrl` نقطه‌ی اتصال است.
- **نکته‌ی محصولِ واقعی:** رزرو فعلاً مستقیم است؛ مثلِ آموزشگاه می‌توان با لینک‌کردنِ `Therapist.productId` و قلاب در `OrderService`، رزرو/تأیید را به پرداخت گره زد.

---

## تأیید (روی دستگاهِ شما)
محیطِ ابری قادر به build نیست. لطفاً بعد از pull:
- `gradlew.bat :composeApp:assembleCarmilaDebug` و `...:assembleAtrisDebug` را بزنید تا هر دو اپ ساخته شوند.
- دسکتاپ: `gradlew.bat :composeApp:run -Dbrand=atris` تا رنگ/برندِ عطر را ببینید.
- اگر خطای کامپایلی بود، متن خطا را بفرستید تا اصلاح کنم.
