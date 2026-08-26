# P03-QA-MANUAL-020 — toggle واقعی بدون rebuild در WordPress/PWA/client internal

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
P03-QA-MANUAL-020

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
- Phase/Area/Type: P03 / QA / MANUAL
- Priority/Risk/Size: P0/HIGH / UNASSESSED (قبل از READY تعیین شود)
- Owner: HUMAN
- Completion authority: BOTH یا HUMAN طبق Evidence
- Depends on: P03-QA-AUTO-019
- Blocks: P03-MANIFEST-OPS-021
- Requirement source: Master checklist row P03-QA-MANUAL-020 و Source audit بخش QA

## هدف قابل اندازه‌گیری
toggle واقعی بدون rebuild در WordPress/PWA/client internal

## خروجی مورد انتظار
خاموش/روشن، stale/invalid، deep link و process restart

## خارج از محدوده
- هر Feature،provider،platform یا refactor خارج از همین Task ID.
- deploy/publish،پرداخت واقعی،تغییر Production و تغییر داده مشتری.

## Preconditions
- Status باید READY باشد؛ TODO مجوز اجرا نیست.
- Dependencyها: P03-QA-AUTO-019
- git status و baseline پیش از تغییر ثبت شوند.

## Allowed files/directories
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
1. بخش P03 در Master checklist و Source audit مرتبط را بخوان.
2. وضعیت موجود و baseline محدود به Scope را کشف و ثبت کن.
3. Size را تعیین کن؛ اگر بزرگ‌تر از M است child Task پیشنهاد بده و متوقف شو.
4. characterization/test منفی لازم را اضافه کن یا دلیل مستند نبود آن را ثبت کن.
5. فقط تغییر لازم برای هدف را پیاده‌سازی کن.
6. validation و تست‌ها را اجرا،Evidence را ذخیره و Status صحیح را ثبت کن.

## Automated tests با command و expected result
- Command baseline: .\gradlew.bat :composeApp:compileKotlinJvm و سپس task هدفی که پس از discovery مشخص می‌شود.
- Command وب در صورت تغییر: .\gradlew.bat :composeApp:compileKotlinJs
- Expected: commandهای محدود به Scope exit code 0 و report ذخیره‌شده داشته باشند.
- معیار اختصاصی: خاموش/روشن، stale/invalid، deep link و process restart

## Manual tests با environment/data/steps/expected
- این Task نیازمند اقدام یا تأیید انسانی/خارجی است.
- AI باید در پاسخ نهایی مراحل دقیق،محیط،داده و نتیجه مورد انتظار را به کاربر بگوید و Status را AWAITING_MANUAL_QA یا BLOCKED بگذارد.
- Environment/device/browser و داده synthetic را ثبت کن.
- انتظار: خاموش/روشن، stale/invalid، deep link و process restart
- Tester،تاریخ،build fingerprint،نتیجه و Evidence الزامی است.

## Acceptance Criteria
- [ ] خروجی با هدف و validation این کارت منطبق است.
- [x] Scope خارج از Allowed files/directories گسترش نیافته است.
- [x] تست خودکار/بازبینی لازم واقعاً اجرا و نتیجه ثبت شده است.
- [x] تست دستی هنوز اجرا نشده و وضعیت درست `AWAITING_MANUAL_QA` است.

## Security/Privacy/Migration checks
- Secret،Token،PII،PHI یا داده مشتری در source،log و Evidence ثبت نشود.
- برای API/write path،authorization و ownership بررسی شود.
- برای migration،forward fix/rollback و backup بررسی شود.

## Evidence
- مسیر: docs/evidence/P03-QA-MANUAL-020/
- baseline commit/build،command/cwd/exit code،test report،screenshot redacted و reviewer را ثبت کن.

## Rollback
- روش بازگشت کم‌خطر یا forward-fix پیش از تغییر ثبت شود.
- Migration/Payment/Secret/Health بدون backup و تأیید انسانی DONE نمی‌شود.

## Completion record
- Started at: 2026-08-26
- Completed at:
- Changed files: `docs/tasks.md`, `docs/tasks/P03-QA-MANUAL-020.md`, `docs/evidence/P03-QA-MANUAL-020/summary.md`
- Commands and exit codes: prerequisite `:core:navigation:jvmTest`, `:core:config:capabilities:jvmTest`, `:composeApp:compileKotlinJvm`, `:composeApp:compileKotlinJs` all exit 0.
- Manual tester/date/result: منتظر اجرای کاربر روی WordPress/PWA/client internal.
- Evidence paths: `docs/evidence/P03-QA-MANUAL-020/summary.md`
- Remaining risks/blockers: تأیید انسانی خاموش/روشن،stale/invalid،deep link و process restart انجام نشده؛ `P03-MANIFEST-OPS-021` تا این تأیید متوقف است.
- Final status: AWAITING_MANUAL_QA
