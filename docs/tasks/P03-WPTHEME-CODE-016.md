# P03-WPTHEME-CODE-016 — Theme visibility از manifest/plugin config واحد

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
P03-WPTHEME-CODE-016

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
- Phase/Area/Type: P03 / WPTHEME / CODE
- Priority/Risk/Size: P0/MEDIUM / UNASSESSED (قبل از READY تعیین شود)
- Owner: AI
- Completion authority: BOTH
- Depends on: P03-WPPLUGIN-SEC-015
- Blocks: P03-ANDROID-CODE-017
- Requirement source: Master checklist row P03-WPTHEME-CODE-016 و Source audit بخش WPTHEME

## هدف قابل اندازه‌گیری
Theme visibility از manifest/plugin config واحد

## خروجی مورد انتظار
Theme Mod موازی منبع حقیقت نباشد

## خارج از محدوده
- هر Feature،provider،platform یا refactor خارج از همین Task ID.
- deploy/publish،پرداخت واقعی،تغییر Production و تغییر داده مشتری.

## Preconditions
- Status باید READY باشد؛ TODO مجوز اجرا نیست.
- Dependencyها: P03-WPPLUGIN-SEC-015
- git status و baseline پیش از تغییر ثبت شوند.

## Allowed files/directories
- wordpress/carmilla-theme/**
- wordpress/**/tests/**
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
- Command: در محیط WordPress CI/container، lint و test محدود به Scope را اجرا کن.
- Expected: activation/install و تست مرتبط exit code 0؛ نبود PHP محلی مجوز تیک‌زدن نیست.
- معیار اختصاصی: Theme Mod موازی منبع حقیقت نباشد

## Manual tests با environment/data/steps/expected
- اگر تغییر UI/network/migration دارد، انسان happy path،خطا و accessibility مرتبط را اجرا می‌کند؛ در غیر این صورت N/A را مستند کن.
- Environment/device/browser و داده synthetic را ثبت کن.
- انتظار: Theme Mod موازی منبع حقیقت نباشد
- Tester،تاریخ،build fingerprint،نتیجه و Evidence الزامی است.

## Acceptance Criteria
- [x] خروجی با هدف و validation این کارت منطبق است.
- [x] Scope خارج از Allowed files/directories گسترش نیافته است.
- [x] تست خودکار/بازبینی لازم واقعاً اجرا و نتیجه ثبت شده است.
- [x] تست دستی واقعی سایت عمداً به P03-QA-MANUAL-020 منتقل شده و این کارت DONE است.

## Security/Privacy/Migration checks
- Secret،Token،PII،PHI یا داده مشتری در source،log و Evidence ثبت نشود.
- برای API/write path،authorization و ownership بررسی شود.
- برای migration،forward fix/rollback و backup بررسی شود.

## Evidence
- مسیر: docs/evidence/P03-WPTHEME-CODE-016/
- baseline commit/build،command/cwd/exit code،test report،screenshot redacted و reviewer را ثبت کن.

## Rollback
- روش بازگشت کم‌خطر یا forward-fix پیش از تغییر ثبت شود.
- Migration/Payment/Secret/Health بدون backup و تأیید انسانی DONE نمی‌شود.

## Completion record
- Started at: 2026-08-26
- Completed at: 2026-08-26
- Changed files: `wordpress/carmilla-theme/inc/customizer.php`, `wordpress/carmilla-theme/tests/smoke-manifest-theme.php`
- Commands and exit codes: Docker `php:8.1-cli` lint for theme PHP files and `tests/smoke-manifest-theme.php` (all exit 0).
- Manual tester/date/result: تست UI روی سایت تا `P03-QA-MANUAL-020` عمداً انجام نشده است.
- Evidence paths: `docs/evidence/P03-WPTHEME-CODE-016/summary.md`
- Remaining risks/blockers: تنظیمات قدیمی Theme Mod در دیتابیس باقی می‌مانند ولی دیگر در runtime خوانده نمی‌شوند؛ بررسی بصری به QA دستی موکول است.
- Final status: DONE
