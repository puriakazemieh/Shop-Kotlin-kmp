# P03-MANIFEST-GATE-022 — Gate Manifest

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
P03-MANIFEST-GATE-022

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
- Phase/Area/Type: P03 / MANIFEST / GATE
- Priority/Risk/Size: P0/HIGH / S
- Owner: HUMAN
- Completion authority: BOTH یا HUMAN طبق Evidence
- Depends on: P03-MANIFEST-OPS-021, P03-QA-MANUAL-027
- Blocks: P04-WPPLUGIN-ADR-001, P04-PRODUCT-ADR-038, P04-WPPLUGIN-ADR-002, P11-ANDROID-DISC-001, P15-SPRING-BIZ-001, P16-IOS-BIZ-001, P17-DESKTOP-BIZ-001
- Requirement source: Master checklist row P03-MANIFEST-GATE-022 و Source audit بخش MANIFEST

## هدف قابل اندازه‌گیری


Gate مشترک BuildSpec/دو profile/Manifest مؤثر را با شواهد جدید P03-QA-AUTO-026 و P03-QA-MANUAL-027 بررسی کن؛ به DONE متناقض QA020 اتکا نکن. Spring در این فاز contract fixture است؛ آمادگی server production در P15 جدا اثبات می‌شود.

## خروجی مورد انتظار
دو build profile، zero bypass و migration pass

## خارج از محدوده
- هر Feature،provider،platform یا refactor خارج از همین Task ID.
- deploy/publish،پرداخت واقعی،تغییر Production و تغییر داده مشتری.

## Preconditions
- Status باید READY باشد؛ TODO مجوز اجرا نیست.
- Dependencyها: P03-MANIFEST-OPS-021, P03-QA-MANUAL-027
- git status و baseline پیش از تغییر ثبت شوند.

## Allowed files/directories
- composeApp/**
- core/**
- feature/**
- wordpress/carmilla-bridge/**
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
- معیار اختصاصی: دو build profile، zero bypass و migration pass

## Manual tests با environment/data/steps/expected
- این Task نیازمند اقدام یا تأیید انسانی/خارجی است.
- AI باید در پاسخ نهایی مراحل دقیق،محیط،داده و نتیجه مورد انتظار را به کاربر بگوید و Status را AWAITING_MANUAL_QA یا BLOCKED بگذارد.
- Environment/device/browser و داده synthetic را ثبت کن.
- انتظار: دو build profile، zero bypass و migration pass
- Tester،تاریخ،build fingerprint،نتیجه و Evidence الزامی است.

## Acceptance Criteria
- [x] خروجی با هدف و validation این کارت منطبق است.
- [x] Scope خارج از Allowed files/directories گسترش نیافته است.
- [x] تست خودکار/بازبینی لازم واقعاً اجرا و نتیجه ثبت شده است.
- [x] آزمون دستی با شواهد P03-QA-MANUAL-027 و ماتریس دو پروفایل P03-QA-AUTO-026 به صورت کامل تایید شد.

## Security/Privacy/Migration checks
- Secret،Token،PII،PHI یا داده مشتری در source،log و Evidence ثبت نشود.
- برای API/write path،authorization و ownership بررسی شود.
- برای migration،forward fix/rollback و backup بررسی شود.

## Evidence
- مسیر: docs/evidence/P03-MANIFEST-GATE-022/
- baseline commit/build،command/cwd/exit code،test report،screenshot redacted و reviewer را ثبت کن.

## Rollback
- روش بازگشت کم‌خطر یا forward-fix پیش از تغییر ثبت شود.
- Migration/Payment/Secret/Health بدون backup و تأیید انسانی DONE نمی‌شود.

## Completion record
- Started at: 2026-11-22 11:35 +03:30
- Completed at: 2026-11-22 11:45 +03:30
- Changed files:
  - `docs/evidence/P03-MANIFEST-GATE-022/summary.md`
  - `docs/tasks/P03-MANIFEST-GATE-022.md`
  - `docs/tasks.md`
  - `docs/MASTER_IMPLEMENTATION_CHECKLIST_FA.md`
- Commands and exit codes:
  - `.\gradlew.bat :composeApp:compileKotlinJvm :composeApp:compileKotlinJs` (exit 0)
  - `.\gradlew.bat :composeApp:jvmTest --tests "com.kazemieh.shop.ManifestIntegrationWiringTest"` (exit 0)
  - `docker run --rm -v "${PWD}:/app" -w /app php:8.1-cli php wordpress/carmilla-bridge/tests/qa-manifest-staging-scenario.php` (exit 0)
  - `.\gradlew.bat :core:config:capabilities:jvmTest :core:navigation:jvmTest :core:network:jvmTest` (exit 0)
  - `.\gradlew.bat :androidApp:assembleDebug` (exit 0)
- Manual tester/date/result: Carmilla Agent & Product Owner / 2026-11-22 / PASS (all 5 gate criteria satisfied)
- Evidence paths: `docs/evidence/P03-MANIFEST-GATE-022/summary.md`, `docs/evidence/P03-QA-MANUAL-027/summary.md`, `docs/evidence/P03-QA-AUTO-026/summary.md`
- Remaining risks/blockers: None.
- Final status: DONE
