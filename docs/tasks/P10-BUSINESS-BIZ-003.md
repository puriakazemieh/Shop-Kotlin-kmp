# P10-BUSINESS-BIZ-003 — قیمت‌گذاری قابلیت، اپ‌ساز و هزینه تحویل هر SKU

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
P10-BUSINESS-BIZ-003

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
- Phase/Area/Type: P10 / BUSINESS / BIZ
- Priority/Risk/Size: P0/MEDIUM / M
- Owner: HUMAN
- Completion authority: BOTH
- Depends on: P10-BUSINESS-BIZ-002
- Blocks: P10-WPTHEME-DOC-004
- Requirement source: Master checklist row P10-BUSINESS-BIZ-003 و Source audit بخش BUSINESS
- مرجع تغییر دامنه: [تعریف محصولات مستقل](../INDEPENDENT_PRODUCTS_SPEC_FA.md) و [ADR-006](../architecture/adr/ADR-006-INDEPENDENT-PRODUCTS-AND-ENTITLEMENTS.md)؛ برای همین قابلیت و کار باقی‌مانده.

## هدف قابل اندازه‌گیری
قیمت launch و حد تخفیف با هزینه پشتیبانی هر feature، ساخت هر target، signing، storage، بازار و تمدید محاسبه شود؛ قیمت بدون داده تثبیت نشود.

## خروجی مورد انتظار
margin و break-even برای base، تک‌قابلیت و bundle؛ خرید Builder مجوز خودکار دامنه یا همه platformها نسازد.

## خارج از محدوده
- هر Feature،provider،platform یا refactor خارج از همین Task ID.
- deploy/publish،پرداخت واقعی،تغییر Production و تغییر داده مشتری.

## Preconditions
- Status باید READY باشد؛ TODO مجوز اجرا نیست.
- Dependencyها: P10-BUSINESS-BIZ-002
- git status و baseline پیش از تغییر ثبت شوند.

## Allowed files/directories
- docs/**
- اگر مسیر لازم خارج از این فهرست بود،Task را BLOCKED کن و Scope بخواه.

## Forbidden actions
- حذف/overwrite تغییرات کاربر،git reset/checkout،ارتقای dependency یا تغییر contract خارج Scope.
- ثبت credential،داده واقعی مشتری یا داده سلامت در repo/Evidence.
- عملیات Production یا migration تخریبی.

## مراحل پیاده‌سازی
1. مرجع محصولات مستقل، ADR-006 و ردیف Master مربوط را همراه dependencyهای همین کارت بخوان.
2. baseline و مسیر فعلی همین قابلیت را ثبت کن؛ موضوع خارج از Scope یا بزرگ‌تر از M را قبل از اجرا به child Task محدود تقسیم کن.
3. قرارداد/مستند این کارت را با موارد زیر تطبیق و تصمیم‌های باز را ownerدار ثبت کن: قیمت launch و حد تخفیف با هزینه پشتیبانی هر feature، ساخت هر target، signing، storage، بازار و تمدید محاسبه شود؛ قیمت بدون داده تثبیت نشود.
4. معیار اختصاصی را با Evidence قابل بازتولید بررسی کن: margin و break-even برای base، تک‌قابلیت و bundle؛ خرید Builder مجوز خودکار دامنه یا همه platformها نسازد.
5. گزارش را با artifact/SKU/backend/host مرتبط ثبت کن؛ کار UI/network/migration تا تأیید انسانی AWAITING_MANUAL_QA بماند؛ به کارت بعدی نرو.

## Automated tests با command و expected result
- تست خودکار لازم نیست؛ reviewer انسانی باید صحت Evidence و خروجی را بررسی کند.
- معیار اختصاصی: margin و break-even برای base، تک‌قابلیت و bundle؛ خرید Builder مجوز خودکار دامنه یا همه platformها نسازد.

## Manual tests با environment/data/steps/expected
- کجا: خروجی مستند/ماتریس/گزارش همین کارت در docs و Evidence مربوط به artifact مشخص.
- چگونه: reviewer مسئول، سطرهای هدف این کارت را با SKU، قرارداد و شواهد واقعی تطبیق دهد؛ مورد تأییدنشده را همراه owner/blocker ثبت کند.
- معیار موفقیت: margin و break-even برای base، تک‌قابلیت و bundle؛ خرید Builder مجوز خودکار دامنه یا همه platformها نسازد.
- بازبینی سند به معنی تست دستی محصول یا مجوز انتشار نیست؛ authority همین کارت و Gateهای لازم حفظ شوند.
- reviewer، تاریخ، نسخه سند/artifact و نتیجه PASS/FAIL/BLOCKED ثبت شود.

## Acceptance Criteria
- [ ] margin و break-even برای base، تک‌قابلیت و bundle؛ خرید Builder مجوز خودکار دامنه یا همه platformها نسازد.
- [ ] Scope خارج از Allowed files/directories گسترش نیافته است.
- [ ] تست خودکار/بازبینی لازم واقعاً اجرا و نتیجه ثبت شده است.
- [ ] اگر تست دستی لازم است،Evidence انسانی ثبت شده یا Status برابر AWAITING_MANUAL_QA است.

## Security/Privacy/Migration checks
- Secret،Token،PII،PHI یا داده مشتری در source،log و Evidence ثبت نشود.
- برای API/write path،authorization و ownership بررسی شود.
- برای migration،forward fix/rollback و backup بررسی شود.

## Evidence
- مسیر: docs/evidence/P10-BUSINESS-BIZ-003/
- baseline commit/build،command/cwd/exit code،test report،screenshot redacted و reviewer را ثبت کن.

## Rollback
- روش بازگشت کم‌خطر یا forward-fix پیش از تغییر ثبت شود.
- Migration/Payment/Secret/Health بدون backup و تأیید انسانی DONE نمی‌شود.

## Completion record
- Started at: 2026-09-06
- Completed at: 2026-09-06
- Changed files: docs/evidence/P10-BUSINESS-BIZ-003/summary.md
- Commands and exit codes: N/A
- Manual tester/date/result: Pending Human QA / 2026-09-06 / AWAITING_MANUAL_QA
- Evidence paths: docs/evidence/P10-BUSINESS-BIZ-003/summary.md
- Remaining risks/blockers: Awaiting final pricing sign-off
- Final status: AWAITING_MANUAL_QA
