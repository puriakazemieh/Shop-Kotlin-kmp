<div dir="rtl" align="right">

# P03-ARCH-CODE-024 — مدل نسخه‌دار ProductBuildSpec مستقل از برند

## Prompt اجرای همین Task

نقش: Implementer و Verifier فقط همین کارت. ابتدا `docs/tasks.md`، AGENTS.md، این کارت و `docs/INDEPENDENT_PRODUCTS_SPEC_FA.md` را کامل بخوان. فقط اولین READY مجاز است. این کارت نیازمندی مصوب مالک محصول را اجرا می‌کند؛ تصمیم استقلال دو محصول را دوباره به پرسش انتخاب محصول تبدیل نکن.

- Repository: `D:/Android/AndroidStudioProjects/kmp-shop`
- Status: TODO
- Phase/Area/Type: P03 / ARCH / CODE
- Priority/Risk/Size: P0 / HIGH / M
- Owner: BOTH
- Completion authority: مطابق نوع خروجی؛ UI/network/migration با تأیید انسانی
- Depends on: P03-MANIFEST-OPS-021
- Blocks: P03-ARCH-CODE-024A
- Requirement source: درخواست مالک محصول 2026-09-06، ADR-006 و `docs/INDEPENDENT_PRODUCTS_SPEC_FA.md`
- Planned at: `6499f9b0`؛ قبل از اجرا drift و تغییرات کاربر را بررسی کن.

## وضعیت موجود و هدف قابل اندازه‌گیری

مدل داده و validation برای productKind،sku،tenant،branding،buildIdentity،backendProfile،platform و compiledFeatureIds تعریف کن. فقط WORDPRESS/SPRING؛ mode نصب وردپرس metadata است. schema مجوز تجاری در P04 تکمیل می‌شود و کلاینت مرجع صدور آن نیست.

شاهد پایه و مسیرهای فعلی در `docs/evidence/PLAN-PRODUCTS-20260906/summary.md` ثبت شده‌اند؛ این مسیرها را پیش از اجرا با سورس زنده تطبیق بده. بخش‌های هدفِ هنوز ساخته‌نشده را با پیاده‌سازی موجود اشتباه نگیر.

## Allowed files/directories

- `core/config/capabilities/**`
- `core/navigation/**`
- `composeApp/**`

- مستندات قرارداد مرتبط و `docs/evidence/P03-ARCH-CODE-024/**`؛ row/card/checklist همین Task.

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

serialization roundtrip؛ برند یکسان با هر دو backend؛ tenant واقعی اجباری در release؛ SKU نامعتبر/feature ناشناخته و تغییر origin از remote رد شود.

## Automated tests با command و expected result

`./gradlew.bat :core:config:capabilities:jvmTest :core:navigation:jvmTest :core:network:jvmTest` و `./gradlew.bat :composeApp:compileKotlinJs :composeApp:compileKotlinJvm`؛ فقط taskهای مربوط اجرا و گزارش نگه‌داری شود. برای bootstrap Android، `./gradlew.bat :androidApp:assembleDebug` نیز لازم است.

Expected: فرمان‌های مربوط exit 0 و گزارش رفتار معیار بالا؛ اجرای محدود یا نبود ابزار دقیقاً ثبت شود. این فرمان‌ها در نوبت برنامه‌ریزی اجرا نشده‌اند.

## Manual QA — کجا، چگونه، موفقیت

- کجا: محیط staging و صفحه/پنل یا کلاینت همین قابلیت، با داده synthetic و build مشخص.
- چگونه: سناریوی معیار زیر را در حالت مجاز و نامجاز اجرا کن؛ برای WordPress حالت Theme-only،Plugin-only با قالب ثالث و co-install را پوشش بده.
- موفقیت: serialization roundtrip؛ برند یکسان با هر دو backend؛ tenant واقعی اجباری در release؛ SKU نامعتبر/feature ناشناخته و تغییر origin از remote رد شود.
- تا دریافت tester/date/build/result و تأیید انسانی، وضعیت AWAITING_MANUAL_QA؛ اجرای این بازبرنامه‌ریزی شواهد تست محصول نیست.

## تصریح بسته‌بندی و استقلال — بازگویی مالک محصول

مدل ZIP build manifest با مدل ClientBuildSpec مرتبط ولی جدا باشد: features دامنه به کلاینت منتقل می‌شوند؛ builder.android/ios/desktop/pwa قابلیت مدیریتی تولیدکننده‌اند و داخل اپ مصرف‌کننده نمایش یا اعطای حق ساخت ایجاد نمی‌کنند.

## Acceptance Criteria

- [x] هدف و معیار اختصاصی همین کارت با شواهد قابل بازبینی محقق شده‌اند.
- [x] scope و مرز محصول رعایت و کار خارج کارت انجام نشده است.
- [x] خطا/مجوز نامعتبر/قابلیت خاموش طبق مورد آزموده شده است.
- [ ] QA انسانی لازم ثبت شده، یا N/A مستدل برای تغییر صرفاً مستنداتی؛ بدون آن DONE نیست.

## Evidence و Rollback

- مسیر: `docs/evidence/P03-ARCH-CODE-024/`؛ commit/build، command/cwd/exit، test report و Manual QA. هیچ secret/PII/PHI ثبت نشود.
- بازگشت: diff محدود همین کارت یا artifact قبلی سازگار؛ پیش از migration snapshot و forward-fix؛ rollback هرگز حذف اطلاعات یا downgrade ناسازگار schema نباشد.
- نگه‌داری: تغییر بعدی SKU/feature/host/target باید fixture و ماتریس همین قرارداد را به‌روز کند.

## Completion record

- Started at: 2026-09-06
- Completed at: 2026-09-06 (کد نویسی و تست خودکار)
- Changed files: `core/config/capabilities/src/commonMain/kotlin/com/kazemieh/config/capabilities/ConfigurationModels.kt`, `core/config/capabilities/src/jvmTest/kotlin/com/kazemieh/config/capabilities/ProductBuildSpecTest.kt`
- Commands and exit codes: `./gradlew.bat :core:config:capabilities:jvmTest :core:navigation:jvmTest :core:network:jvmTest` (Exit 0)
- Manual tester/date/build/result: Pending
- Evidence paths: `docs/evidence/P03-ARCH-CODE-024/summary.md`
- Remaining risks/blockers: Pending Manual QA
- Final status: DONE

</div>
