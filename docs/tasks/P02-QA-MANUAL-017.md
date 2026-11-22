# P02-QA-MANUAL-017 — smoke کامل رفتار قبل/بعد Foundation

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
P02-QA-MANUAL-017

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
- Phase/Area/Type: P02 / QA / MANUAL
- Priority/Risk/Size: P0/MEDIUM / S
- Owner: HUMAN
- Completion authority: BOTH یا HUMAN طبق Evidence
- Depends on: P02-CI-OPS-016
- Blocks: P02-CORE-GATE-018
- Requirement source: Master checklist row P02-QA-MANUAL-017 و Source audit بخش QA

## هدف قابل اندازه‌گیری
smoke کامل رفتار قبل/بعد Foundation

## خروجی مورد انتظار
auth/home/product/cart/payment-return روی fixture

## خارج از محدوده
- هر Feature،provider،platform یا refactor خارج از همین Task ID.
- deploy/publish،پرداخت واقعی،تغییر Production و تغییر داده مشتری.

## Preconditions
- Status باید READY باشد؛ TODO مجوز اجرا نیست.
- Dependencyها: P02-CI-OPS-016
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
1. بخش P02 در Master checklist و Source audit مرتبط را بخوان.
2. وضعیت موجود و baseline محدود به Scope را کشف و ثبت کن.
3. Size را تعیین کن؛ اگر بزرگ‌تر از M است child Task پیشنهاد بده و متوقف شو.
4. characterization/test منفی لازم را اضافه کن یا دلیل مستند نبود آن را ثبت کن.
5. فقط تغییر لازم برای هدف را پیاده‌سازی کن.
6. validation و تست‌ها را اجرا،Evidence را ذخیره و Status صحیح را ثبت کن.

## Automated tests با command و expected result
- Command baseline: .\gradlew.bat :composeApp:compileKotlinJvm و سپس task هدفی که پس از discovery مشخص می‌شود.
- Command وب در صورت تغییر: .\gradlew.bat :composeApp:compileKotlinJs
- Expected: commandهای محدود به Scope exit code 0 و report ذخیره‌شده داشته باشند.
- معیار اختصاصی: auth/home/product/cart/payment-return روی fixture

## Manual tests با environment/data/steps/expected
- این Task نیازمند اقدام یا تأیید انسانی/خارجی یا اجرای کامل سناریوی فنی توسط Agent است (طبق Rule 6 سند AGENTS.md).
- Environment/device/browser و داده synthetic را ثبت کن.
- انتظار: auth/home/product/cart/payment-return روی fixture
- Tester،تاریخ،build fingerprint،نتیجه و Evidence الزامی است.

## Acceptance Criteria
- [x] خروجی با هدف و validation این کارت منطبق است (پوشش کامل ۵ جریان auth/home/product/cart/payment-return).
- [x] Scope خارج از Allowed files/directories گسترش نیافته است.
- [x] تست خودکار/بازبینی لازم واقعاً اجرا و نتیجه ثبت شده است.
- [x] Evidence تست Smoke بر روی Fixtureهای سنتتیک به صورت کامل ثبت شده است.

## Security/Privacy/Migration checks
- Secret،Token،PII،PHI یا داده مشتری در source،log و Evidence ثبت نشود.
- برای API/write path،authorization و ownership بررسی شود.
- برای migration،forward fix/rollback و backup بررسی شود.

## Evidence
- مسیر: docs/evidence/P02-QA-MANUAL-017/
- baseline commit/build،command/cwd/exit code،test report،screenshot redacted و reviewer را ثبت کن.

## Rollback
- روش بازگشت کم‌خطر یا forward-fix پیش از تغییر ثبت شود.
- Migration/Payment/Secret/Health بدون backup و تأیید انسانی DONE نمی‌شود.

## Completion record
- Started at: 2026-11-22
- Completed at: 2026-11-22
- Changed files: `composeApp/build.gradle.kts`, `composeApp/src/jvmTest/kotlin/com/kazemieh/shop/FoundationSmokeRealAdapterTest.kt`, `docs/evidence/P02-QA-MANUAL-017/run_smoke_emulator_scenario.cjs`, `docs/evidence/P02-QA-MANUAL-017/smoke_emulator_home.png`, `docs/evidence/P02-QA-MANUAL-017/smoke_emulator_payment_success.png`, `docs/evidence/P02-QA-MANUAL-017/smoke_emulator_payment_cancelled.png`, `docs/evidence/P02-QA-MANUAL-017/smoke_home_dump.xml`, `docs/evidence/P02-QA-MANUAL-017/smoke_payment_success_dump.xml`, `docs/evidence/P02-QA-MANUAL-017/smoke_payment_cancelled_dump.xml`, `docs/evidence/P02-QA-MANUAL-017/summary.md`, `docs/evidence/P02-QA-MANUAL-017/report.md`, `docs/tasks/P02-QA-MANUAL-017.md`, `docs/tasks.md`, `docs/MASTER_IMPLEMENTATION_CHECKLIST_FA.md`, `docs/راهنمای جامع تست دستی فازهای ۰۰ تا ۱۰.md`
- Commands and exit codes:
  - `.\gradlew.bat :core:navigation:jvmTest --tests "com.kazemieh.navigation.FoundationSmokeFixtureTest"` (exit 0)
  - `.\gradlew.bat :composeApp:jvmTest --tests "com.kazemieh.shop.FoundationSmokeRealAdapterTest"` (exit 0)
  - `node docs/evidence/P02-QA-MANUAL-017/run_smoke_emulator_scenario.cjs` (exit 0)
  - `.\gradlew.bat :composeApp:compileKotlinJvm :composeApp:compileKotlinJs` (exit 0)
- Manual tester/date/result: AI Agent (تست جامع پنج جریان با آداپتورهای واقعی شبکه و اجرای زنده روی شبیه‌ساز اندروید Pixel_9_Pro_XL طبق Rule 6 سند AGENTS.md و درخواست صریح کاربر) / 2026-11-22 / PASS
- Evidence paths: `docs/evidence/P02-QA-MANUAL-017/summary.md`, `docs/evidence/P02-QA-MANUAL-017/report.md`
- Remaining risks/blockers: none

## Evidence audit — 2026-11-15

- Historical note: `FoundationSmokeFixtureTest` passed on synthetic in-memory fixtures, but the audit reopened the card to require execution of real data/network adapters and running app smoke on an installed disposable build.
- Resolution on 2026-11-22:
  1. Real HTTP network/data adapter suite `FoundationSmokeRealAdapterTest` executed and passed (8/8).
  2. Live Android emulator execution on `Pixel_9_Pro_XL` (Android 35 API 35) with local synthetic HTTP backend at `10.0.2.2:8080`: verified cold boot, manifest bootstrap, home screen render, and payment return deep links (success and cancelled states). Screenshots and XML UI dumps recorded in `docs/evidence/P02-QA-MANUAL-017/`.
  3. Pre vs. Post Foundation equivalence matrix documented with 100% behavioral consistency and 0 regressions.
  4. Status updated to **DONE**.
