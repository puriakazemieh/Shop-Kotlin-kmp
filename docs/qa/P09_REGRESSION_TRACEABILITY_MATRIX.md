<div dir="rtl" align="right">

# ماتریس رگرسیون و ردیابی محصولات، SKU، میزبان و Backend — P09

تاریخ: ۶ سپتامبر ۲۰۲۶ | مرجع: [INDEPENDENT_PRODUCTS_SPEC_FA.md](../INDEPENDENT_PRODUCTS_SPEC_FA.md)، [ADR-006](../architecture/adr/ADR-006-INDEPENDENT-PRODUCTS-AND-ENTITLEMENTS.md)

## ۱. استراتژی آزمون و محورهای ماتریس (Test Axes)

### محور ۱: حالت‌های میزبان (Host Modes)
1. **پوسته تنها (Theme Standalone):** اجرای کامل بدون نیاز به Carmilla Bridge؛ مدیریت UI، CPT، REST API و App Builder از پوسته.
2. **افزونه تنها (Plugin Standalone / Carmilla Bridge):** اجرا روی قالب‌های استاندارد ثالث؛ ارائه فرم‌ها، shortcode/blocks، REST API و App Builder.
3. **نصب هم‌زمان (Concurrent Theme + Plugin):** هماهنگ‌کننده نسخه، بیلد واحد، پیشگیری از double registration/cron/CPT/migration.

### محور ۲: بسته‌های محصولات و قابلیت‌ها (Base / Feature / Bundle Matrix)
- **Base UI:** پوسته/افزونه پایه بدون دامنه پولی.
- **Commerce (فروشگاه):** کاتالوگ، تسویه‌حساب WooCommerce، درگاه‌های پرداخت، کیف پول.
- **Academy (آموزش):** دوره‌ها، دروس، ثبت‌نام، آزمون و گواهی.
- **Clinic & Consultation (نوبت‌دهی و مشاوره):** رزرو وقت، متخصصین، جلسات مشاوره و پرونده خصوصی.
- **Psychological Tests (تست روان‌شناسی):** اجرای تست‌ها، نمره‌دهی و گزارش، مجزا از خرید صرف نوبت‌دهی.
- **App Builder (اپ‌ساز):** درخواست ساخت کلاینت‌های Android/iOS/Desktop/PWA از وردپرس.

### محور ۳: لایه‌های کنترل و Enforcements (Entitlement / Toggle / Deny)
1. **ZIP Manifest (زمان ساخت):** حذف فیزیکی کد ماژول غیرمجاز از بسته تحویلی.
2. **Site Entitlement (زمان خرید/لایسنس):** مجوز خرید فعال دامنه‌ها و پلتفرم‌های کلاینت.
3. **Manager Toggle (زمان اجرا):** روشن/خاموش کردن قابلیت توسط مدیر سایت بدون نیاز به Rebuild.
4. **Deny Path & Fallback:** هدایت امن مسیرهای خاموش/غیرمجاز به صفحه خطای امن (بدون افشای اطلاعات یا کرش).

### محور ۴: کلاینت‌های مستقل و Backend Profiles
- **Backend Profile `WORDPRESS`:** KMP کلاینت از REST API مشترک وردپرس استفاده می‌کند.
- **Backend Profile `SPRING`:** KMP کلاینت مستقیماً به سرور Spring Boot متصل می‌شود (بدون نیاز به وردپرس).

---

## ۲. جدول ردیابی مسیرها (UI → API → Data → Deny Path)

| قابلیت (Feature) | مسیر UI | API Contract | ذخیره‌سازی داده | مسیر Deny / Access Control |
|---|---|---|---|---|
| **Commerce Core** | `/cart`, `/checkout` | `POST /wp-json/carmilla/v1/cart/add`<br>`POST /wp-json/carmilla/v1/checkout` | WooCommerce Orders / DB Tables | `403 Entitlement Required` if commerce feature false in manifest |
| **Academy LMS** | `/courses`, `/lesson/{id}` | `GET /wp-json/carmilla/v1/courses`<br>`POST /wp-json/carmilla/v1/enroll` | `carmilla_courses`, `carmilla_enrollments` | Redirect to `/upgrade` if LMS not purchased/enabled |
| **Clinic Booking** | `/booking`, `/providers` | `GET /wp-json/carmilla/v1/booking/slots`<br>`POST /wp-json/carmilla/v1/booking/reserve` | `carmilla_appointments` | `403 Feature Disabled` if `clinic.booking` toggle off |
| **Consultation** | `/consultations/{id}` | `GET /wp-json/carmilla/v1/consultations` | Private Encrypted DB Records | Strict Privacy Check + `401/403` Deny |
| **Psych Tests** | `/psych-tests/{id}` | `POST /wp-json/carmilla/v1/psych-tests/submit` | `carmilla_psych_results` | Entitlement Check independent of Booking |
| **App Builder** | `/admin/app-builder` | `POST /wp-json/carmilla/v1/builder/request` | `carmilla_build_jobs` | `403 Target Platform Not Licensed` |

---

## ۳. نگاشت Gateها به Artifactها و نسخه‌ها

| Gate ID | پلتفرم / ماژول | Artifact مورد سنجش | شرط عبور |
|---|---|---|---|
| `P04-WORDPRESS-GATE-037` | WP Theme & Plugin | `carmilla-theme-v*.zip`, `carmilla-bridge-v*.zip` | تست مستقل و هم‌زمان PASS |
| `P05-PAYMENT-GATE-024` | Payment Engine | Core Payment Module | تست درگاه‌ها، BNPL و Verify بی‌نقص |
| `P06-MESSAGE-GATE-016` | Messaging | SMS / Email Adapter | ارسال OTP و ناتیفیکیشن بدون نشت Secret |
| `P07-SEED-GATE-026` | Import / Seed | Seed Pack & Data Exporter | Preflight, dry-run و migration بدون تلفات داده |
| `P08-PWA-GATE-019` | Web / PWA Client | PWA Production Bundle | PWA Audit 100%, offline cache & service worker PASS |
| `P09-QA-GATE-018` | Release Regression | Release Candidate All Targets | ماتریس رگرسیون P09 کاملاً PASS |

</div>
