<div dir="rtl" align="right">

# P04-WPPLUGIN-CODE-012 — چرخه فعال‌سازی دو میزبان و پاک‌سازی صریح داده سایت

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
P04-WPPLUGIN-CODE-012

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
- Depends on: P04-WPPLUGIN-CODE-011
- Blocks: P04-WPPLUGIN-CODE-013
- Requirement source: Master checklist row P04-WPPLUGIN-CODE-012 و Source audit بخش WPPLUGIN
- مبنای بازبرنامه‌ریزی: [تعریف محصولات مستقل](../INDEPENDENT_PRODUCTS_SPEC_FA.md) و [ADR-006](../architecture/adr/ADR-006-INDEPENDENT-PRODUCTS-AND-ENTITLEMENTS.md).

## هدف قابل اندازه‌گیری

activation/deactivation/theme switch و uninstall از kernel مشترک تبعیت کنند؛ حذف داده پیش‌فرض خاموش و cleanup فقط عملیات صریح مدیر باشد.

## خروجی مورد انتظار

خاموشی فیچر یا میزبان داده را حذف نکند؛ با میزبان سازگار و مجاز دیگر خدمات ادامه یابند،و با خاموشی آخرین میزبان اجرای خدمات متوقف اما داده محفوظ بماند.

## خارج از محدوده
- هر Feature،provider،platform یا refactor خارج از همین Task ID.
- deploy/publish،پرداخت واقعی،تغییر Production و تغییر داده مشتری.

## Preconditions
- Status باید READY باشد؛ TODO مجوز اجرا نیست.
- Dependencyها: P04-WPPLUGIN-CODE-011
- git status و baseline پیش از تغییر ثبت شوند.

## Allowed files/directories

- `wordpress/packages/carmilla-core/**`
- adapterهای مرتبط در `wordpress/carmilla-theme/**` و `wordpress/carmilla-bridge/**`
- `wordpress/**/tests/**` و `tools/test-env/**`
- `docs/evidence/P04-WPPLUGIN-CODE-012/**` و وضعیت همین کارت در `docs/**`

## Forbidden actions
- حذف/overwrite تغییرات کاربر،git reset/checkout،ارتقای dependency یا تغییر contract خارج Scope.
- ثبت credential،داده واقعی مشتری یا داده سلامت در repo/Evidence.
- عملیات Production یا migration تخریبی.

## مراحل پیاده‌سازی

1. hookهای lifecycle و وابستگی داده به host را inventory کن.
2. یک policy مشترک data retention و opt-in cleanup تعریف و host hookها را متصل کن.
3. خاموشی یکی/آخرین میزبان و پاک‌سازی بدون opt-in را تست کن.
4. جابه‌جایی/adoption کامل و انتقال مجوز را به کارت 042 بسپار و اینجا رفتار hook را ثابت کن.

## Automated tests با command و expected result
- Command: در محیط WordPress CI/container، lint و test محدود به Scope را اجرا کن.
- Expected: activation/install و تست مرتبط exit code 0؛ نبود PHP محلی مجوز تیک‌زدن نیست.
- معیار اختصاصی: خاموشی فیچر یا میزبان داده را حذف نکند؛ با میزبان سازگار و مجاز دیگر خدمات ادامه یابند،و با خاموشی آخرین میزبان اجرای خدمات متوقف اما داده محفوظ بماند.

## Manual tests با environment/data/steps/expected

- کجا: فهرست محصولات فعال،داده fixture و صفحه cleanup مدیر.
- چگونه: یک میزبان از نصب هم‌زمان را خاموش کنید؛ سپس آخرین میزبان را خاموش/روشن و uninstall بدون opt-in را آزمایش کنید.
- معیار موفقیت: داده حفظ شود؛ ادامه خدمات فقط با میزبان سازگار و مجاز دیگر رخ دهد و بدون opt-in هیچ purge انجام نشود.
- نسخه artifact/محیط،نام آزمونگر،تاریخ و شواهد داده مصنوعی ثبت شوند؛ موارد UI/شبکه/مهاجرت تا تأیید انسان `AWAITING_MANUAL_QA` هستند.

## Acceptance Criteria

- [ ] deactivate/theme switch داده را پاک نمی‌کند.
- [ ] خاموشی آخرین میزبان توقف اجرا را صریح نشان می‌دهد.
- [ ] cleanup capability/nonce و opt-in مستقل دارد.
- [ ] adoption/entitlement انتقالی به کارت 042 ارجاع شده است.

## Security/Privacy/Migration checks
- Secret،Token،PII،PHI یا داده مشتری در source،log و Evidence ثبت نشود.
- برای API/write path،authorization و ownership بررسی شود.
- برای migration،forward fix/rollback و backup بررسی شود.

## Evidence
- مسیر: docs/evidence/P04-WPPLUGIN-CODE-012/
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
