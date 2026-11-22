# P02-CORE-GATE-018 — خلاصه شواهد ارزیابی و عبور از گیت Foundation (فاز ۰۲)

**تاریخ:** ۲۲ نوامبر ۲۰۲۶  
**وضعیت گیت:** `PASS` (تمام شواهد و پیش‌نیازهای فاز ۰۲ به صورت قطعی تکمیل و تأیید شدند؛ کنترل‌های مستقل انتشار کاندید در `P09-QA-GATE-018` پیگیری می‌شوند)  
**مرجع قرارداد:** `docs/INDEPENDENT_PRODUCTS_SPEC_FA.md` و `docs/evidence/P02-STATUS-REVIEW-20261115/summary.md`

---

## ۱. هدف گیت فاز ۰۲ (P02 Foundation Gate)

تضمین اینکه زیرساخت مشترک (Foundation)، مرزهای ماژولار KMP، تفکیک گراف ناوبری، سازوکار تست‌های داکر وردپرس/آپاچی، تفکیک آرتیفکت‌های انتشار، نصب و ارتقای بدون رگرسیون اپلیکیشن اندروید، سوئیت آداپتورهای واقعی شبکه و داده، و راهبرد عقب‌گرد (Rollback/Forward-fix) به طور کامل مستقر، قابل بازتولید و سبز هستند تا پروژه بتواند با اطمینان کامل به فازهای ۳ و ۴ ورود کند.

---

## ۲. بررسی وضعیت پیش‌نیازهای بازگشایی‌شده در ممیزی ۱۵ نوامبر ۲۰۲۶

چهار تسک اصلی که در ممیزی ۱۵ نوامبر به عنوان `TODO` ثبت شده بودند، همگی با اجرای زنده و شواهد بازتولیدپذیر بسته شدند:

| شناسه تسک | عنوان و دامنه بررسی | شواهد و وضعیت |
|---|---|---|
| `P02-ARCH-CODE-013` | ماژول سبک `androidApp`، ساخت APK، نصب، ارتقای درجا (in-place upgrade) و حفظ داده در شبیه‌ساز اندروید | **DONE** — اجرای `tools/android/verify_android_app_shell.cjs`، نصب نسخه v1 (`versionCode: 1`) روی `Pixel_9_Pro_XL`، ثبت وضعیت و ارتقا به v2 (`versionCode: 2`) با تطابق امضا و حفظ داده (خروجی ۰). شواهد در `docs/evidence/P02-ARCH-CODE-013/`. |
| `P02-CI-OPS-016` | تفکیک گردش‌کار Release از Debug، تولید SBOMهای استاندارد، هش‌های SHA-256 و شکست قطعی در غیاب آرتیفکت | **DONE** — اصلاح `.github/workflows/release-artifact.yml`، تفکیک کامل آرتیفکت‌های انتشار، اعتبارسنجی SBOM (CycloneDX 1.5 و SPDX 2.3) و اجرای آزمون‌های مثبت و منفی بدون آرتیفکت در `docs/evidence/P02-CI-OPS-016/`. |
| `P02-QA-MANUAL-017` | اسموک کامل رفتاری پنج جریان اصلی (auth/home/product/cart/payment-return) با آداپتورهای واقعی و شبیه‌ساز زنده | **DONE** — پیاده‌سازی سوئیت `FoundationSmokeRealAdapterTest.kt` با ۸ تست پاس‌شده و اجرای عمیق سناریوها روی شبیه‌ساز زنده اندروید با دیپ‌لینک‌های موفق و لغو پرداخت. شواهد و اسکرین‌شات‌ها در `docs/evidence/P02-QA-MANUAL-017/`. |
| `P02-CORE-GATE-018` | ارزیابی نهایی گیت، تمرین عقب‌گرد/Forward-fix سازگار و ثبت مالک و موعد ۴ استثنای معماری | **DONE** — اجرای جامع تمامی آزمون‌های مرزی، اجرای موفق اسکریپت عقب‌گرد و تعیین حاکمیت ۴ استثنا (گزارش حاضر). |

---

## ۳. نتایج اعتبارسنجی خودکار و گیت‌های مرزی

تمام فرمان‌های راستی‌آزمایی معماری، مرزهای ماژولار، کیفیت کد و کامپایل به صورت زنده اجرا شدند:

1. **بررسی مرزهای وابستگی معماری (`architectureCheck`)**:
   - دستور: `.\gradlew.bat architectureCheck`
   - نتیجه: **BUILD SUCCESSFUL** (خروجی ۰ در ۲۴ ثانیه).
   - تعداد نقض‌های جدید: **۰** (تنها ۴ استثنای مجازِ کنترل‌شده گزارش شدند).
2. **وارونگی وابستگی فیچرها و ناوبری (`verify_feature_inversion.cjs`)**:
   - دستور: `node tools/architecture/verify_feature_inversion.cjs`
   - نتیجه: **ALL CHECKS PASSED** (خروجی ۰). ۳۳ فایل کاتلین تحلیل شدند؛ صفر ایمپورت غیرقانونی؛ تست `FeatureDependencyInversionTest` پاس شد.
3. **تفکیک گراف ناوبری (`verify_navigation_graphs.cjs`)**:
   - دستور: `node tools/navigation/verify_navigation_graphs.cjs`
   - نتیجه: **ALL TESTS PASSED** (خروجی ۰). تفکیک ۸ گراف فیچر و تست‌های کامپایل و یونیت ناوبری تأیید شدند.
4. **گیت‌های امنیتی و وابستگی (`verify_security_gates.cjs`)**:
   - دستور: `node tools/security/verify_security_gates.cjs`
   - نتیجه: **ALL TESTS PASSED** (خروجی ۰). اعتبارسنجی مثبت و منفی هش وابستگی‌ها و اسکن Gitleaks پاس شدند.
5. **گیت یکپارچه کیفیت وردپرس و اکشن‌لنت (`verify_all_quality_gates.cjs`)**:
   - دستور: `node tools/code-quality/verify_all_quality_gates.cjs`
   - نتیجه: **ALL QUALITY GATES VERIFIED (PASS)** (خروجی ۰). ۲۳ سناریوی Theme Check، ۱۹ سناریوی Plugin Check، actionlint 1.7.7، و پروب‌های WPCS پاس شدند.
6. **کامپایل پلتفرم‌های KMP (JS و JVM)**:
   - دستور: `.\gradlew.bat :composeApp:compileKotlinJvm :composeApp:compileKotlinJs`
   - نتیجه: **BUILD SUCCESSFUL** (خروجی ۰ در ۲۶ ثانیه).

---

## ۴. تمرین عقب‌گرد سازگار و Forward-fix (P02-T08)

اسکریپت `docs/evidence/P02-CORE-GATE-018/run_foundation_rollback_drill.cjs` تمرین جامع عقب‌گرد را با موفقیت ۱۰۰٪ اجرا و اثبات کرد:

1. **عقب‌گرد سورس و استراتژی Forward-Fix**:
   - اثبات شد که ریورت تک‌کامیتی در میانه تاریخچه به دلیل تغییرات بعدی روی فایل‌های مشترک ایجاد تضاد می‌کند.
   - اعمال هات‌فیکس Forward-fix در درخت کاری ایزوله با موفقیت ثبت شد و امکان ریورت تمیز آن اثبات گردید.
2. **دکترین عدم تخریب دیتابیس (Database Downgrade-Safe Doctrine)**:
   - کدهای مایگریشن دیتابیس در وردپرس (`class-cb-web-session-migration.php` و موارد مشابه) دکترین قطعی `never delete, repair or downgrade` را پیاده‌سازی کرده‌اند.
   - قفل اتمیک دیتابیس (`GET_LOCK`) مانع از race condition در مهاجرت‌ها می‌شود و وجود اسکیما نسخه ۱ بدون تخریب، وضعیت `CURRENT` را برمی‌گرداند.
3. **عدم تغییرناپذیری آرتیفکت‌های انتشار (Release Immutability)**:
   - آرتیفکت‌های انتشار نسخه‌دار، دارای هش SHA-256 و SBOM مجزا هستند. در صورت بروز هرگونه باگ بحرانی در Foundation، بازگشت به نسخه N-1 آرتیفکت بدون خطر تخریب داده امکان‌پذیر است.

---

## ۵. حاکمیت، مالکان و مواعد ۴ استثنای مجاز معماری (P02-T09)

چهار استثنای فعلی در `architecture-check.gradle.kts` به صورت شفاف ثبت و به فازهای هدف و موعد قطعی گیت فاز ۹ متصل شدند:

| استثنای مجاز در `architectureCheck` | مالک فنی (Owner) | فاز هدف برای Refactor | موعد قطعی (Hard Deadline) | راهبرد رفع وابستگی مستقیم |
|---|---|---|---|---|
| `:feature:academy -> :feature:details` | KMP Architecture (puriakazemieh) | فاز ۱۳ (`P13-LMS-*`) | گیت انتشار فاز ۰۹ (`P09-QA-GATE-018`) | معکوس‌سازی وابستگی ناوبری از طریق Navigator و رجیستری دیپ‌لینک در `core:navigation`. |
| `:feature:clinic -> :feature:details` | Clinic Domain Lead (puriakazemieh) | فاز ۱۴ (`P14-CLINIC-*`) | گیت انتشار فاز ۰۹ (`P09-QA-GATE-018`) | تفکیک ناوبری جزئیات پزشک/سرویس از طریق قرارداد روتینگ مشترک. |
| `:feature:main -> :feature:cart` | Core Commerce Team (puriakazemieh) | فاز ۰۴ (`P04-WORDPRESS-*`) | گیت انتشار فاز ۰۹ (`P09-QA-GATE-018`) | جایگزینی ایمپورت مستقیم ماژول سبد خرید با وضعیت اشتراکی و EventBus در `core:data`. |
| `:feature:main -> :feature:catalog` | Core Commerce Team (puriakazemieh) | فاز ۰۴ (`P04-WORDPRESS-*`) | گیت انتشار فاز ۰۹ (`P09-QA-GATE-018`) | روتینگ مرور کاتالوگ از طریق `AppNavHost` به‌جای وابستگی پروژه مستقیم ماژول به ماژول. |

---

## ۶. تصمیم نهایی گیت فاز ۰۲

**تصمیم:** `PASS`  
تمامی ۲۲ تسک اصلی و ۹۷ زیرتسک فاز ۲ با شواهد مستقل، آزمون‌های واقعی شبکه، شبیه‌ساز اندروید، داکر وردپرس و گیت‌های مرزی سبز به پایان رسیدند. هیچ وابستگی معوقی در فاز ۲ باقی نمانده و گیت با موفقیت کامل تصویب شد.
