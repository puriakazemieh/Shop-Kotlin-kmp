# P13-QA-MANUAL-022 — UAT دانشجو و مدرس در دو ZIP و کلاینت‌های آماده

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
P13-QA-MANUAL-022

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
- Phase/Area/Type: P13 / QA / MANUAL
- Priority/Risk/Size: P0/HIGH / UNASSESSED (قبل از READY تعیین شود)
- Owner: HUMAN
- Completion authority: BOTH یا HUMAN طبق Evidence
- Depends on: P13-QA-AUTO-021
- Blocks: P13-QA-MANUAL-023
- Requirement source: Master checklist row P13-QA-MANUAL-022 و Source audit بخش QA
- مرجع تغییر دامنه: [تعریف محصولات مستقل](../INDEPENDENT_PRODUCTS_SPEC_FA.md) و [ADR-006](../architecture/adr/ADR-006-INDEPENDENT-PRODUCTS-AND-ENTITLEMENTS.md)؛ برای همین قابلیت و کار باقی‌مانده.

## هدف قابل اندازه‌گیری
دوره synthetic را در Theme-only و Plugin-only روی قالب ثالث از فهرست تا درس/ثبت‌نام/پیشرفت اجرا کن؛ PWA/Android و هر target آماده نیز همان flow را مصرف کنند.

## خروجی مورد انتظار
بدون نصب محصول دیگر و بدون اجبار اپ، مسیر سایت کامل باشد؛ backend/target بدون Gate به‌عنوان آماده فروش ثبت نشود؛ screenshots و defect ID.

## خارج از محدوده
- هر Feature،provider،platform یا refactor خارج از همین Task ID.
- deploy/publish،پرداخت واقعی،تغییر Production و تغییر داده مشتری.

## Preconditions
- Status باید READY باشد؛ TODO مجوز اجرا نیست.
- Dependencyها: P13-QA-AUTO-021
- git status و baseline پیش از تغییر ثبت شوند.

## Allowed files/directories
- tools/test-env/**
- wordpress/**/tests/**
- wordpress/carmilla-bridge/**
- wordpress/carmilla-theme/**
- wordpress/packages/carmilla-core/**
- composeApp/**
- core/**
- feature/**
- wordpress/**
- docs/**
- اگر مسیر لازم خارج از این فهرست بود،Task را BLOCKED کن و Scope بخواه.

## Forbidden actions
- حذف/overwrite تغییرات کاربر،git reset/checkout،ارتقای dependency یا تغییر contract خارج Scope.
- ثبت credential،داده واقعی مشتری یا داده سلامت در repo/Evidence.
- عملیات Production یا migration تخریبی.

## مراحل پیاده‌سازی
1. مرجع محصولات مستقل، ADR-006 و ردیف Master مربوط را همراه dependencyهای همین کارت بخوان.
2. baseline و مسیر فعلی همین قابلیت را ثبت کن؛ موضوع خارج از Scope یا بزرگ‌تر از M را قبل از اجرا به child Task محدود تقسیم کن.
3. تغییر محدود این کارت را در مسیرهای مجاز پیاده یا آزمون کن: دوره synthetic را در Theme-only و Plugin-only روی قالب ثالث از فهرست تا درس/ثبت‌نام/پیشرفت اجرا کن؛ PWA/Android و هر target آماده نیز همان flow را مصرف کنند.
4. معیار اختصاصی را با Evidence قابل بازتولید بررسی کن: بدون نصب محصول دیگر و بدون اجبار اپ، مسیر سایت کامل باشد؛ backend/target بدون Gate به‌عنوان آماده فروش ثبت نشود؛ screenshots و defect ID.
5. گزارش را با artifact/SKU/backend/host مرتبط ثبت کن؛ کار UI/network/migration تا تأیید انسانی AWAITING_MANUAL_QA بماند؛ به کارت بعدی نرو.

## Automated tests با command و expected result
- Command baseline: .\gradlew.bat :composeApp:compileKotlinJvm و سپس task هدفی که پس از discovery مشخص می‌شود.
- Command وب در صورت تغییر: .\gradlew.bat :composeApp:compileKotlinJs
- Expected: commandهای محدود به Scope exit code 0 و report ذخیره‌شده داشته باشند.
- معیار اختصاصی: بدون نصب محصول دیگر و بدون اجبار اپ، مسیر سایت کامل باشد؛ backend/target بدون Gate به‌عنوان آماده فروش ثبت نشود؛ screenshots و defect ID.

## Manual tests با environment/data/steps/expected
- کجا: فهرست/درس دوره، پنل مدیر/مدرس و حساب دانشجوی synthetic.
- چگونه: همان سناریوی کارت را با داده synthetic در Theme-only، Plugin-only روی قالب ثالث و co-install اجرا کن؛ SKU مجاز، خاموش و غیرخریداری‌شده را مقایسه کن.
- معیار موفقیت: بدون نصب محصول دیگر و بدون اجبار اپ، مسیر سایت کامل باشد؛ backend/target بدون Gate به‌عنوان آماده فروش ثبت نشود؛ screenshots و defect ID.
- tester، تاریخ، environment، fingerprint و نتیجه هر مرحله همراه screenshot/report داده‌زدایی‌شده ثبت شود.
- تا تأیید انسانی برای تغییر UI/network/migration وضعیت AWAITING_MANUAL_QA بماند؛ QA اجرا‌نشده PASS نشود.

## Acceptance Criteria
- [ ] بدون نصب محصول دیگر و بدون اجبار اپ، مسیر سایت کامل باشد؛ backend/target بدون Gate به‌عنوان آماده فروش ثبت نشود؛ screenshots و defect ID.
- [ ] Scope خارج از Allowed files/directories گسترش نیافته است.
- [ ] تست خودکار/بازبینی لازم واقعاً اجرا و نتیجه ثبت شده است.
- [ ] اگر تست دستی لازم است،Evidence انسانی ثبت شده یا Status برابر AWAITING_MANUAL_QA است.

## Security/Privacy/Migration checks
- Secret،Token،PII،PHI یا داده مشتری در source،log و Evidence ثبت نشود.
- برای API/write path،authorization و ownership بررسی شود.
- برای migration،forward fix/rollback و backup بررسی شود.

## Evidence
- مسیر: docs/evidence/P13-QA-MANUAL-022/
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
