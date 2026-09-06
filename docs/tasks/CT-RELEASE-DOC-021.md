# CT-RELEASE-DOC-021 — سیاست انتشار و ارتقای محصول و SKU مستقل

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
CT-RELEASE-DOC-021

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
- Phase/Area/Type: CONTROL / RELEASE / DOC
- Priority/Risk/Size: P1/MEDIUM / UNASSESSED (قبل از READY تعیین شود)
- Owner: BOTH
- Completion authority: BOTH
- Depends on: ندارد؛ اولین Task صف یا Control مستقل است.
- Blocks: طبق Gate و نقشه وابستگی Master checklist.
- Requirement source: Master checklist row CT-RELEASE-DOC-021 و Source audit بخش RELEASE
- مرجع تغییر دامنه: [تعریف محصولات مستقل](../INDEPENDENT_PRODUCTS_SPEC_FA.md) و [ADR-006](../architecture/adr/ADR-006-INDEPENDENT-PRODUCTS-AND-ENTITLEMENTS.md)؛ برای همین قابلیت و کار باقی‌مانده.

## هدف قابل اندازه‌گیری
docs/delivery/RELEASE_POLICY.md streams/channels/version/approval/rollback و upgrade بسته/مجوز را به تفکیک محصول، feature، backend و target تعریف کند.

## خروجی مورد انتظار
Gate هر ترکیب پیش از فروش؛ نسخه ناسازگار و downgrade schema مهار؛ پایان build credit داده یا اپ تحویل‌شده را حذف نکند.

## خارج از محدوده
- هر Feature،provider،platform یا refactor خارج از همین Task ID.
- deploy/publish،پرداخت واقعی،تغییر Production و تغییر داده مشتری.

## Preconditions
- Status باید READY باشد؛ TODO مجوز اجرا نیست.
- Dependencyها: ندارد؛ اولین Task صف یا Control مستقل است.
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
3. قرارداد/مستند این کارت را با موارد زیر تطبیق و تصمیم‌های باز را ownerدار ثبت کن: docs/delivery/RELEASE_POLICY.md streams/channels/version/approval/rollback و upgrade بسته/مجوز را به تفکیک محصول، feature، backend و target تعریف کند.
4. معیار اختصاصی را با Evidence قابل بازتولید بررسی کن: Gate هر ترکیب پیش از فروش؛ نسخه ناسازگار و downgrade schema مهار؛ پایان build credit داده یا اپ تحویل‌شده را حذف نکند.
5. گزارش را با artifact/SKU/backend/host مرتبط ثبت کن؛ کار UI/network/migration تا تأیید انسانی AWAITING_MANUAL_QA بماند؛ به کارت بعدی نرو.

## Automated tests با command و expected result
- تست خودکار لازم نیست؛ reviewer انسانی باید صحت Evidence و خروجی را بررسی کند.
- معیار اختصاصی: Gate هر ترکیب پیش از فروش؛ نسخه ناسازگار و downgrade schema مهار؛ پایان build credit داده یا اپ تحویل‌شده را حذف نکند.

## Manual tests با environment/data/steps/expected
- کجا: خروجی مستند/ماتریس/گزارش همین کارت در docs و Evidence مربوط به artifact مشخص.
- چگونه: reviewer مسئول، سطرهای هدف این کارت را با SKU، قرارداد و شواهد واقعی تطبیق دهد؛ مورد تأییدنشده را همراه owner/blocker ثبت کند.
- معیار موفقیت: Gate هر ترکیب پیش از فروش؛ نسخه ناسازگار و downgrade schema مهار؛ پایان build credit داده یا اپ تحویل‌شده را حذف نکند.
- بازبینی سند به معنی تست دستی محصول یا مجوز انتشار نیست؛ authority همین کارت و Gateهای لازم حفظ شوند.
- reviewer، تاریخ، نسخه سند/artifact و نتیجه PASS/FAIL/BLOCKED ثبت شود.

## Acceptance Criteria
- [ ] Gate هر ترکیب پیش از فروش؛ نسخه ناسازگار و downgrade schema مهار؛ پایان build credit داده یا اپ تحویل‌شده را حذف نکند.
- [ ] Scope خارج از Allowed files/directories گسترش نیافته است.
- [ ] تست خودکار/بازبینی لازم واقعاً اجرا و نتیجه ثبت شده است.
- [ ] اگر تست دستی لازم است،Evidence انسانی ثبت شده یا Status برابر AWAITING_MANUAL_QA است.

## Security/Privacy/Migration checks
- Secret،Token،PII،PHI یا داده مشتری در source،log و Evidence ثبت نشود.
- برای API/write path،authorization و ownership بررسی شود.
- برای migration،forward fix/rollback و backup بررسی شود.

## Evidence
- مسیر: docs/evidence/CT-RELEASE-DOC-021/
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
