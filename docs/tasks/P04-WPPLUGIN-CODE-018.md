<div dir="rtl" align="right">

# P04-WPPLUGIN-CODE-018 — ترجمه و escaping مرزهای مشترک و میزبان‌های مستقل

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
P04-WPPLUGIN-CODE-018

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
- Priority/Risk/Size: P1/LOW / UNASSESSED (قبل از READY تعیین شود)
- Owner: AI
- Completion authority: BOTH
- Depends on: P04-WPTHEME-CODE-025
- Blocks: P04-WPPLUGIN-CODE-045
- Requirement source: Master checklist row P04-WPPLUGIN-CODE-018 و Source audit بخش WPPLUGIN
- مبنای بازبرنامه‌ریزی: [تعریف محصولات مستقل](../INDEPENDENT_PRODUCTS_SPEC_FA.md) و [ADR-006](../architecture/adr/ADR-006-INDEPENDENT-PRODUCTS-AND-ENTITLEMENTS.md).

## هدف قابل اندازه‌گیری

بارگذاری textdomain/POT و escaping برای رشته‌های عمومی kernel،onboarding و تنظیمات دو host تثبیت شود؛ رشته‌های جدید هر vertical همراه کارت همان vertical پوشش داده شوند.

## خروجی مورد انتظار

ZIP پوسته و افزونه مستقل فارسی/انگلیسی را درست بارگذاری کنند؛ co-install ترجمه تکراری/گم‌شده و output escape نشده در مرزهای این کارت نداشته باشد.

## خارج از محدوده
- هر Feature،provider،platform یا refactor خارج از همین Task ID.
- deploy/publish،پرداخت واقعی،تغییر Production و تغییر داده مشتری.

## Preconditions
- Status باید READY باشد؛ TODO مجوز اجرا نیست.
- Dependencyها: P04-WPTHEME-CODE-025
- git status و baseline پیش از تغییر ثبت شوند.

## Allowed files/directories

- `wordpress/packages/carmilla-core/**`
- adapterهای مرتبط در `wordpress/carmilla-theme/**` و `wordpress/carmilla-bridge/**`
- `wordpress/**/tests/**` و `tools/test-env/**`
- `docs/evidence/P04-WPPLUGIN-CODE-018/**` و وضعیت همین کارت در `docs/**`

## Forbidden actions
- حذف/overwrite تغییرات کاربر،git reset/checkout،ارتقای dependency یا تغییر contract خارج Scope.
- ثبت credential،داده واقعی مشتری یا داده سلامت در repo/Evidence.
- عملیات Production یا migration تخریبی.

## مراحل پیاده‌سازی

1. مالکیت textdomain kernel و hostها را مشخص و رشته‌های مرز پایه را inventory کن.
2. POT و load path دو ZIP را اصلاح و escaping همان مرزها را بررسی کن.
3. locale switch را در دو محصول مستقل و هم‌زمان اجرا کن.
4. وظیفه رشته‌های vertical آینده را در قرارداد مشارکت ثبت کن؛ ترجمه همه controllerها یکجا در scope نیست.

## Automated tests با command و expected result
- Command: در محیط WordPress CI/container، lint و test محدود به Scope را اجرا کن.
- Expected: activation/install و تست مرتبط exit code 0؛ نبود PHP محلی مجوز تیک‌زدن نیست.
- معیار اختصاصی: ZIP پوسته و افزونه مستقل فارسی/انگلیسی را درست بارگذاری کنند؛ co-install ترجمه تکراری/گم‌شده و output escape نشده در مرزهای این کارت نداشته باشد.

## Manual tests با environment/data/steps/expected

- کجا: onboarding و تنظیمات Carmilla با locale فارسی و انگلیسی.
- چگونه: هر ZIP را تنها نصب کنید؛ زبان را تغییر و متن مصنوعی دارای نویسه HTML را در فیلد مجاز وارد کنید؛ حالت هم‌زمان را نیز ببینید.
- معیار موفقیت: ترجمه صحیح،escaping امن و بارگذاری بدون وابستگی به textdomain محصول دیگر باشد.
- نسخه artifact/محیط،نام آزمونگر،تاریخ و شواهد داده مصنوعی ثبت شوند؛ موارد UI/شبکه/مهاجرت تا تأیید انسان `AWAITING_MANUAL_QA` هستند.

## Acceptance Criteria

- [ ] textdomain هر میزبان و kernel مسئولیت روشن دارد.
- [ ] POT در هر ZIP مستقل موجود است.
- [ ] locale و escaping مرز پایه آزموده‌اند.
- [ ] پوشش رشته verticalها به کارت دامنه محدود و قابل پیگیری است.

## Security/Privacy/Migration checks
- Secret،Token،PII،PHI یا داده مشتری در source،log و Evidence ثبت نشود.
- برای API/write path،authorization و ownership بررسی شود.
- برای migration،forward fix/rollback و backup بررسی شود.

## Evidence
- مسیر: docs/evidence/P04-WPPLUGIN-CODE-018/
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
