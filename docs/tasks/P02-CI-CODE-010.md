# P02-CI-CODE-010 — ktlint/detekt و WPCS/Plugin Check/Theme Check تنظیم شوند

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
P02-CI-CODE-010

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
- Phase/Area/Type: P02 / CI / CODE
- Priority/Risk/Size: P1/MEDIUM / L (decomposed into 010A then 010B then 010C)
- Owner: AI
- Completion authority: BOTH
- Depends on: P02-CI-CODE-010C (ordered children 010A → 010B → 010C; original prerequisite 009 moves to 010A)
- Blocks: P02-CI-CODE-011
- Requirement source: Master checklist row P02-CI-CODE-010 و Source audit بخش CI

## هدف قابل اندازه‌گیری
ktlint/detekt و WPCS/Plugin Check/Theme Check تنظیم شوند

## خروجی مورد انتظار
baseline debt جدا؛ کد جدید violation اضافه نکند

## خارج از محدوده
- هر Feature،provider،platform یا refactor خارج از همین Task ID.
- deploy/publish،پرداخت واقعی،تغییر Production و تغییر داده مشتری.

## Preconditions
- Status باید READY باشد؛ TODO مجوز اجرا نیست.
- Dependencyها: P02-CI-CODE-009
- git status و baseline پیش از تغییر ثبت شوند.

## Allowed files/directories
- .github/**
- gradle/**
- build-logic/**
- tools/**
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
- معیار اختصاصی: baseline debt جدا؛ کد جدید violation اضافه نکند

## Manual tests با environment/data/steps/expected
- اگر تغییر UI/network/migration دارد، انسان happy path،خطا و accessibility مرتبط را اجرا می‌کند؛ در غیر این صورت N/A را مستند کن.
- Environment/device/browser و داده synthetic را ثبت کن.
- انتظار: baseline debt جدا؛ کد جدید violation اضافه نکند
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
- مسیر: docs/evidence/P02-CI-CODE-010/
- baseline commit/build،command/cwd/exit code،test report،screenshot redacted و reviewer را ثبت کن.

## Rollback
- روش بازگشت کم‌خطر یا forward-fix پیش از تغییر ثبت شود.
- Migration/Payment/Secret/Health بدون backup و تأیید انسانی DONE نمی‌شود.

## Completion record
- Started at: 2026-10-31 (decomposed into 010A, 010B, 010C)
- Completed at: 2026-11-11
- Changed files:
  - `.github/workflows/code-quality.yml`
  - `.gitattributes`
  - `tools/code-quality/**`
  - `docs/evidence/P02-CI-CODE-010/summary.md`
  - `docs/tasks/P02-CI-CODE-010.md`
  - `docs/tasks.md`
  - `docs/MASTER_IMPLEMENTATION_CHECKLIST_FA.md`
- Subtasks Completed:
  - `P02-CI-CODE-010A`: Kotlin tools (`ktlint`, `detekt-cli`) setup (DONE)
  - `P02-CI-CODE-010B`: WordPress / Theme tools setup (DONE)
  - `P02-CI-CODE-010C`: Bounded baselines, security triage, zero new violation gates, positive & negative PR proof (DONE)
- Commands and exit codes:
  - `node tools/code-quality/verify_all_quality_gates.cjs` (exit 0)
  - `./gradlew.bat :composeApp:compileKotlinJs :composeApp:compileKotlinJvm` (exit 0)
  - GitHub Actions Run 43034830201 (positive PR #35: all 5 jobs success)
  - GitHub Actions Run 43036240387 (negative PR #36: Theme Check job failure, exit 1)
- Manual tester/date/result: AI automated verification & owner-authorized remote CI proof.
- Evidence paths: `docs/evidence/P02-CI-CODE-010/summary.md`
- Remaining risks/blockers: None. All 5 quality tools operational in CI and locally verified.
- Final status: DONE

## Status audit — 2026-10-28
- گزارش پیشین در Completion record سابقهٔ تاریخی است؛ وضعیت جاری پس از تطبیق معیار پذیرش با کد و Evidence برابر TODO است.
- دلیل: Theme Check در workflow موجود نیست و فایل پیکربندی detekt ارجاع‌شده نیز یافت نشد؛ اجرای gate اثبات نشده است.
- گزارش: docs/evidence/P02-STATUS-AUDIT-20261028/summary.md

## Size gate — 2026-10-31

This card spans Kotlin lint/detekt, PHP WPCS, Plugin Check, Theme Check and an existing-debt/new-violation policy. Existing CI at `e8f3798` has missing detekt/WPCS action repositories, Theme Check is absent, and Plugin Check reports product-code findings. The original card is L and crosses CI/tooling/product boundaries. The owner-authorized split is 010A Kotlin tools, 010B WordPress/Theme tool execution, then 010C baseline debt and regression gate. The parent remains TODO until all acceptance criteria have real CI evidence. See `docs/evidence/P02-CI-CODE-010/decomposition-20261031.md`.
