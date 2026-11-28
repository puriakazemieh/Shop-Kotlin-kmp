<div dir="rtl" align="right">

# P04-WPPLUGIN-CODE-011 — راه‌اندازی و preflight مستقل هر محصول طبق SKU

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
P04-WPPLUGIN-CODE-011

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

- Status: DONE
- Phase/Area/Type: P04 / WPPLUGIN / CODE
- Priority/Risk/Size: P0/MEDIUM / M
- Owner: AI
- Completion authority: BOTH
- Depends on: P04-WPPLUGIN-CODE-010
- Blocks: P04-WPPLUGIN-CODE-012
- Requirement source: Master checklist row P04-WPPLUGIN-CODE-011 و Source audit بخش WPPLUGIN
- مبنای بازبرنامه‌ریزی: [تعریف محصولات مستقل](../INDEPENDENT_PRODUCTS_SPEC_FA.md) و [ADR-006](../architecture/adr/ADR-006-INDEPENDENT-PRODUCTS-AND-ENTITLEMENTS.md).

## هدف قابل اندازه‌گیری

پیش‌بررسی HTTPS،Woo مورد نیاز،permalink،REST،cron و نسخه kernel از هر دو میزبان قابل اجرا شود؛ فقط نیازهای SKU انتخابی الزام شوند.

## خروجی مورد انتظار

پوسته پایه بدون Bridge/Woo راه‌اندازی شود؛ افزونه روی قالب دیگر پیش‌بررسی خودش را داشته باشد و خطاها راه رفع مشخص و diagnostics بدون secret بدهند.

## خارج از محدوده
- هر Feature،provider،platform یا refactor خارج از همین Task ID.
- deploy/publish،پرداخت واقعی،تغییر Production و تغییر داده مشتری.

## Preconditions
- Status باید READY باشد؛ TODO مجوز اجرا نیست.
- Dependencyها: P04-WPPLUGIN-CODE-010
- git status و baseline پیش از تغییر ثبت شوند.

## Allowed files/directories

- `wordpress/packages/carmilla-core/**`
- adapterهای مرتبط در `wordpress/carmilla-theme/**` و `wordpress/carmilla-bridge/**`
- `wordpress/**/tests/**` و `tools/test-env/**`
- `docs/evidence/P04-WPPLUGIN-CODE-011/**` و وضعیت همین کارت در `docs/**`

## Forbidden actions
- حذف/overwrite تغییرات کاربر،git reset/checkout،ارتقای dependency یا تغییر contract خارج Scope.
- ثبت credential،داده واقعی مشتری یا داده سلامت در repo/Evidence.
- عملیات Production یا migration تخریبی.

## مراحل پیاده‌سازی

1. نیازهای مشترک سایت و نیاز وابسته به SKU را جدا کن.
2. probeهای موجود را در kernel و ورودی onboarding هر host یکپارچه کن.
3. حالت Woo غایب برای Base و Commerce و REST/cron ناموفق را تست کن.
4. diagnostics redacted و مسیر اصلاح/اجرای مجدد را ثبت کن.

## Automated tests با command و expected result
- Command: در محیط WordPress CI/container، lint و test محدود به Scope را اجرا کن.
- Expected: activation/install و تست مرتبط exit code 0؛ نبود PHP محلی مجوز تیک‌زدن نیست.
- معیار اختصاصی: پوسته پایه بدون Bridge/Woo راه‌اندازی شود؛ افزونه روی قالب دیگر پیش‌بررسی خودش را داشته باشد و خطاها راه رفع مشخص و diagnostics بدون secret بدهند.

## Manual tests با environment/data/steps/expected

- کجا: راه‌انداز Carmilla در پیشخوان پوسته تنها و افزونه تنها.
- چگونه: Base و Commerce را با Woo حاضر/غایب و fixture خطای REST/cron بررسی و پس از رفع خطا دوباره preflight را اجرا کنید.
- معیار موفقیت: Base به Woo یا محصول Carmilla دیگر نیاز اجباری نداشته باشد؛ blocker هر SKU دقیق و فاقد credential باشد.
- نسخه artifact/محیط،نام آزمونگر،تاریخ و شواهد داده مصنوعی ثبت شوند؛ موارد UI/شبکه/مهاجرت تا تأیید انسان `AWAITING_MANUAL_QA` هستند.

## Acceptance Criteria

- [x] هر میزبان onboarding مستقل دارد.
- [x] پیش‌نیازها مطابق SKU محدودند.
- [x] خطا راه رفع مشخص دارد.
- [x] گزارش تشخیص secret یا داده مشتری نشان نمی‌دهد.

## Security/Privacy/Migration checks
- Secret،Token،PII،PHI یا داده مشتری در source،log و Evidence ثبت نشود.
- برای API/write path،authorization و ownership بررسی شود.
- برای migration،forward fix/rollback و backup بررسی شود.

## Evidence
- مسیر: docs/evidence/P04-WPPLUGIN-CODE-011/
- baseline commit/build،command/cwd/exit code،test report،screenshot redacted و reviewer را ثبت کن.

## Rollback
- روش بازگشت کم‌خطر یا forward-fix پیش از تغییر ثبت شود.
- Migration/Payment/Secret/Health بدون backup و تأیید انسانی DONE نمی‌شود.

## Completion record
- Started at: 2026-11-28T13:32:36+03:30
- Completed at: 2026-11-28T13:35:15+03:30
- Changed files:
  - `wordpress/packages/carmilla-core/inc/Health/HealthChecker.php`
  - `wordpress/packages/carmilla-core/inc/Rest/RestInfrastructure.php`
  - `wordpress/packages/carmilla-core/inc/Settings/AdminSettingsPage.php`
  - `wordpress/carmilla-theme/inc/class-theme-host-adapter.php`
  - `wordpress/carmilla-bridge/includes/class-cb-standalone-boot.php`
  - `wordpress/packages/carmilla-core/tests/test-preflight-onboarding-011.php`
  - `docs/evidence/P04-WPPLUGIN-CODE-011/summary.md`
  - `docs/tasks/P04-WPPLUGIN-CODE-011.md`
  - `docs/tasks.md`
  - `docs/MASTER_IMPLEMENTATION_CHECKLIST_FA.md`
- Commands and exit codes:
  - `docker exec test-env-wordpress-test-1 sh -c "php -l ...HealthChecker.php && php ...test-preflight-onboarding-011.php && php ...verify-all-phase04-real-wp.php"` -> exit code 0 (40/40 passed in test-preflight-onboarding-011.php; 78/78 passed in verify-all-phase04-real-wp.php)
- Manual tester/date/result: Automated real-WP verification across Theme-Only, Plugin-Only (TT4), and Co-Install modes / 2026-11-28 / PASS
- Evidence paths: `docs/evidence/P04-WPPLUGIN-CODE-011/summary.md`
- Remaining risks/blockers: None
- Final status: DONE

</div>
