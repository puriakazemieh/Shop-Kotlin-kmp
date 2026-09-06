<div dir="rtl" align="right">

# P04-WPTHEME-CODE-005 — اتصال میزبان پوسته به هسته داخلی بدون Bridge

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
P04-WPTHEME-CODE-005

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
- Phase/Area/Type: P04 / WPTHEME / CODE
- Priority/Risk/Size: P0 / HIGH / M
- Owner: AI
- Completion authority: BOTH
- Depends on: P04-WPPLUGIN-CODE-004
- Blocks: P04-WPPLUGIN-CODE-006
- Requirement source: Master checklist row P04-WPTHEME-CODE-005 و Source audit بخش WPTHEME
- مبنای بازبرنامه‌ریزی: [تعریف محصولات مستقل](../INDEPENDENT_PRODUCTS_SPEC_FA.md) و [ADR-006](../architecture/adr/ADR-006-INDEPENDENT-PRODUCTS-AND-ENTITLEMENTS.md).

## هدف قابل اندازه‌گیری

loader، دسترسی تنظیمات و adapter پایه پوسته به kernel بسته‌بندی‌شده متصل شوند؛ وابستگی به کلاس‌ها و مسیر نصب Bridge در این مرز حذف شود.

## خروجی مورد انتظار

پوسته به‌تنهایی UI پایه و خدمات منتقل‌شده kernel را اجرا کند و templateها از قرارداد host استفاده کنند؛ انتقال همه verticalها در این کارت انجام نشود.

## خارج از محدوده
- هر Feature،provider،platform یا refactor خارج از همین Task ID.
- deploy/publish،پرداخت واقعی،تغییر Production و تغییر داده مشتری.

## Preconditions
- Status باید READY باشد؛ TODO مجوز اجرا نیست.
- Dependencyها: P04-WPPLUGIN-CODE-004
- git status و baseline پیش از تغییر ثبت شوند.

## Allowed files/directories

- `wordpress/packages/carmilla-core/**`
- فایل‌های میزبان مرتبط در `wordpress/carmilla-theme/**` و `wordpress/carmilla-bridge/**`
- `wordpress/**/tests/**` و `tools/test-env/**`
- `wordpress/build-theme-zip.sh`
- `docs/evidence/P04-WPTHEME-CODE-005/**` و وضعیت همین کارت در `docs/**`

## Forbidden actions
- حذف/overwrite تغییرات کاربر،git reset/checkout،ارتقای dependency یا تغییر contract خارج Scope.
- ثبت credential،داده واقعی مشتری یا داده سلامت در repo/Evidence.
- عملیات Production یا migration تخریبی.

## مراحل پیاده‌سازی

1. ارجاع‌های bootstrap/settings پوسته به Bridge را inventory کن.
2. adapter پایه Theme Host را به kernel داخلی متصل کن.
3. تنظیمات ظاهر را Theme-owned و وضعیت قابلیت را site-owned نگه دار.
4. نصب Theme-only و نصب با Bridge را با fixture پایه مقایسه کن.

## Automated tests با command و expected result
- Command: در محیط WordPress CI/container، lint و test محدود به Scope را اجرا کن.
- Expected: activation/install و تست مرتبط exit code 0؛ نبود PHP محلی مجوز تیک‌زدن نیست.
- معیار اختصاصی: Theme Host از kernel بسته‌بندی‌شده استفاده و بدون Bridge boot شود.

## Manual tests با environment/data/steps/expected

- کجا: صفحه خانه و تنظیمات پوسته روی سایت آزمایشی بدون Carmilla Bridge.
- چگونه: پوسته ZIP را فعال، تنظیم ظاهر را ذخیره و fixture پایه را نمایش دهید؛ سپس Bridge را اضافه کنید.
- معیار موفقیت: ظاهر و داده پایه باقی بماند؛ نبود Bridge خطا یا درخواست نصب آن نسازد.
- نسخه محیط و ZIP، داده مصنوعی، نام آزمونگر، تاریخ و نتیجه واقعی ثبت شود؛ تغییر UI/شبکه/مهاجرت تا تأیید انسانی `AWAITING_MANUAL_QA` می‌ماند.

## Acceptance Criteria

- [ ] loader/adapter به مسیر Bridge وابسته نیست.
- [ ] تنظیمات ظاهر با تنظیمات قابلیت مخلوط نشده است.
- [ ] نصب مستقل و هم‌زمان fixture پایه parity دارند.
- [ ] QA دستی مسیر پایه ثبت شده است.

## Security/Privacy/Migration checks
- Secret،Token،PII،PHI یا داده مشتری در source،log و Evidence ثبت نشود.
- برای API/write path،authorization و ownership بررسی شود.
- برای migration،forward fix/rollback و backup بررسی شود.

## Evidence
- مسیر: docs/evidence/P04-WPTHEME-CODE-005/
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
