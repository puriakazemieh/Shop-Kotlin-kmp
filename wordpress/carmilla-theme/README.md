# Carmilla (WordPress theme)

پوسته‌ی فارسی/RTL که دیزاین اپ Compose «کارمیلا» را **بازسازی** می‌کند (نه تبدیل کد؛ چون UI اپ روی canvas رندر می‌شود و HTML معنایی ندارد). توکن‌ها **یک‌به‌یک** از `core/designSystem` پورت شده‌اند.

این **مسیر A** از پلن دو-مسیره است و کاملاً مستقل از کد کاتلین و از پلاگین `carmilla-bridge` کار می‌کند.

## وضعیت فعلی (0.1.0) — پایه‌ی دیزاین
- `assets/css/tokens.css` — کل توکن‌ها به CSS variables: رنگ (روشن/تیره از `Colors.kt`)، شعاع (`Shape.kt`)، فاصله/سایه (`Dimens.kt`)، بریک‌پوینت‌های ریسپانسیو (۶۰۰/۸۴۰ از `WindowSize.kt`).
- `assets/css/base.css` — RTL، مقیاس تایپوگرافی (`Typography.kt`)، کانتینر کپ‌وسط (`responsiveMaxWidth`)، گرید تطبیقی ۲/۳/۴ ستون (`adaptiveGridColumns`)، پوسته‌ی نوار پایین↔نوار کناری، و کامپوننت‌های پایه: `btn`, `badge`, `chip`, `story-ring`, `card`, `product-card`.
- `theme.json` — پالت و مقیاس فونت/فاصله برای ادیتور بلاک وردپرس (+ سازگاری با White-Label).
- `style.css`, `functions.php`, `header.php`, `footer.php`, `index.php` — بوت‌استرپ پوسته + enqueue + پشتیبانی WooCommerce/RTL.

## حالت تاریک
با `data-theme="dark"` روی `<html>` (تاگل کاربر) یا هر کانتینر داخلی فعال می‌شود؛ در نبود تاگل، از `prefers-color-scheme` سیستم پیروی می‌کند.

## فونت
خانواده Vazirmatn است. فایل `assets/fonts/Vazirmatn[wght].woff2` (variable) را کنار پوسته بگذارید؛ در نبود آن به Tahoma/سیستم fallback می‌شود.

## پیش‌نمایش دیزاین (dev)
`preview.html` یک صفحه‌ی نمایش توکن‌هاست (خارج از runtime پوسته). برای دیدن، در مرورگر بازش کنید یا اسکرین‌شات بگیرید.

## قدم بعدی (فاز ۲ مسیر A)
سلسله‌مراتب قالب: `front-page.php` (خانه)، override‌های WooCommerce، `single-product.php`، قالب‌های بلاگ، و قالب‌های CPT عمودی‌ها (`single-cb_course.php`, `single-cb_therapist.php`, تست) — مطابق `docs/RESPONSIVE_COVERAGE.md` و `docs/DESIGN_IMPLEMENTATION_PLAN.md` (برنچ eyraci).
