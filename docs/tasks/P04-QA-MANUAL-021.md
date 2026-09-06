<div dir="rtl" align="right">

# P04-QA-MANUAL-021 — UAT پوسته تنها برای قابلیت‌ها و پنل بیلدر بسته‌شده

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
P04-QA-MANUAL-021

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
- Phase/Area/Type: P04 / QA / MANUAL
- Priority/Risk/Size: P0 / HIGH / M
- Owner: HUMAN
- Completion authority: BOTH یا HUMAN طبق Evidence
- Depends on: P04-QA-AUTO-020
- Blocks: P04-WPPLUGIN-MANUAL-034, P04-WPTHEME-GATE-036
- Requirement source: Master checklist row P04-QA-MANUAL-021 و Source audit بخش QA

## هدف قابل اندازه‌گیری

بدون نصب Carmilla Plugin، نسخه Base و نسخه Booking با Android/PWA Builder را نصب و UI/admin/API قابلیت‌ها را استفاده کن؛ بیلدر در P04 فقط با runner آزمایشی و برچسب روشن است.

## خروجی مورد انتظار

کجا: پوسته و پنل قابلیت‌ها/اپ‌ساز. چگونه: نوبت synthetic ثبت کن؛ قابلیت مجاز را خاموش/روشن؛ target نخریده را امتحان کن. موفقیت: ثبت مستقل،حفظ داده،نبود ماژول انتخاب‌نشده و رد target غیرمجاز؛ تأیید انسانی لازم.

## خارج از محدوده
- هر Feature،provider،platform یا refactor خارج از همین Task ID.
- deploy/publish،پرداخت واقعی،تغییر Production و تغییر داده مشتری.

## Preconditions
- Status باید READY باشد؛ TODO مجوز اجرا نیست.
- Dependencyها: P04-QA-AUTO-020
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

1. قرارداد `docs/INDEPENDENT_PRODUCTS_SPEC_FA.md` و مانیفست بسته را با inventory ZIP همین SKU تطبیق بده؛ نتیجه این کنترل را همراه مراحل زیر ثبت کن.
1. بخش P04 در Master checklist و Source audit مرتبط را بخوان.
2. وضعیت موجود و baseline محدود به Scope را کشف و ثبت کن.
3. Size را تعیین کن؛ اگر بزرگ‌تر از M است child Task پیشنهاد بده و متوقف شو.
4. characterization/test منفی لازم را اضافه کن یا دلیل مستند نبود آن را ثبت کن.
5. فقط تغییر لازم برای هدف را پیاده‌سازی کن.
6. validation و تست‌ها را اجرا،Evidence را ذخیره و Status صحیح را ثبت کن.

## Automated tests با command و expected result
- Command baseline: .\gradlew.bat :composeApp:compileKotlinJvm و سپس task هدفی که پس از discovery مشخص می‌شود.
- Command وب در صورت تغییر: .\gradlew.bat :composeApp:compileKotlinJs
- Expected: commandهای محدود به Scope exit code 0 و report ذخیره‌شده داشته باشند.
- معیار اختصاصی: تمام capabilityهای Theme standalone بدون Bridge UAT شوند.

## Manual tests با environment/data/steps/expected

کجا: پوسته و پنل قابلیت‌ها/اپ‌ساز. چگونه: نوبت synthetic ثبت کن؛ قابلیت مجاز را خاموش/روشن؛ target نخریده را امتحان کن. موفقیت: ثبت مستقل،حفظ داده،نبود ماژول انتخاب‌نشده و رد target غیرمجاز؛ تأیید انسانی لازم.

- محیط staging،داده synthetic،build fingerprint و tester/date/result؛ تا تأیید واقعی AWAITING_MANUAL_QA.

## Acceptance Criteria

- [ ] کجا: پوسته و پنل قابلیت‌ها/اپ‌ساز. چگونه: نوبت synthetic ثبت کن؛ قابلیت مجاز را خاموش/روشن؛ target نخریده را امتحان کن. موفقیت: ثبت مستقل،حفظ داده،نبود ماژول انتخاب‌نشده و رد target غیرمجاز؛ تأیید انسانی لازم.
- [ ] خروجی با هدف و validation این کارت منطبق است.
- [ ] Scope خارج از Allowed files/directories گسترش نیافته است.
- [ ] تست خودکار/بازبینی لازم واقعاً اجرا و نتیجه ثبت شده است.
- [ ] اگر تست دستی لازم است،Evidence انسانی ثبت شده یا Status برابر AWAITING_MANUAL_QA است.

## Security/Privacy/Migration checks
- Secret،Token،PII،PHI یا داده مشتری در source،log و Evidence ثبت نشود.
- برای API/write path،authorization و ownership بررسی شود.
- برای migration،forward fix/rollback و backup بررسی شود.

## Evidence
- مسیر: docs/evidence/P04-QA-MANUAL-021/
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

</div>
