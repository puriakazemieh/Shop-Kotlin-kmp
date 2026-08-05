# محیط‌های مرجع تست (Reference Environments)

> نسخه سند: ۱.۰  
> تاریخ: ۵ اوت ۲۰۲۶  
> وضعیت: پیش‌نویس (Task P00-QA-OPS-010)

این مستند پیکربندی دقیق محیط‌های مرجع برای تست‌های دستی و خودکار پروژه Carmilla را تعریف می‌کند.

---

## ۱. سطوح قابلیت (Feature Presets)

| شناسه | نام محیط | قابلیت‌های فعال (Manifest) | کاربرد |
|---|---|---|---|
| **F0** | Minimal | `content.blog`, `auth` | تست پایه اتصال و محتوای ایستا |
| **F1** | Shop | `F0` + `commerce.core`, `commerce.physical` | تست هسته فروشگاهی (هدف نسخه ۱.۰) |
| **F2** | Academy | `F1` + `academy.*` | تست سیستم آموزشی و فروش دوره |
| **F3** | Clinic | `F1` + `clinic.*`, `psych.*` | تست رزرو مشاوره و داده‌های سلامت |
| **F4** | All-in-One | تمام قابلیت‌ها | تست Regression سراسری و تداخل فیچرها |

---

## ۲. ماتریس نرم‌افزاری (Software Stack)

### ۲.۱ زیرساخت وردپرس (WordPress Path)
- **OS**: Linux (Alpine/Debian via Docker)
- **PHP**: `8.1` (Minimum: 7.4)
- **WordPress**: `6.4` یا بالاتر
- **WooCommerce**: `8.5` یا بالاتر (با فعال بودن HPOS)
- **Database**: MariaDB `10.6`

### ۲.۲ زیرساخت اسپرینگ (Spring Path)
- **Java**: `21` (Temurin/OpenJDK)
- **Database**: PostgreSQL `15`
- **Container**: Docker `24+`

---

## ۳. ماتریس دستگاه و مرورگر (Device & Browser)

### ۳.۱ اندروید (Android)
- **Reference 1 (Min)**: Android 7.0 (API 24) - ۵ اینچ
- **Reference 2 (Target)**: Android 14 (API 34) یا Android 15 (API 36) - ۶ اینچ
- **Reference 3 (Tablet)**: Android 12L یا بالاتر

### ۳.۲ وب و PWA (Web)
- **Chrome**: آخرین نسخه Stable (مرجع اصلی)
- **Firefox/Edge**: تست سازگاری UI
- **Safari (iOS)**: تست رفتار PWA در آیفون

---

## ۴. روش بازنشانی محیط (Environment Reset)

برای اطمینان از صحت تست‌ها، محیط باید طبق روش‌های زیر به حالت "پاک" (Clean State) بازگردد:

1. **WordPress**: استفاده از دستور `docker-compose down -v` و اجرای مجدد.
2. **KMP Client**: پاک کردن Cache اپلیکیشن (Clear Data) و حذف Local Storage در مرورگر.
3. **Database**: درون‌ریزی مجدد اسکریپت‌های SQL مرجع در `db/init`.

---

## ۵. محدودیت‌های شناخته شده
- تست روی سیستم‌عامل **iOS** در فاز صفر به دلیل نبود محیط macOS پایدار در بیس‌لاین، به عنوان **DEFERRED** علامت‌گذاری شده است.
- تست نسخه **Desktop** فعلاً محدود به ویندوز ۱۰/۱۱ است.
