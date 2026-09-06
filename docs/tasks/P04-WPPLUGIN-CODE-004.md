<div dir="rtl" align="right">

# P04-WPPLUGIN-CODE-004 — bootstrap اولیه و بسته‌بندی هسته در هر دو ZIP

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
P04-WPPLUGIN-CODE-004

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
- Priority/Risk/Size: P0 / HIGH / M
- Owner: BOTH
- Completion authority: BOTH
- Depends on: P04-WPPLUGIN-CODE-003
- Blocks: P04-WPTHEME-CODE-005
- Requirement source: Master checklist row P04-WPPLUGIN-CODE-004 و Source audit بخش WPPLUGIN
- مبنای بازبرنامه‌ریزی: [تعریف محصولات مستقل](../INDEPENDENT_PRODUCTS_SPEC_FA.md) و [ADR-006](../architecture/adr/ADR-006-INDEPENDENT-PRODUCTS-AND-ENTITLEMENTS.md).

## هدف قابل اندازه‌گیری

یک هسته versioned از سورس مشترک داخل ZIP پوسته و افزونه بسته‌بندی و از هر دو میزبان بارگذاری شود؛ فقط زیرساخت bootstrap و inventory اولیه در محدوده است.

## خروجی مورد انتظار

Theme-only و Plugin-only بدون محصول دیگر boot شوند؛ co-install نسخه یکسان collision یا duplicate boot نداشته باشد؛ هیچ companion plugin نصب نشود.

## خارج از محدوده
- هر Feature،provider،platform یا refactor خارج از همین Task ID.
- deploy/publish،پرداخت واقعی،تغییر Production و تغییر داده مشتری.

## Preconditions
- Status باید READY باشد؛ TODO مجوز اجرا نیست.
- Dependencyها: P04-WPPLUGIN-CODE-003
- git status و baseline پیش از تغییر ثبت شوند.

## Allowed files/directories

- `wordpress/packages/carmilla-core/**`
- فایل‌های میزبان مرتبط در `wordpress/carmilla-theme/**` و `wordpress/carmilla-bridge/**`
- `wordpress/**/tests/**` و `tools/test-env/**`
- `wordpress/build-theme-zip.sh` و `wordpress/build-bridge-zip.sh`
- `docs/evidence/P04-WPPLUGIN-CODE-004/**` و وضعیت همین کارت در `docs/**`

## Forbidden actions
- حذف/overwrite تغییرات کاربر،git reset/checkout،ارتقای dependency یا تغییر contract خارج Scope.
- ثبت credential،داده واقعی مشتری یا داده سلامت در repo/Evidence.
- عملیات Production یا migration تخریبی.

## مراحل پیاده‌سازی

1. inventory کلاس، تابع، hook و مسیر بارگذاری فعلی را ثبت کن.
2. بسته kernel و loader دو میزبان را طبق ADR بساز.
3. اسکریپت‌های بسته‌بندی هر دو ZIP را به همان منبع kernel متصل کن.
4. هر ZIP خروجی را در WordPress تمیز نصب و حالت هم‌زمان نسخه یکسان را تست کن.

## Automated tests با command و expected result
- Command: در محیط WordPress CI/container، lint و test محدود به Scope را اجرا کن.
- Expected: activation/install و تست مرتبط exit code 0؛ نبود PHP محلی مجوز تیک‌زدن نیست.
- معیار اختصاصی: Theme-only،Bridge-only و both بدون collision و duplicate bootstrap فعال شوند.

## Manual tests با environment/data/steps/expected

- کجا: فهرست پوسته‌ها/افزونه‌ها و صفحه وضعیت kernel در WordPress آزمایشی.
- چگونه: هر ZIP را جدا نصب و فعال کنید؛ سپس هر دو را هم‌زمان فعال کنید.
- معیار موفقیت: محصول دیگر شرط فعال‌شدن نباشد؛ kernel یک بار boot شود و افزونه همراهی خودکار نصب نشود.
- نسخه محیط و ZIP، داده مصنوعی، نام آزمونگر، تاریخ و نتیجه واقعی ثبت شود؛ تغییر UI/شبکه/مهاجرت تا تأیید انسانی `AWAITING_MANUAL_QA` می‌ماند.

## Acceptance Criteria

- [ ] هر دو ZIP kernel داخلی از یک سورس دارند.
- [ ] نصب مستقل بدون collision و companion plugin اثبات شده است.
- [ ] co-install نسخه یکسان یک boot دارد.
- [ ] استخراج دامنه و arbitration نسخه‌های متفاوت به کارت‌های بعد محدود مانده است.

## Security/Privacy/Migration checks
- Secret،Token،PII،PHI یا داده مشتری در source،log و Evidence ثبت نشود.
- برای API/write path،authorization و ownership بررسی شود.
- برای migration،forward fix/rollback و backup بررسی شود.

## Evidence
- مسیر: docs/evidence/P04-WPPLUGIN-CODE-004/
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
