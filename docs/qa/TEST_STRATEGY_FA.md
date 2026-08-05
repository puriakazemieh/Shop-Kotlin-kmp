# استراتژی تست پروژه Carmilla

> نسخه سند: ۱.۰  
> تاریخ: ۵ اوت ۲۰۲۶  
> وضعیت: پیش‌نویس برای تصویب (Task P00-QA-DISC-007)

## ۱. هدف و لایه‌های تست
هدف این استراتژی تضمین صحت عملکرد هسته فروشگاهی و امنیت داده‌ها در پلتفرم‌های مختلف است.

| لایه تست | مسئول | زمان اجرا | ابزار |
|---|---|---|---|
| **Unit Test** | AI/Developer | در هر Commit/PR | JUnit, Kotlin Test |
| **Integration** | AI/Developer | پیش از Release | Ktor Mock, Testcontainers |
| **Manual QA** | Human | گیت‌های فازی (Phase Gates) | Real Devices, Browser |
| **E2E/Smoke** | BOTH | پس از Deploy | Playwright (Web), Emulator (Android) |

---

## ۲. سطوح شدت اشکال (Severity Levels)

| شدت | تعریف | معیار خروج (Exit Criteria) |
|---|---|---|
| **Blocker (Sev0)** | توقف کامل جریان درآمدی، نشت داده یا عدم نصب اپ. | انتشار ممنوع. باید فوراً رفع شود. |
| **Critical (Sev1)** | خرابی در فیچرهای اصلی (مانند خرید یا لاگین) بدون Workaround. | انتشار ممنوع (مگر با تأیید ریسک مکتوب). |
| **Major (Sev2)** | اشکال در فیچر مهم که Workaround دشوار دارد. | حداقل ۹۵٪ باید قبل از GA رفع شوند. |
| **Minor (Sev3)** | اشکالات ظاهری یا فیچرهای کم‌اولویت. | در Backlog برای نسخه‌های بعدی. |

---

## ۳. قالب استاندارد تست‌کیس (MTC Template)
مسیر ذخیره‌سازی: `docs/qa/MANUAL_SUITES/`

```markdown
# MTC-{AREA}-{NUMBER} — [عنوان تست]

- **Related Task**: Pxx-xxxx
- **Risk/Priority**: HIGH/P0
- **Tester**: [Name]
- **Environment**: [Device/OS/Browser]

## مراحل (Steps)
1. ...
2. ...

## نتیجه مورد انتظار (Expected Result)
- [ ] ...

## نتیجه واقعی و شواهد
- **Result**: PASS/FAIL
- **Evidence**: [Link to screenshot/log]
```

---

## ۴. ردیابی (Traceability)
هر تسک پیاده‌سازی (`CODE`) باید به حداقل یک تست اتوماتیک یا یک تست‌کیس دستی متصل باشد. تیک زدن `DONE` در مانیفست اصلی بدون رفرنس به Evidence تست مجاز نیست.

---

## ۵. مجموعه تست Smoke (فروشگاه - Shop-only)
این تست‌ها باید در هر بار استقرار (Deploy) در محیط Staging اجرا شوند.

### ۵.۱ بخش هویت (Identity)
- [ ] ورود با رمز عبور (HAPPY PATH)
- [ ] دریافت کد OTP و ورود موفق
- [ ] خروج از حساب کاربری (Logout)

### ۵.۲ بخش کاتالوگ و محصول
- [ ] مشاهده لیست دسته‌بندی‌ها
- [ ] جستجوی محصول و فیلتر کردن
- [ ] مشاهده جزئیات محصول (نام، قیمت، عکس)

### ۵.۳ بخش خرید (Commerce)
- [ ] افزودن محصول به سبد خرید
- [ ] اعمال کد تخفیف معتبر
- [ ] طی کردن مراحل Checkout تا درگاه پرداخت

### ۵.۴ بخش پرداخت و سفارش
- [ ] بازگشت از درگاه (شبیه‌سازی Success/Fail)
- [ ] مشاهده سفارش ثبت شده در "سفارشات من"
- [ ] تغییر وضعیت سفارش در پنل ادمین و مشاهده آن در اپ

---

## ۶. محیط‌های مرجع (Reference Environments)
- **Staging Web**: `http://localhost:8080` (Docker WP)
- **Android Emulator**: API 33+
- **API Mocker**: برای تست‌های لایه شبکه در نبود اینترنت یا بک‌اِند واقعی.
