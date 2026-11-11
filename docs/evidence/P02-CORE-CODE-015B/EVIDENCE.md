# مستندات اثبات (Evidence) برای P02-CORE-CODE-015B

## اهداف و اقدامات
تسک P02-CORE-CODE-015B با هدف «حل Dependency Inversion‌های ماژول‌های Profile و Admin و حذف کامل وابستگی‌های متقاطع فیچرها» انجام شد:
1. **بررسی عدم وابستگی در فایل‌های ساخت (`build.gradle.kts`):**
   - ماژول‌های `:feature:profile` و تمامی زیرماژول‌های ادمین (`:feature:admin:products`، `:feature:admin:academy`، `:feature:admin:blog`، `:feature:admin:clinic`، `:feature:admin:options`، `:feature:admin:orders`، `:feature:admin:psychtest`، `:feature:admin:wallet`) مورد بررسی دقیق قرار گرفتند و تایید شد هیچ‌کدام دارای وابستگی مستقیم به سایر ماژول‌های Feature نیستند.
2. **بررسی عدم نشت در کدهای سورس کاتلین:**
   - تعداد ۳۳ فایل کاتلین در مسیرهای سورس ماژول‌های Profile و Admin اسکن شدند و تایید شد که صفر ایمپورت مستقیم از بسته‌های دیگر فیچرها وجود دارد.
3. **الگوی وارونگی وابستگی در واسط کاربری:**
   - صفحه `ProfileScreen` تمام مسیریابی‌ها را به صورت کال‌بک‌های لامبدا (`navigateToDetail`، `navigateToCourse`، `navigateToOrderDetail` و غیره) و محتواهای وابسته را به صورت Slot Composable دریافت می‌کند.
   - صفحه `AdminPanelScreen` نیز تمامی پنل‌های زیردامنه (مانند آکادمی، کلینیک، گزینه‌ها و آزمون‌های روان‌سنجی) را از طریق Slot API دریافت می‌کند و هیچ وابستگی مستقیمی ندارد.
   - گراف‌های `profileNavGraph` و `adminNavGraph` در ماژول `core:navigation` به عنوان Composition Root و متصل‌کننده عمل می‌کنند.
4. **تست‌های خودکار:**
   - آزمون‌های شخصیت‌سنجی معماری `FeatureDependencyInversionTest.kt` در `core:navigation:jvmTest` اضافه شدند و با موفقیت پاس شدند.
   - تسک گریدل `architectureCheck` اجرا شد و پاس شد.
   - اسکریپت جامع `tools/architecture/verify_feature_inversion.cjs` همه آزمون‌ها را با وضعیت سبز PASS کرد.

## وضعیت تسک
DONE
