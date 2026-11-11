# مستندات اثبات (Evidence) برای P02-ARCH-CODE-014

## اهداف و اقدامات
تسک P02-ARCH-CODE-014 بر اساس نیازمندی «تقسیم تدریجی گراف Navigation به Graphهای Feature محور» انجام شد. فایل غول‌پیکر `AppNavigation.kt` به فایل‌های تفکیک‌شده بر اساس دامنه‌ی کاری (Feature) تقسیم شده است:
1. `AuthNavigation.kt` (`authNavGraph`: مسیرهای احراز هویت، ورود، ثبت‌نام و بازیابی رمز عبور)
2. `AdminNavigation.kt` (`adminNavGraph`: مسیرهای ادمین، مدیریت محصولات، سفارشات، آپشن‌ها، تخفیف‌ها و بلاگ)
3. `AcademyNavigation.kt` (`academyNavGraph`: مسیرهای آکادمی، آزمون‌ها، دوره‌ها و گواهی‌ها)
4. `ClinicNavigation.kt` (`clinicNavGraph`: کلینیک روانشناسی، لیست درمانگران و رزرو نوبت)
5. `PsychTestNavigation.kt` (`psychTestNavGraph`: آزمون‌های روان‌سنجی)
6. `OrdersNavigation.kt` (`ordersNavGraph`: مدیریت سفارشات، سبد خرید و پرداخت)
7. `ProfileNavigation.kt` (`profileNavGraph`: پروفایل کاربری، تنظیمات، آدرس‌ها و کیف پول)
8. `CatalogNavigation.kt` (`catalogNavGraph`: کاتالوگ محصولات، دسته‌بندی‌ها، جستجو و مقایسه)

فایل `AppNavigation.kt` اکنون فقط شامل فراخوانی توابع `builder` هر گراف است (`adminNavGraph`، `academyNavGraph`، `authNavGraph` و غیره) و مسیر اصلی `HomeGraph` در آن باقی مانده است تا دیپ‌لینک‌ها و Characterizationهای Navigation دست‌نخورده باقی بماند.

## آزمون‌های Characterization و Deep-Link
مجموعه آزمون خودکار `NavigationCharacterizationTest.kt` در ماژول `core:navigation` اضافه شد که موارد زیر را اعتبارسنجی می‌کند:
- ثبات شناسه مسیرها (Route Identity Contracts) برای صفحات اصلی شاپ، ورود، ادمین و ماژول‌های عمودی.
- فیلترینگ و گارد ماژول‌ها توسط `FeatureRouteGuard` و انطباق با `ResolvedFeatures`.
- تبدیل و بازنمایی آدرس‌های Deep Link (`/product/{slug}`، `/reset-password?token={token}`، `/search?id={id}&name={name}`، `/checkout?amount={amount}`).
- تفکیک و استقلال کامل گراف‌های ۸‌گانه در لایه Navigation.

## وضعیت بیلد و اعتبارسنجی
- اسکریپت تایید خودکار `tools/navigation/verify_navigation_graphs.cjs` با موفقیت اجرا شد:
  - `Feature Graph Partitioning`: PASS
  - `AppNavigation Host Delegation`: PASS
  - `Navigation Characterization Test Suite` (`:core:navigation:jvmTest`): PASS
  - `Baseline KMP Compilation Check` (`:composeApp:compileKotlinJvm`, `:composeApp:compileKotlinJs`): PASS
- گزارش کامل در `docs/evidence/P02-ARCH-CODE-014/summary.md` ثبت شده است.

## وضعیت تسک
DONE
