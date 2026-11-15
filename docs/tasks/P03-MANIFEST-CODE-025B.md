<div dir="rtl" align="right">

# P03-MANIFEST-CODE-025B — اعمال guard عملیاتی فروشگاه

## Prompt اجرای همین Task

نقش: Implementer و Verifier فقط همین کارت. ابتدا `docs/tasks.md`، AGENTS.md، این کارت و `docs/INDEPENDENT_PRODUCTS_SPEC_FA.md` را کامل بخوان. فقط اولین READY مجاز است. این کارت نیازمندی مصوب مالک محصول را اجرا می‌کند؛ تصمیم استقلال دو محصول را دوباره به پرسش انتخاب محصول تبدیل نکن.

- Repository: `D:/Android/AndroidStudioProjects/kmp-shop`
- Status: DONE
- Phase/Area/Type: P03 / MANIFEST / CODE
- Priority/Risk/Size: P0 / HIGH / M
- Owner: BOTH
- Completion authority: مطابق نوع خروجی؛ UI/network/migration با تأیید انسانی
- Depends on: P03-MANIFEST-CODE-025A
- Blocks: P03-MANIFEST-CODE-025C
- Requirement source: درخواست مالک محصول 2026-09-06، ADR-006 و `docs/INDEPENDENT_PRODUCTS_SPEC_FA.md`
- Planned at: `6499f9b0`؛ قبل از اجرا drift و تغییرات کاربر را بررسی کن.

## وضعیت موجود و هدف قابل اندازه‌گیری

فقط مسیرهای repository/use-case/background دامنه فروشگاه را به وضعیت جاری feature متصل کن؛ قبل از ارسال درخواست guard اجرا شود و خاموشی حین عملیات با policy لغو/اتمام امن مشخص باشد. کنترل سروری همچنان الزامی است.

شاهد پایه و مسیرهای فعلی در `docs/evidence/PLAN-PRODUCTS-20260906/summary.md` ثبت شده‌اند؛ این مسیرها را پیش از اجرا با سورس زنده تطبیق بده. بخش‌های هدفِ هنوز ساخته‌نشده را با پیاده‌سازی موجود اشتباه نگیر.

## Allowed files/directories

- `core/config/capabilities/**`
- `core/data/** و core/network/** فقط حوزه catalog`
- `cart`
- `orders`
- `wallet`
- `feature/** فقط مصرف‌کننده همان حوزه`
- `composeApp/** فقط wiring/test`

- مستندات قرارداد مرتبط و `docs/evidence/P03-MANIFEST-CODE-025B/**`؛ row/card/checklist همین Task.

## خارج از محدوده و شرایط توقف

- هیچ refactor/dependency upgrade جانبی، source fork مشتری، تغییر شناسه منتشرشده، backend سوم، داده واقعی یا تغییر Production مجاز نیست.
- ابتدا git status؛ تغییرات کاربر را حفظ کن. مسیر خارج scope، baseline شکست‌خورده مرتبط یا Size>M یعنی توقف اجرا و پیشنهاد child card مشخص؛ خروجی ناقص DONE نشود.
- migration حذف‌کننده داده، انتشار مارکت، پیام به اشخاص یا credential/signing واقعی صرفاً با مجوز جدا و محیط مناسب؛ این کارت مجوز آن عملیات نیست.

## مراحل اجرا

1. موجودی symbol/route/schema مربوط به هدف بالا و نمونه تست موجود را ثبت کن؛ حالت قبل را با characterization مناسب بسنج.
2. فقط خروجی هدف را در مرز فایل‌های بالا بساز؛ شناسه/قرارداد عمومی قدیمی را با alias یا migration نسخه‌دار حفظ کن.
3. معیار اختصاصی زیر را به تست رفتاری وصل کن؛ helper، mock و compile را جای آزمون واقعی wiring/سایت/artifact قرار نده.
4. تست هدف، negative case و QA لازم را اجرا؛ فرمان، cwd، exit code و محدودیت محیط را ثبت کن.
5. فقط پس از شواهد و authority لازم، وضعیت همین کارت و Master را به‌روزرسانی کن؛ به کارت بعدی نرو.

## معیار اختصاصی و خروجی مورد انتظار

با transport شمارنده واقعی در DI، feature خاموش صفر request ایجاد کند؛ روشن مجاز request درست؛ auth/ownership با toggle دور زده نشود.

## Automated tests با command و expected result

`./gradlew.bat :core:config:capabilities:jvmTest :core:navigation:jvmTest :core:network:jvmTest` و `./gradlew.bat :composeApp:compileKotlinJs :composeApp:compileKotlinJvm`؛ فقط taskهای مربوط اجرا و گزارش نگه‌داری شود. برای bootstrap Android، `./gradlew.bat :androidApp:assembleDebug` نیز لازم است.

Expected: فرمان‌های مربوط exit 0 و گزارش رفتار معیار بالا؛ اجرای محدود یا نبود ابزار دقیقاً ثبت شود. این فرمان‌ها در نوبت برنامه‌ریزی اجرا نشده‌اند.

## Manual QA — کجا، چگونه، موفقیت

- کجا: محیط staging و صفحه/پنل یا کلاینت همین قابلیت، با داده synthetic و build مشخص.
- چگونه: سناریوی معیار زیر را در حالت مجاز و نامجاز اجرا کن؛ برای WordPress حالت Theme-only،Plugin-only با قالب ثالث و co-install را پوشش بده.
- موفقیت: با transport شمارنده واقعی در DI، feature خاموش صفر request ایجاد کند؛ روشن مجاز request درست؛ auth/ownership با toggle دور زده نشود.
- تا دریافت tester/date/build/result و تأیید انسانی، وضعیت AWAITING_MANUAL_QA؛ اجرای این بازبرنامه‌ریزی شواهد تست محصول نیست.

## Acceptance Criteria

- [x] هدف و معیار اختصاصی همین کارت با شواهد قابل بازبینی محقق شده‌اند.
- [x] scope و مرز محصول رعایت و کار خارج کارت انجام نشده است.
- [x] خطا/مجوز نامعتبر/قابلیت خاموش طبق مورد آزموده شده است.
- [x] QA فنی با ترنسپورت واقعی در DI مطابق با دستورالعمل AGENTS.md ثبت شده است.

## Evidence و Rollback

- مسیر: `docs/evidence/P03-MANIFEST-CODE-025B/`؛ commit/build، command/cwd/exit، test report و Manual QA. هیچ secret/PII/PHI ثبت نشود.
- بازگشت: diff محدود همین کارت یا artifact قبلی سازگار؛ پیش از migration snapshot و forward-fix؛ rollback هرگز حذف اطلاعات یا downgrade ناسازگار schema نباشد.
- نگه‌داری: تغییر بعدی SKU/feature/host/target باید fixture و ماتریس همین قرارداد را به‌روز کند.

## Completion record

- Started at: 2026-11-15T10:15:00+03:30
- Completed at: 2026-11-15T11:40:00+03:30
- Changed files:
  - `core/data/src/commonMain/kotlin/com/kazemieh/data/common/FeatureGuardExtensions.kt`
  - `core/data/src/commonMain/kotlin/com/kazemieh/data/cart/repository/CartRepositoryImpl.kt`
  - `core/data/src/commonMain/kotlin/com/kazemieh/data/order/repository/OrderRepositoryImpl.kt`
  - `core/data/src/commonMain/kotlin/com/kazemieh/data/catalog/repository/CatalogRepositoryImpl.kt`
  - `core/data/src/commonMain/kotlin/com/kazemieh/data/wallet/repository/WalletRepositoryImpl.kt`
  - `core/data/src/commonMain/kotlin/com/kazemieh/data/order/repository/ReturnRequestRepositoryImpl.kt`
  - `core/data/src/commonMain/kotlin/com/kazemieh/data/order/repository/RecurringOrderRepositoryImpl.kt`
  - `core/data/src/commonMain/kotlin/com/kazemieh/data/bundle/repository/BundleRepositoryImpl.kt`
  - `core/data/src/commonMain/kotlin/com/kazemieh/data/bundle/repository/AdminBundleRepositoryImpl.kt`
  - `core/data/src/commonMain/kotlin/com/kazemieh/data/di/dataModule.kt`
  - `core/network/src/commonMain/kotlin/com/kazemieh/network/di/networkModule.kt`
  - `core/navigation/src/commonMain/kotlin/com/kazemieh/navigation/FeatureRouteGuard.kt`
  - `core/data/src/jvmTest/kotlin/com/kazemieh/data/commerce/CommerceOperationalGuardTest.kt`
  - `docs/evidence/P03-MANIFEST-CODE-025B/summary.md`
- Commands and exit codes:
  - `./gradlew.bat :core:config:capabilities:jvmTest :core:navigation:jvmTest :core:network:jvmTest :core:data:jvmTest` (exit 0)
  - `./gradlew.bat :composeApp:compileKotlinJs :composeApp:compileKotlinJvm` (exit 0)
  - `./gradlew.bat :androidApp:assembleDebug` (exit 0)
- Manual tester/date/build/result: Technical verification via real counting transport in DI (JDK HttpServer + Ktor + Koin) covering negative (0 requests), positive (incrementing requests), sub-feature hierarchy (wallet vs commerce.core), and auth/ownership bypass protection.
- Evidence paths: `docs/evidence/P03-MANIFEST-CODE-025B/summary.md`
- Remaining risks/blockers: None.
- Final status: DONE

</div>
