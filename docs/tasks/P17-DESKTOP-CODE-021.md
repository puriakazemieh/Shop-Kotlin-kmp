<div dir="rtl" align="right">

# P17-DESKTOP-CODE-021 — پخش واقعی محتوای آموزشی در Desktop

## Prompt اجرای همین Task

نقش: Implementer و Verifier فقط همین کارت. ابتدا `docs/tasks.md`، AGENTS.md، این کارت و `docs/INDEPENDENT_PRODUCTS_SPEC_FA.md` را کامل بخوان. فقط اولین READY مجاز است. این کارت نیازمندی مصوب مالک محصول را اجرا می‌کند؛ تصمیم استقلال دو محصول را دوباره به پرسش انتخاب محصول تبدیل نکن.

- Repository: `D:/Android/AndroidStudioProjects/kmp-shop`
- Status: TODO
- Phase/Area/Type: P17 / DESKTOP / CODE
- Priority/Risk/Size: P0 / HIGH / M
- Owner: BOTH
- Completion authority: مطابق نوع خروجی؛ UI/network/migration با تأیید انسانی
- Depends on: P17-DESKTOP-CODE-008
- Blocks: P17-DESKTOP-OPS-009
- Requirement source: درخواست مالک محصول 2026-09-06، ADR-006 و `docs/INDEPENDENT_PRODUCTS_SPEC_FA.md`
- Planned at: `6499f9b0`؛ قبل از اجرا drift و تغییرات کاربر را بررسی کن.

## وضعیت موجود و هدف قابل اندازه‌گیری

VideoPlayer.jvm.kt placeholder را برای سیستم‌عامل‌های scope با player مناسب و lifecycle قابل cleanup کامل کن؛ دسترسی lesson و expiry مجوز را رعایت کن.

شاهد پایه و مسیرهای فعلی در `docs/evidence/PLAN-PRODUCTS-20260906/summary.md` ثبت شده‌اند؛ این مسیرها را پیش از اجرا با سورس زنده تطبیق بده. بخش‌های هدفِ هنوز ساخته‌نشده را با پیاده‌سازی موجود اشتباه نگیر.

## Allowed files/directories

- `feature/details/src/jvmMain/**`
- `feature/academy/** فقط player integration`
- `composeApp/build.gradle.kts فقط dependency ضروری player`

- مستندات قرارداد مرتبط و `docs/evidence/P17-DESKTOP-CODE-021/**`؛ row/card/checklist همین Task.

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

پخش/توقف/resume واقعی؛ teardown بدون نشت player؛ media خصوصی خارج cache مجاز ذخیره نشود؛ unsupported OS پیام روشن.

## Automated tests با command و expected result

`./gradlew.bat :composeApp:compileKotlinJvm :composeApp:packageUberJarForCurrentOS` روی Windows؛ installer و signing هر OS طبق runbook P17. JAR تنها معیار فروش نیست.

Expected: فرمان‌های مربوط exit 0 و گزارش رفتار معیار بالا؛ اجرای محدود یا نبود ابزار دقیقاً ثبت شود. این فرمان‌ها در نوبت برنامه‌ریزی اجرا نشده‌اند.

## Manual QA — کجا، چگونه، موفقیت

- کجا: محیط staging و صفحه/پنل یا کلاینت همین قابلیت، با داده synthetic و build مشخص.
- چگونه: سناریوی معیار زیر را در حالت مجاز و نامجاز اجرا کن؛ برای WordPress حالت Theme-only،Plugin-only با قالب ثالث و co-install را پوشش بده.
- موفقیت: پخش/توقف/resume واقعی؛ teardown بدون نشت player؛ media خصوصی خارج cache مجاز ذخیره نشود؛ unsupported OS پیام روشن.
- تا دریافت tester/date/build/result و تأیید انسانی، وضعیت AWAITING_MANUAL_QA؛ اجرای این بازبرنامه‌ریزی شواهد تست محصول نیست.

## Acceptance Criteria

- [ ] هدف و معیار اختصاصی همین کارت با شواهد قابل بازبینی محقق شده‌اند.
- [ ] scope و مرز محصول رعایت و کار خارج کارت انجام نشده است.
- [ ] خطا/مجوز نامعتبر/قابلیت خاموش طبق مورد آزموده شده است.
- [ ] QA انسانی لازم ثبت شده، یا N/A مستدل برای تغییر صرفاً مستنداتی؛ بدون آن DONE نیست.

## Evidence و Rollback

- مسیر: `docs/evidence/P17-DESKTOP-CODE-021/`؛ commit/build، command/cwd/exit، test report و Manual QA. هیچ secret/PII/PHI ثبت نشود.
- بازگشت: diff محدود همین کارت یا artifact قبلی سازگار؛ پیش از migration snapshot و forward-fix؛ rollback هرگز حذف اطلاعات یا downgrade ناسازگار schema نباشد.
- نگه‌داری: تغییر بعدی SKU/feature/host/target باید fixture و ماتریس همین قرارداد را به‌روز کند.

## Completion record

- Started at:
- Completed at:
- Changed files:
- Commands and exit codes:
- Manual tester/date/build/result:
- Evidence paths:
- Remaining risks/blockers:
- Final status: TODO

</div>
