<div dir="rtl" align="right">

# P04-ENTITLEMENT-CODE-044 — ارتقای خرید و به‌روزرسانی سازگار هر SKU

## Prompt اجرای همین Task

نقش: Implementer و Verifier فقط همین کارت. ابتدا `docs/tasks.md`، AGENTS.md، این کارت و `docs/INDEPENDENT_PRODUCTS_SPEC_FA.md` را کامل بخوان. فقط اولین READY مجاز است. این کارت نیازمندی مصوب مالک محصول را اجرا می‌کند؛ تصمیم استقلال دو محصول را دوباره به پرسش انتخاب محصول تبدیل نکن.

- Repository: `D:/Android/AndroidStudioProjects/kmp-shop`
- Status: TODO
- Phase/Area/Type: P04 / ENTITLEMENT / CODE
- Priority/Risk/Size: P0 / HIGH / M
- Owner: BOTH
- Completion authority: مطابق نوع خروجی؛ UI/network/migration با تأیید انسانی
- Depends on: P04-CI-CODE-043
- Blocks: P04-CI-CODE-019
- Requirement source: درخواست مالک محصول 2026-09-06، ADR-006 و `docs/INDEPENDENT_PRODUCTS_SPEC_FA.md`
- Planned at: `6499f9b0`؛ قبل از اجرا drift و تغییرات کاربر را بررسی کن.

## وضعیت موجود و هدف قابل اندازه‌گیری

مسیر upgrade package+entitlement و updater محصول را به SKU/site/version متصل کن؛ ZIP عمومی نباید حق خرید یا کد نسخه دیگر را ناخواسته جایگزین کند. سیاست grace/renewal/transfer مطابق039 اجرا شود.

شاهد پایه و مسیرهای فعلی در `docs/evidence/PLAN-PRODUCTS-20260906/summary.md` ثبت شده‌اند؛ این مسیرها را پیش از اجرا با سورس زنده تطبیق بده. بخش‌های هدفِ هنوز ساخته‌نشده را با پیاده‌سازی موجود اشتباه نگیر.

## Allowed files/directories

- `wordpress/packages/carmilla-core/**`
- `wordpress/carmilla-theme/**`
- `wordpress/carmilla-bridge/**`
- `wordpress/**/tests/**`
- `tools/test-env/**`
- `tools/wordpress-packaging/**`

- مستندات قرارداد مرتبط و `docs/evidence/P04-ENTITLEMENT-CODE-044/**`؛ row/card/checklist همین Task.

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

Base→Booking داده/هویت حفظ؛ downgrade/revoke مطابق قرارداد و بدون purge؛ package غلط/مجوز سایت دیگر رد؛ update co-install به یک kernel سازگار.

## Automated tests با command و expected result

`docker compose -f tools/test-env/docker-compose.yml config` و `git diff --check`؛ lint در Docker طبق RTK. فرمان موجود `bash wordpress/build-theme-zip.sh` و `bash wordpress/build-bridge-zip.sh` فقط sanity بسته‌اند؛ تست رفتاری اختصاصی این کارت را در harness WP واقعی اجرا و فرمان دقیق و exit code در Evidence ثبت کن. صرف config/lint اثبات acceptance نیست.

Expected: فرمان‌های مربوط exit 0 و گزارش رفتار معیار بالا؛ اجرای محدود یا نبود ابزار دقیقاً ثبت شود. این فرمان‌ها در نوبت برنامه‌ریزی اجرا نشده‌اند.

## Manual QA — کجا، چگونه، موفقیت

- کجا: محیط staging و صفحه/پنل یا کلاینت همین قابلیت، با داده synthetic و build مشخص.
- چگونه: سناریوی معیار زیر را در حالت مجاز و نامجاز اجرا کن؛ برای WordPress حالت Theme-only،Plugin-only با قالب ثالث و co-install را پوشش بده.
- موفقیت: Base→Booking داده/هویت حفظ؛ downgrade/revoke مطابق قرارداد و بدون purge؛ package غلط/مجوز سایت دیگر رد؛ update co-install به یک kernel سازگار.
- تا دریافت tester/date/build/result و تأیید انسانی، وضعیت AWAITING_MANUAL_QA؛ اجرای این بازبرنامه‌ریزی شواهد تست محصول نیست.

## تصریح بسته‌بندی و استقلال — بازگویی مالک محصول

افزودن قابلیت حذف‌شده از ZIP فقط با تحویل نسخه جدید آن SKU؛ روشن/خاموش قابلیت موجود و مجاز بدون ZIP جدید. update معمولی نباید ماژول خارج خرید یا builder target جدید وارد کند.

## Acceptance Criteria

- [ ] هدف و معیار اختصاصی همین کارت با شواهد قابل بازبینی محقق شده‌اند.
- [ ] scope و مرز محصول رعایت و کار خارج کارت انجام نشده است.
- [ ] خطا/مجوز نامعتبر/قابلیت خاموش طبق مورد آزموده شده است.
- [ ] QA انسانی لازم ثبت شده، یا N/A مستدل برای تغییر صرفاً مستنداتی؛ بدون آن DONE نیست.

## Evidence و Rollback

- مسیر: `docs/evidence/P04-ENTITLEMENT-CODE-044/`؛ commit/build، command/cwd/exit، test report و Manual QA. هیچ secret/PII/PHI ثبت نشود.
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
