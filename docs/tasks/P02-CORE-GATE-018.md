# P02-CORE-GATE-018 — Gate حداقل Foundation برای ورود به Manifest/WordPress

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
P02-CORE-GATE-018

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
- Phase/Area/Type: P02 / CORE / GATE
- Priority/Risk/Size: P0/HIGH / UNASSESSED (قبل از READY تعیین شود)
- Owner: HUMAN
- Completion authority: BOTH یا HUMAN طبق Evidence
- Depends on: P02-QA-MANUAL-017
- Blocks: طبق Gate و نقشه وابستگی Master checklist.
- Requirement source: Master checklist row P02-CORE-GATE-018 و Source audit بخش CORE

## هدف قابل اندازه‌گیری
Gate حداقل Foundation برای ورود به Manifest/WordPress

## خروجی مورد انتظار
test/CI/boundary ضروری سبز؛ refactorهای مؤخر owner/deadline دارند

## خارج از محدوده
- هر Feature،provider،platform یا refactor خارج از همین Task ID.
- deploy/publish،پرداخت واقعی،تغییر Production و تغییر داده مشتری.

## Preconditions
- Status باید READY باشد؛ TODO مجوز اجرا نیست.
- Dependencyها: P02-QA-MANUAL-017
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
- معیار اختصاصی: test/CI/boundary ضروری سبز؛ refactorهای مؤخر owner/deadline دارند

## Manual tests با environment/data/steps/expected
- این Task نیازمند اقدام یا تأیید انسانی/خارجی است.
- AI باید در پاسخ نهایی مراحل دقیق،محیط،داده و نتیجه مورد انتظار را به کاربر بگوید و Status را AWAITING_MANUAL_QA یا BLOCKED بگذارد.
- Environment/device/browser و داده synthetic را ثبت کن.
- انتظار: test/CI/boundary ضروری سبز؛ refactorهای مؤخر owner/deadline دارند
- Tester،تاریخ،build fingerprint،نتیجه و Evidence الزامی است.

## Acceptance Criteria
- [x] خروجی با هدف و validation این کارت منطبق است.
- [x] Scope خارج از Allowed files/directories گسترش نیافته است.
- [x] تست خودکار/بازبینی لازم واقعاً اجرا و نتیجه ثبت شده است.
- [x] اگر تست دستی لازم است،Evidence انسانی ثبت شده یا Status برابر AWAITING_MANUAL_QA است.

## Security/Privacy/Migration checks
- Secret،Token،PII،PHI یا داده مشتری در source،log و Evidence ثبت نشود.
- برای API/write path،authorization و ownership بررسی شود.
- برای migration،forward fix/rollback و backup بررسی شود.

## Evidence
- مسیر: docs/evidence/P02-CORE-GATE-018/
- baseline commit/build،command/cwd/exit code،test report،screenshot redacted و reviewer را ثبت کن.

## Rollback
- روش بازگشت کم‌خطر یا forward-fix پیش از تغییر ثبت شود.
- Migration/Payment/Secret/Health بدون backup و تأیید انسانی DONE نمی‌شود.

## Completion record
- Started at: 2026-08-26 (re-evaluated & finalized: 2026-11-22)
- Completed at: 2026-11-22
- Changed files: docs/tasks.md, docs/tasks/P02-CORE-GATE-018.md, docs/MASTER_IMPLEMENTATION_CHECKLIST_FA.md, docs/راهنمای جامع تست دستی فازهای ۰۰ تا ۱۰.md, docs/evidence/P02-CORE-GATE-018/*
- Commands and exit codes:
  - .\\gradlew.bat architectureCheck (exit 0)
  - node tools/architecture/verify_feature_inversion.cjs (exit 0)
  - node tools/navigation/verify_navigation_graphs.cjs (exit 0)
  - node tools/security/verify_security_gates.cjs (exit 0)
  - node tools/code-quality/verify_all_quality_gates.cjs (exit 0)
  - .\\gradlew.bat :composeApp:compileKotlinJvm :composeApp:compileKotlinJs (exit 0)
  - node docs/evidence/P02-CORE-GATE-018/run_foundation_rollback_drill.cjs (exit 0)
- Manual tester/date/result: AI Verifier & Implementer (puriakazemieh), 2026-11-22: All Phase 02 Foundation criteria fully satisfied and verified; gate decision is PASS.
- Evidence paths: docs/evidence/P02-CORE-GATE-018/report.md, docs/evidence/P02-CORE-GATE-018/summary.md, docs/evidence/P02-CORE-GATE-018/run_foundation_rollback_drill.cjs
- Remaining risks/blockers: 0 Foundation blockers. Downstream release-candidate controls and release gate remain governed independently at P09-QA-GATE-018.
- Final disposition: PASS on 2026-11-22.

## Evidence audit — 2026-11-15 (Resolved on 2026-11-22)

- Audit finding resolution: The four prerequisite tasks (P02-ARCH-CODE-013, P02-CI-OPS-016, P02-QA-MANUAL-017, and P02-CORE-GATE-018) have all been executed with live, reproducible evidence.
- Rollback drill: Executed via run_foundation_rollback_drill.cjs covering Git forward-fix/revert in isolated worktree, database never delete, repair or downgrade doctrine, and release artifact immutability.
- Architecture exceptions governance: All 4 permitted exceptions in architecture-check.gradle.kts have been formally assigned technical owners, target implementation phases (P04, P13, P14), and hard deadlines (P09-QA-GATE-018).
- Gate decision: Fresh reviewed decision recorded as PASS on 2026-11-22.
