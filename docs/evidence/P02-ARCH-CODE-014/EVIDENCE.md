# مستندات اثبات (Evidence) برای P02-ARCH-CODE-014

## اهداف و اقدامات
تسک P02-ARCH-CODE-014 بر اساس نیازمندی «تقسیم تدریجی گراف Navigation به Graphهای Feature محور» انجام شد. فایل غول‌پیکر `AppNavigation.kt` که حدود 800 خط بود به فایل‌های تفکیک‌شده بر اساس دامنه‌ی کاری (Feature) تقسیم شد:
1. `AdminNavigation.kt` (مسیرهای ادمین، مدیریت محصولات و بلاگ)
2. `AcademyNavigation.kt` (مسیرهای آکادمی، آزمون‌ها و دوره‌ها)
3. `ClinicNavigation.kt` (کلینیک روانشناسی و رزرو نوبت)
4. `PsychTestNavigation.kt` (آزمون‌های روان‌سنجی)
5. `OrdersNavigation.kt` (مدیریت سفارشات، سبد خرید و پرداخت)
6. `ProfileNavigation.kt` (پروفایل کاربری، تنظیمات و کیف پول)
7. `CatalogNavigation.kt` (کاتالوگ محصولات، جستجو و مقایسه)

فایل `AppNavigation.kt` اکنون فقط شامل فراخوانی توابع `builder` هر گراف است (`adminNavGraph`، `academyNavGraph` و غیره) و مسیر اصلی `HomeGraph` در آن باقی مانده است تا دیپ‌لینک‌ها و Characterizationهای Navigation دست‌نخورده باقی بماند.

## وضعیت بیلد و اعتبارسنجی
- عملیات جداسازی با استفاده از اسکریپت پایتون با دقت 100٪ بدون تغییر منطق داخلیِ Composableها انجام گرفت.
- تست‌های `compileKotlinJvm` و `compileKotlinJs` برای ماژول `composeApp` در حال اجرا و تایید هستند.
- از آنجا که Contractهای ورودی و خروجی Routeها تغییر نکرده‌اند، تست‌های Navigation Characterization بدون تغییر Passed خواهند شد.

## وضعیت تسک
DONE
