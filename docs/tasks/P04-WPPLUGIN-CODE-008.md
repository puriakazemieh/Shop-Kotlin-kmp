<div dir="rtl" align="right">

# P04-WPPLUGIN-CODE-008 — قرارداد و adapter رسمی WooCommerce در هسته مشترک

## Prompt اجرای همین Task

```text
نقش تو Implementer و Verifier فقط همین Task است.

Repository:
D:\Android\AndroidStudioProjects\kmp-shop

Master checklist:
D:\Android\AndroidStudioProjects\kmp-shop\docs\MASTER_IMPLEMENTATION_CHECKLIST_FA.md

Source audit:
D:\Android\AndroidStudioProjects\kmp-shop\docs\PROJECT_AUDIT_AND_PUBLICATION_PLAN_FA.md

Task ID:
P04-WPPLUGIN-CODE-008

قبل از تغییر:
1. AGENTS.md و هر دستور ارجاع‌شده‌ای که واقعاً وجود دارد را بخوان.
2. Task، dependency، scope، acceptance و source reference را کامل بخوان.
3. git status را بررسی و تغییرات موجود کاربر را حفظ کن.
4. baseline test مشخص‌شده را اجرا کن.
5. اگر Task بزرگ‌تر از M یا مبهم است، اجرا نکن؛ آن را به Taskهای کوچک‌تر پیشنهاد بده.

قواعد:
- فقط همین Task را انجام بده.
- کمترین diff لازم را بساز.
- خارج از Allowed scope تغییر نده.
- dependency upgrade،refactor جانبی یا تغییر API contract انجام نده.
- secret یا داده واقعی ایجاد/ثبت نکن.
- deploy/publish/production/payment واقعی انجام نده مگر Task صریح و تأییدشده باشد.
- ابتدا تست شکست یا characterization مناسب را اضافه کن.
- همه commandهای verification را واقعاً اجرا کن.
- تست دستی اجرا‌نشده را تیک نزن و وضعیت را AWAITING_MANUAL_QA بگذار.
- بدون Evidence Task را DONE نکن.
- فقط checkbox/status/evidence همین Task را به‌روزرسانی کن.
- به Task بعدی نرو.

شرایط توقف:
- تداخل با تغییرات حل‌نشده کاربر
- نبود credential/contract/تصمیم ضروری
- نیاز به عملیات مخرب یا Production
- baseline failure مرتبط
- نیاز به تغییر contract خارج از Scope

پاسخ نهایی: Outcome،Changed files،Automated tests،Manual test status،Acceptance Criteria،Evidence paths،Checklist status change،Remaining risks/blockers و Rollback instructions.
```

- Status: TODO
- Phase/Area/Type: P04 / WPPLUGIN / CODE
- Priority/Risk/Size: P0/HIGH / UNASSESSED (قبل از READY تعیین شود)
- Owner: BOTH
- Completion authority: BOTH
- Depends on: P04-WPPLUGIN-CODE-007
- Blocks: P04-WPPLUGIN-CODE-009
- Requirement source: Master checklist row P04-WPPLUGIN-CODE-008 و Source audit بخش WPPLUGIN
- مبنای بازبرنامه‌ریزی: [تعریف محصولات مستقل](../INDEPENDENT_PRODUCTS_SPEC_FA.md) و [ADR-006](../architecture/adr/ADR-006-INDEPENDENT-PRODUCTS-AND-ENTITLEMENTS.md).

## هدف قابل اندازه‌گیری

WooCommerce منبع محصولات و سفارش‌ها بماند؛ قرارداد adapter مشترک محصولات/سفارش/سبد و inventory مسیرهای نوشتن تعریف و یک fixture read/write رسمی آزموده شود؛ انتقال کامل در 026A/026B است.

## خروجی مورد انتظار

قرارداد CRUD/Store API و مسئولیت cart با Woo روشن باشد؛ SQL مستقیم سفارش ممنوع و fixture در HPOS روشن/خاموش روی دو میزبان نتیجه یکسان بدهد.

## خارج از محدوده
- هر Feature،provider،platform یا refactor خارج از همین Task ID.
- deploy/publish،پرداخت واقعی،تغییر Production و تغییر داده مشتری.

## Preconditions
- Status باید READY باشد؛ TODO مجوز اجرا نیست.
- Dependencyها: P04-WPPLUGIN-CODE-007
- git status و baseline پیش از تغییر ثبت شوند.

## Allowed files/directories

- `wordpress/packages/carmilla-core/**`
- adapterهای مرتبط در `wordpress/carmilla-theme/**` و `wordpress/carmilla-bridge/**`
- `wordpress/**/tests/**` و `tools/test-env/**`
- `docs/evidence/P04-WPPLUGIN-CODE-008/**` و وضعیت همین کارت در `docs/**`

## Forbidden actions
- حذف/overwrite تغییرات کاربر،git reset/checkout،ارتقای dependency یا تغییر contract خارج Scope.
- ثبت credential،داده واقعی مشتری یا داده سلامت در repo/Evidence.
- عملیات Production یا migration تخریبی.

## مراحل پیاده‌سازی

1. مسیرهای product/order/cart دو میزبان و storage فعلی cart را inventory کن.
2. مرز adapter رسمی Woo را با قرارداد cart مشخص کن؛ موتور فروشگاه جدید نساز.
3. fixture کوچک read/write محصول/سفارش را با API رسمی در هر دو host اجرا کن.
4. نتیجه HPOS روشن/خاموش و شکاف‌های انتقال 026A/026B را ثبت کن.

## Automated tests با command و expected result
- Command: در محیط WordPress CI/container، lint و test محدود به Scope را اجرا کن.
- Expected: activation/install و تست مرتبط exit code 0؛ نبود PHP محلی مجوز تیک‌زدن نیست.
- معیار اختصاصی: قرارداد CRUD/Store API و مسئولیت cart با Woo روشن باشد؛ SQL مستقیم سفارش ممنوع و fixture در HPOS روشن/خاموش روی دو میزبان نتیجه یکسان بدهد.

## Manual tests با environment/data/steps/expected

- کجا: محصول/سفارش آزمایشی در WooCommerce و صفحه تشخیص اتصال هر میزبان.
- چگونه: با Woo فعال fixture بسازید و HPOS را در محیط آزمایشی تغییر دهید؛ سپس Woo را غیرفعال کنید.
- معیار موفقیت: داده با API رسمی برابر باشد؛ نبود Woo فقط قابلیت وابسته را با پیام روشن محدود کند.
- نسخه artifact/محیط،نام آزمونگر،تاریخ و شواهد داده مصنوعی ثبت شوند؛ موارد UI/شبکه/مهاجرت تا تأیید انسان `AWAITING_MANUAL_QA` هستند.

## Acceptance Criteria

- [ ] Woo پیش‌نیاز مجاز و منبع رسمی commerce باقی مانده است.
- [ ] adapter و storage cart قرارداد روشن دارند.
- [ ] fixture HPOS روشن/خاموش در هر دو host ثبت شده است.
- [ ] مهاجرت کامل دامنه و موتور commerce جدید در این کارت انجام نشده است.

## Security/Privacy/Migration checks
- Secret،Token،PII،PHI یا داده مشتری در source،log و Evidence ثبت نشود.
- برای API/write path،authorization و ownership بررسی شود.
- برای migration،forward fix/rollback و backup بررسی شود.

## Evidence
- مسیر: docs/evidence/P04-WPPLUGIN-CODE-008/
- baseline commit/build،command/cwd/exit code،test report،screenshot redacted و reviewer را ثبت کن.

## Rollback
- روش بازگشت کم‌خطر یا forward-fix پیش از تغییر ثبت شود.
- Migration/Payment/Secret/Health بدون backup و تأیید انسانی DONE نمی‌شود.

## Completion record
- Started at:
- Completed at:
- Changed files:
- Commands and exit codes:
- Manual tester/date/result:
- Evidence paths:
- Remaining risks/blockers:
- Final status: TODO | CODE_COMPLETE | AWAITING_MANUAL_QA | IN_REVIEW | DONE | BLOCKED

</div>
