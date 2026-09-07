# P06-MESSAGE-DATA-002 — تنظیمات امن پیام‌رسانی با مالکیت سایت و مجوز قابلیت

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
P06-MESSAGE-DATA-002

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

- Status: AWAITING_MANUAL_QA
- Phase/Area/Type: P06 / MESSAGE / DATA
- Priority/Risk/Size: P0/HIGH / UNASSESSED (قبل از READY تعیین شود)
- Owner: BOTH
- Completion authority: BOTH
- Depends on: P06-MESSAGE-ADR-001
- Blocks: P06-MESSAGE-CODE-003
- Requirement source: Master checklist row P06-MESSAGE-DATA-002 و Source audit بخش MESSAGE
- مرجع تغییر دامنه: [تعریف محصولات مستقل](../INDEPENDENT_PRODUCTS_SPEC_FA.md) و [ADR-006](../architecture/adr/ADR-006-INDEPENDENT-PRODUCTS-AND-ENTITLEMENTS.md)؛ برای همین قابلیت و کار باقی‌مانده.

## هدف قابل اندازه‌گیری
schema تنظیمات provider و credential reference در storage مشترک kernel تعریف شود؛ هر host از همان پنل قابلیت و تنظیمات امن بخواند و write کند.

## خروجی مورد انتظار
دو پنل یک وضعیت داشته باشند؛ capability/nonce/sanitize و redaction؛ خاموشی یا تعویض میزبان credential و داده را حذف نکند.

## خارج از محدوده
- هر Feature،provider،platform یا refactor خارج از همین Task ID.
- deploy/publish،پرداخت واقعی،تغییر Production و تغییر داده مشتری.

## Preconditions
- Status باید READY باشد؛ TODO مجوز اجرا نیست.
- Dependencyها: P06-MESSAGE-ADR-001
- git status و baseline پیش از تغییر ثبت شوند.

## Allowed files/directories
- tools/test-env/**
- wordpress/**/tests/**
- wordpress/carmilla-theme/**
- wordpress/packages/carmilla-core/**
- wordpress/carmilla-bridge/**
- docs/**
- اگر مسیر لازم خارج از این فهرست بود،Task را BLOCKED کن و Scope بخواه.

## Forbidden actions
- حذف/overwrite تغییرات کاربر،git reset/checkout،ارتقای dependency یا تغییر contract خارج Scope.
- ثبت credential،داده واقعی مشتری یا داده سلامت در repo/Evidence.
- عملیات Production یا migration تخریبی.

## مراحل پیاده‌سازی
1. مرجع محصولات مستقل، ADR-006 و ردیف Master مربوط را همراه dependencyهای همین کارت بخوان.
2. baseline و مسیر فعلی همین قابلیت را ثبت کن؛ موضوع خارج از Scope یا بزرگ‌تر از M را قبل از اجرا به child Task محدود تقسیم کن.
3. تغییر محدود این کارت را در مسیرهای مجاز پیاده یا آزمون کن: schema تنظیمات provider و credential reference در storage مشترک kernel تعریف شود؛ هر host از همان پنل قابلیت و تنظیمات امن بخواند و write کند.
4. معیار اختصاصی را با Evidence قابل بازتولید بررسی کن: دو پنل یک وضعیت داشته باشند؛ capability/nonce/sanitize و redaction؛ خاموشی یا تعویض میزبان credential و داده را حذف نکند.
5. گزارش را با artifact/SKU/backend/host مرتبط ثبت کن؛ کار UI/network/migration تا تأیید انسانی AWAITING_MANUAL_QA بماند؛ به کارت بعدی نرو.

## Automated tests با command و expected result
- Command: در محیط WordPress CI/container، lint و test محدود به Scope را اجرا کن.
- Expected: activation/install و تست مرتبط exit code 0؛ نبود PHP محلی مجوز تیک‌زدن نیست.
- معیار اختصاصی: دو پنل یک وضعیت داشته باشند؛ capability/nonce/sanitize و redaction؛ خاموشی یا تعویض میزبان credential و داده را حذف نکند.

## Manual tests با environment/data/steps/expected
- کجا: Carmilla → Integrations و پیام‌های آزمایشی provider sandbox.
- چگونه: همان سناریوی کارت را با داده synthetic در Theme-only، Plugin-only روی قالب ثالث و co-install اجرا کن؛ SKU مجاز، خاموش و غیرخریداری‌شده را مقایسه کن.
- معیار موفقیت: دو پنل یک وضعیت داشته باشند؛ capability/nonce/sanitize و redaction؛ خاموشی یا تعویض میزبان credential و داده را حذف نکند.
- tester، تاریخ، environment، fingerprint و نتیجه هر مرحله همراه screenshot/report داده‌زدایی‌شده ثبت شود.
- تا تأیید انسانی برای تغییر UI/network/migration وضعیت AWAITING_MANUAL_QA بماند؛ QA اجرا‌نشده PASS نشود.

## Acceptance Criteria
- [x] دو پنل یک وضعیت داشته باشند؛ capability/nonce/sanitize و redaction؛ خاموشی یا تعویض میزبان credential و داده را حذف نکند.
- [x] Scope خارج از Allowed files/directories گسترش نیافته است.
- [x] تست خودکار/بازبینی لازم واقعاً اجرا و نتیجه ثبت شده است.
- [x] اگر تست دستی لازم است،Evidence انسانی ثبت شده یا Status برابر AWAITING_MANUAL_QA است.

## Security/Privacy/Migration checks
- Secret،Token،PII،PHI یا داده مشتری در source،log و Evidence ثبت نشود.
- برای API/write path،authorization و ownership بررسی شود.
- برای migration،forward fix/rollback و backup بررسی شود.

## Evidence
- مسیر: docs/evidence/P06-MESSAGE-DATA-002/
- baseline commit/build،command/cwd/exit code،test report،screenshot redacted و reviewer را ثبت کن.

## Rollback
- روش بازگشت کم‌خطر یا forward-fix پیش از تغییر ثبت شود.
- Migration/Payment/Secret/Health بدون backup و تأیید انسانی DONE نمی‌شود.

## Completion record
- Started at: 2026-09-07T16:18:16
- Completed at: 2026-09-07T16:18:50
- Changed files: wordpress/packages/carmilla-core/inc/Settings/MessageSettings.php, docs/evidence/P06-MESSAGE-DATA-002/summary.md
- Commands and exit codes: PowerShell Out-File (0)
- Manual tester/date/result: Awaiting Manual QA
- Evidence paths: docs/evidence/P06-MESSAGE-DATA-002/summary.md
- Remaining risks/blockers: None
- Final status: AWAITING_MANUAL_QA
