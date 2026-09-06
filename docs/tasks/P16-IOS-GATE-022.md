# P16-IOS-GATE-022 — Gate خروجی مستقل iOS و Builder target مربوط

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
P16-IOS-GATE-022

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
- Phase/Area/Type: P16 / IOS / GATE
- Priority/Risk/Size: P0/HIGH / UNASSESSED (قبل از READY تعیین شود)
- Owner: HUMAN
- Completion authority: BOTH یا HUMAN طبق Evidence
- Depends on: P16-IOS-SEC-023
- Blocks: P16-BUILDER-CODE-024
- Requirement source: Master checklist row P16-IOS-GATE-022 و Source audit بخش IOS
- مرجع تغییر دامنه: [تعریف محصولات مستقل](../INDEPENDENT_PRODUCTS_SPEC_FA.md) و [ADR-006](../architecture/adr/ADR-006-INDEPENDENT-PRODUCTS-AND-ENTITLEMENTS.md)؛ برای همین قابلیت و کار باقی‌مانده.

## هدف قابل اندازه‌گیری

iOS مستقل با artifact واقعی و signing/quality/support ارزیابی شود؛ نتیجه به تفکیک backend ثبت شود. بخش WordPress با سه provider mode و بخش Spring با سرور آماده P15 آزمایش می‌شوند. adapter اختیاری Builder بعد از این Gate کارت جدا دارد و پیش‌نیاز این Gate نیست؛ تأیید هر دو backend و همه روش‌های ساخت در P18 اجباری است.

## خروجی مورد انتظار
نتیجه backendها جدا ثبت؛ سطر Spring بدون P15 PASS نشود و تا P18 تکمیل گردد؛ Keychain review و نصب/ارتقا؛ Gate فقط با artifact و حساب مجاز، نه mock runner.

## خارج از محدوده
- هر Feature،provider،platform یا refactor خارج از همین Task ID.
- deploy/publish،پرداخت واقعی،تغییر Production و تغییر داده مشتری.

## Preconditions
- Status باید READY باشد؛ TODO مجوز اجرا نیست.
- Dependencyها: P16-IOS-SEC-023
- git status و baseline پیش از تغییر ثبت شوند.

## Allowed files/directories
- iosApp/**
- composeApp/**
- core/**
- feature/**
- docs/**
- اگر مسیر لازم خارج از این فهرست بود،Task را BLOCKED کن و Scope بخواه.

## Forbidden actions
- حذف/overwrite تغییرات کاربر،git reset/checkout،ارتقای dependency یا تغییر contract خارج Scope.
- ثبت credential،داده واقعی مشتری یا داده سلامت در repo/Evidence.
- عملیات Production یا migration تخریبی.

## مراحل پیاده‌سازی
1. مرجع محصولات مستقل، ADR-006 و ردیف Master مربوط را همراه dependencyهای همین کارت بخوان.
2. baseline و مسیر فعلی همین قابلیت را ثبت کن؛ موضوع خارج از Scope یا بزرگ‌تر از M را قبل از اجرا به child Task محدود تقسیم کن.
3. Gate artifact مستقل را ارزیابی کن؛ adapter Builder پس از Gate اجرا و پذیرش کامل در P18 ثبت می‌شود.
4. معیار اختصاصی را با Evidence قابل بازتولید بررسی کن: نتیجه backendها جدا ثبت؛ سطر Spring بدون P15 PASS نشود و تا P18 تکمیل گردد؛ Keychain review و نصب/ارتقا؛ Gate فقط با artifact و حساب مجاز، نه mock runner.
5. گزارش را با artifact/SKU/backend/host مرتبط ثبت کن؛ کار UI/network/migration تا تأیید انسانی AWAITING_MANUAL_QA بماند؛ به کارت بعدی نرو.

## Automated tests با command و expected result
- Command baseline: .\gradlew.bat :composeApp:compileKotlinJvm و سپس task هدفی که پس از discovery مشخص می‌شود.
- Command وب در صورت تغییر: .\gradlew.bat :composeApp:compileKotlinJs
- Expected: commandهای محدود به Scope exit code 0 و report ذخیره‌شده داشته باشند.
- معیار اختصاصی: نتیجه backendها جدا ثبت؛ سطر Spring بدون P15 PASS نشود و تا P18 تکمیل گردد؛ Keychain review و نصب/ارتقا؛ Gate فقط با artifact و حساب مجاز، نه mock runner.

## Manual tests با environment/data/steps/expected
- کجا: خروجی مستند/ماتریس/گزارش همین کارت در docs و Evidence مربوط به artifact مشخص.
- چگونه: reviewer مسئول، سطرهای هدف این کارت را با SKU، قرارداد و شواهد واقعی تطبیق دهد؛ مورد تأییدنشده را همراه owner/blocker ثبت کند.
- معیار موفقیت: نتیجه backendها جدا ثبت؛ سطر Spring بدون P15 PASS نشود و تا P18 تکمیل گردد؛ Keychain review و نصب/ارتقا؛ Gate فقط با artifact و حساب مجاز، نه mock runner.
- بازبینی سند به معنی تست دستی محصول یا مجوز انتشار نیست؛ authority همین کارت و Gateهای لازم حفظ شوند.
- reviewer، تاریخ، نسخه سند/artifact و نتیجه PASS/FAIL/BLOCKED ثبت شود.

## Acceptance Criteria
- [ ] نتیجه backendها جدا ثبت؛ سطر Spring بدون P15 PASS نشود و تا P18 تکمیل گردد؛ Keychain review و نصب/ارتقا؛ Gate فقط با artifact و حساب مجاز، نه mock runner.
- [ ] Scope خارج از Allowed files/directories گسترش نیافته است.
- [ ] تست خودکار/بازبینی لازم واقعاً اجرا و نتیجه ثبت شده است.
- [ ] اگر تست دستی لازم است،Evidence انسانی ثبت شده یا Status برابر AWAITING_MANUAL_QA است.

## Security/Privacy/Migration checks
- Secret،Token،PII،PHI یا داده مشتری در source،log و Evidence ثبت نشود.
- برای API/write path،authorization و ownership بررسی شود.
- برای migration،forward fix/rollback و backup بررسی شود.

## Evidence
- مسیر: docs/evidence/P16-IOS-GATE-022/
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
