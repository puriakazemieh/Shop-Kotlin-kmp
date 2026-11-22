<div dir="rtl" align="right">

# مشخصات کاتالوگ قابلیت، SKU و تفکیک حق خرید (نسخه ۱)
## Carmilla Entitlement & Capability Catalog Specification v1

- **نسخه سند:** 1.0.0 (نسخه‌دار و قطعی)
- **شناسه Task:** `P04-ENTITLEMENT-DATA-039`
- **مرجع معماری:** [ADR-006](../architecture/adr/ADR-006-INDEPENDENT-PRODUCTS-AND-ENTITLEMENTS.md)، [ADR-038](../architecture/adr/ADR-038-PRODUCT-OWNERSHIP-MATRIX.md)، [ADR-007](../architecture/adr/ADR-007-SHARED-KERNEL-HOST-BOOTSTRAP.md) و [تعریف محصولات مستقل](../INDEPENDENT_PRODUCTS_SPEC_FA.md).
- **وضعیت:** تصویب‌شده و پیاده‌سازی‌شده در هسته مشترک (`carmilla-core`).

---

## ۱. اصول حاکم بر طراحی کاتالوگ و مجوزها

1. **تفکیک سه لایه مفهومی:**
   - **Artifact Inventory (کد موجود در بسته):** ماژول‌ها و فایل‌هایی که واقعاً درون فایل ZIP بسته‌بندی شده‌اند. اگر ماژولی در بسته نباشد، با هیچ کلید یا toggleای اجرا نخواهد شد.
   - **Entitlement / License (حق خرید معتبر):** مجوزی که مشخص می‌کند این سایت یا دامنه، چه قابلیت‌ها و چه اهداف اپ‌سازی را خریداری کرده است.
   - **Feature Toggle (انتخاب مدیر سایت):** مدیر سایت می‌تواند قابلیت مجاز و موجود را در پنل روشن یا خاموش کند. روشن‌کردن قابلیت خریداری‌نشده یا موجود نبوده ناممکن و مردود است.

2. **استقلال کامل دو محصول وردپرسی:**
   - پوسته Carmilla و افزونه Carmilla Bridge هر دو حامل هسته مشترک (`carmilla-core`) هستند.
   - بسته Base پوسته یا افزونه هیچ ماژول تخصصی (Vertical) ندارد.
   - بسته نوبت‌دهی (`clinic.booking`) کاملاً مستقل از تست روان‌شناسی (`psych.tests`) است و هیچ پیش‌نیازی میان این دو وجود ندارد.

3. **تفکیک کامل پلتفرم‌های اپ‌ساز (App Builder):**
   - چهار هدف مستقل: `android`، `ios`، `desktop`، `pwa` (و `web`).
   - تمام مقادیر غایب (missing) در پیکربندی به صورت پیش‌فرض `false` در نظر گرفته می‌شوند.
   - وجود `builder.core` برای فعال‌سازی هر کلاینت الزامی است، اما داشتن مجوز یک هدف (مثلاً Android)، هرگز مجوزی برای اهداف دیگر (مثلاً iOS) ایجاد نمی‌کند.

---

## ۲. ساختار شناسه‌های قابلیت (Canonical Features & Aliases)

### ۲.۱. قابلیت‌های دامنه (Domains & Canonical IDs)

| دامنه | شناسه رسمی (Canonical ID) | برچسب فارسی | نوع | وابستگی‌های پیش‌نیاز |
|---|---|---|---|---|
| پایه | `theme.base` | چیدمان و ظاهر پایه پوسته | Base | ندارد |
| پایه | `plugin.base` | آداپتور افزونه روی قالب ثالث | Base | ندارد |
| پایه | `content.core` | نوشته‌ها، برگه‌ها و رسانه | Base | ندارد |
| محتوا | `content.stories` | استوری‌ها و فیدهای تصویری | Base | `content.core` |
| محتوا | `content.banners` | بنرهای تبلیغاتی و کمپین | Base | `content.core` |
| محتوا | `content.support` | پشتیبانی، تیکت و پیام‌رسانی | Base | `content.core` |
| فروشگاه | `commerce.core` | هسته کاتالوگ کالا و قیمت | Vertical | WooCommerce |
| فروشگاه | `commerce.cart` | سبد خرید و تسویه حساب | Vertical | `commerce.core` |
| فروشگاه | `commerce.payment` | درگاه‌های پرداخت و سفارش | Vertical | `commerce.core`, `commerce.cart` |
| فروشگاه | `commerce.wallet` | کیف پول اعتباری مشتری | Vertical | `commerce.core`, `commerce.payment` |
| آموزش | `academy.core` | مدیریت دوره و اساتید | Vertical | ندارد |
| آموزش | `academy.courses` | سرفصل‌ها و دروس | Vertical | `academy.core` |
| آموزش | `academy.quizzes` | آزمون‌ها و ارزیابی دروس | Vertical | `academy.core`, `academy.courses` |
| آموزش | `academy.certificates`| صدور گواهی دوره | Vertical | `academy.core`, `academy.courses` |
| کلینیک | `clinic.booking` | زمان‌بندی و ثبت نوبت | Vertical | ندارد |
| کلینیک | `clinic.therapists` | معرفی درمانگران/متخصصان | Vertical | `clinic.booking` |
| کلینیک | `clinic.consultation` | جلسات و اتاق مشاوره آنلاین | Vertical | `clinic.booking` |
| کلینیک | `clinic.records` | پرونده محرمانه و یادداشت درمان | Vertical | `clinic.booking`, `clinic.consultation` |
| روان‌شناسی| `psych.tests` | کاتالوگ تست‌های روان‌شناسی | Vertical | ندارد |
| روان‌شناسی| `psych.results` | تحلیل نتایج و نمودار تست | Vertical | `psych.tests` |
| اپ‌ساز | `builder.core` | هماهنگ‌کننده ساخت اپ | Builder | ندارد |
| اپ‌ساز | `builder.target.android`| هدف ساخت نسخه اندروید | Builder | `builder.core` |
| اپ‌ساز | `builder.target.ios` | هدف ساخت نسخه iOS | Builder | `builder.core` |
| اپ‌ساز | `builder.target.desktop`| هدف ساخت نسخه دسکتاپ | Builder | `builder.core` |
| اپ‌ساز | `builder.target.pwa` | هدف ساخت نسخه PWA | Builder | `builder.core` |
| اپ‌ساز | `builder.target.web` | هدف ساخت نسخه مستقل وب | Builder | `builder.core` |

### ۲.۲. نگاشت سازگاری شناسه‌های قدیمی (Legacy Aliases)

برای اطمینان از backward compatibility، تمام شناسه‌های تاریخی به شناسه‌های کانونیکال نگاشت می‌شوند:

```json
{
  "builder.ui": "builder.core",
  "builder.config": "builder.core",
  "consultation.core": "clinic.consultation",
  "clinic.sessions": "clinic.consultation",
  "clinic.psych": "psych.tests",
  "clinic.private_records": "clinic.records",
  "builder.android": "builder.target.android",
  "builder.ios": "builder.target.ios",
  "builder.desktop": "builder.target.desktop",
  "builder.pwa": "builder.target.pwa",
  "builder.web": "builder.target.web"
}
```

---

## ۳. کاتالوگ SKUها و بسته‌های تجاری

1. **`sku_theme_base`:** پوسته سبک و پاک، فقط شامل قابلیت‌های پایه و بدون هیچ‌یک از verticalها (`builders: all false`).
2. **`sku_plugin_base`:** افزونه پایه برای اجرا روی هر قالب ثالث استاندارد بدون هیچ‌یک از verticalها (`builders: all false`).
3. **`sku_commerce`:** قابلیت کامل فروشگاهی مبتنی بر WooCommerce.
4. **`sku_academy`:** سیستم جامع آموزش آنلاین و LMS.
5. **`sku_clinic_booking`:** ثبت و زمان‌بندی نوبت‌ها و فهرست متخصصان؛ **بدون الزام به خرید تست روان‌شناسی**.
6. **`sku_clinic_consultation`:** اتاق‌های مشاوره تصویری/صوتی آنلاین و پرونده‌های محرمانه مراجعان.
7. **`sku_psych`:** ماژول آزمون‌های روان‌شناسی و صدور کارنامه؛ **مستقل از نوبت‌دهی**.
8. **`sku_builder_android` / `ios` / `desktop` / `pwa`:** بسته‌های اختصاصی ساخت هر پلتفرم.
9. **`theme-booking-android-pwa`:** بسته ترکیبی مثال قرارداد مالک محصول شامل پوسته + نوبت‌دهی + اپ اندروید + PWA.

---

## ۴. سیاست‌های چرخه عمر لایسنس (Lifecycle Policies)

### ۴.۱. وضعیت‌های مجاز (Statuses)
- `active`: لایسنس معتبر، فعال و متصل به دامنه.
- `grace_period`: لایسنس منقضی شده اما در دوره مهلت (پیش‌فرض ۱۴ روز). امکان خواندن/نوشتن حفظ می‌شود، اما اعلان مدیریتی در پنل ظاهر می‌گردد.
- `expired`: لایسنس پس از اتمام دوره مهلت منقضی شده است. عملیات جدید بسته می‌شود، اما اطلاعات سایت هرگز حذف نمی‌شود.
- `revoked`: لایسنس ابطال شده است. تمام عملیات تجاری متوقف می‌گردد، داده سایت دست‌نخورده باقی می‌ماند.
- `transferred`: لایسنس به دامنه جدید منتقل شده است؛ روی دامنه پیشین غیرفعال می‌شود.
- `suspended`: تعلیق موقت با حفظ تمام داده‌ها.

### ۴.۲. کارکرد آفلاین و بدون اینترنت
- لایسنس محلی امضاشده (`manifest.sig` یا فیلد `signature`) همراه فایل ZIP قرار می‌گیرد.
- احراز هویت محلی با کلید عمومی انجام می‌شود؛ هیچ پینگ آنلاین اجباری برای کارکرد عادی روزمره سایت لازم نیست.

### ۴.۳. ارتقا (Upgrade) و انتقال دامنه (Site Transfer)
- ارتقای بسته صرفاً از طریق Forward-fix و افزودن مجوزها صورت می‌گیرد؛ هیچ بازگشت به عقب ناسازگار (destructive downgrade) در schema صورت نمی‌پذیرد.
- انتقال دامنه نیازمند توکن انتقال (`transfer_token`) و ابطال دامنه قبلی است.

---

## ۵. نگاشت اجزای بسته (Artifact Inventory)

موتور ساخت (`carmilla assembler`) از روی جدول `artifactInventory` در فایل `carmilla-entitlement-catalog-v1.json` فایل‌های ZIP پوسته و افزونه را تولید می‌کند:
- فایل‌های PHP اختصاصی هر ماژول
- assetهای CSS/JS
- مسیرهای REST API مربوطه
- صفحات پنل مدیریت
- templateها و shortcodeهای نمایش عمومی
- پیش‌نیازهای محیطی (مانند WooCommerce)

</div>
