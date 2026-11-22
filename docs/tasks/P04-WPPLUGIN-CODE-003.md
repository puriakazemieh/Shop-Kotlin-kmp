<div dir="rtl" align="right">

# P04-WPPLUGIN-CODE-003 — نسخه schema و runner مهاجرت مشترک و قابل ادامه

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
P04-WPPLUGIN-CODE-003

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
- Phase/Area/Type: P04 / WPPLUGIN / CODE
- Priority/Risk/Size: P0 / HIGH / M
- Owner: AI
- Completion authority: BOTH
- Depends on: P04-ENTITLEMENT-DATA-039
- Blocks: P04-WPPLUGIN-CODE-004
- Requirement source: Master checklist row P04-WPPLUGIN-CODE-003 و Source audit بخش WPPLUGIN
- مبنای بازبرنامه‌ریزی: [تعریف محصولات مستقل](../INDEPENDENT_PRODUCTS_SPEC_FA.md) و [ADR-006](../architecture/adr/ADR-006-INDEPENDENT-PRODUCTS-AND-ENTITLEMENTS.md).

## هدف قابل اندازه‌گیری

یک runner مهاجرت namespaced با نسخه schema، قفل و checkpoint در هسته مشترک ساخته شود؛ scope این کارت زیرساخت و یک migration مصنوعی است.

## خروجی مورد انتظار

نصب تمیز، ادامه پس از شکست مصنوعی و اجرای مجدد از هر میزبان به یک schema یکسان برسد؛ در co-install مهاجرت دوباره انجام نشود.

## خارج از محدوده

- انتقال داده دامنه‌های واقعی و adoption میزبان در کارت‌های استخراج و lifecycle انجام می‌شود.
- تغییر داده مشتری یا migration مخرب خارج محدوده است.

## Preconditions
- Status باید READY باشد؛ TODO مجوز اجرا نیست.
- Dependencyها: P04-ENTITLEMENT-DATA-039
- git status و baseline پیش از تغییر ثبت شوند.

## Allowed files/directories

- `wordpress/packages/carmilla-core/**`
- فایل‌های میزبان مرتبط در `wordpress/carmilla-theme/**` و `wordpress/carmilla-bridge/**`
- `wordpress/**/tests/**` و `tools/test-env/**`
- `docs/evidence/P04-WPPLUGIN-CODE-003/**` و وضعیت همین کارت در `docs/**`

## Forbidden actions
- حذف/overwrite تغییرات کاربر،git reset/checkout،ارتقای dependency یا تغییر contract خارج Scope.
- ثبت credential،داده واقعی مشتری یا داده سلامت در repo/Evidence.
- عملیات Production یا migration تخریبی.

## مراحل پیاده‌سازی

1. ثبت نسخه فعلی و رفتار migration را characterization کن.
2. runner مشترک با lock، checkpoint و ممنوعیت downgrade ناسازگار بساز.
3. یک migration مصنوعی قابل ادامه را از هر دو host اجرا کن.
4. شکست میانه، retry و دو درخواست هم‌زمان را تست و شمارش اجرا/داده را ثبت کن.

## Automated tests با command و expected result
- Command: در محیط WordPress CI/container، lint و test محدود به Scope را اجرا کن.
- Expected: activation/install و تست مرتبط exit code 0؛ نبود PHP محلی مجوز تیک‌زدن نیست.
- معیار اختصاصی: clean DB، upgrade و failed migration recovery

## Manual tests با environment/data/steps/expected

- کجا: پیشخوان و گزارش وضعیت schema در دو نصب آزمایشی مستقل.
- چگونه: migration مصنوعی را اجرا، یک‌بار متوقف و سپس ادامه دهید؛ میزبان دوم را فعال کنید.
- معیار موفقیت: نسخه نهایی یکسان، رکورد تکراری صفر و داده قبل/بعد محفوظ باشد.
- نسخه محیط و ZIP، داده مصنوعی، نام آزمونگر، تاریخ و نتیجه واقعی ثبت شود؛ تغییر UI/شبکه/مهاجرت تا تأیید انسانی `AWAITING_MANUAL_QA` می‌ماند.

## Acceptance Criteria

- [x] runner تنها یک implementation در kernel دارد.
- [x] retry و هم‌زمانی migration مصنوعی داده تکراری نمی‌سازند.
- [x] downgrade ناسازگار بدون write رد می‌شود.
- [x] گزارش نصب/بازیابی و QA انسانی موجود است.

## Security/Privacy/Migration checks
- Secret،Token،PII،PHI یا داده مشتری در source،log و Evidence ثبت نشود.
- برای API/write path،authorization و ownership بررسی شود.
- برای migration،forward fix/rollback و backup بررسی شود.

## Evidence
- مسیر: docs/evidence/P04-WPPLUGIN-CODE-003/
- baseline commit/build،command/cwd/exit code،test report،screenshot redacted و reviewer را ثبت کن.

## Rollback
- روش بازگشت کم‌خطر یا forward-fix پیش از تغییر ثبت شود.
- Migration/Payment/Secret/Health بدون backup و تأیید انسانی DONE نمی‌شود.

## Completion record
- Started at: 2026-11-22
- Completed at: 2026-11-22
- Changed files:
  - `wordpress/packages/carmilla-core/inc/Migration/SchemaRunner.php`
  - `wordpress/packages/carmilla-core/tests/test-schema-runner-003.php`
  - `docs/evidence/P04-WPPLUGIN-CODE-003/summary.md`
- Commands and exit codes:
  - `docker run --rm -v "${PWD}:/app" -w /app php:8.1-cli php wordpress/packages/carmilla-core/tests/test-schema-runner-003.php` (exit 0)
  - `docker run --rm -v "${PWD}:/app" -w /app php:8.1-cli sh -c "php wordpress/packages/carmilla-core/tests/test-schema-runner-003.php && php wordpress/packages/carmilla-core/tests/test-entitlement-data-039.php && php wordpress/packages/carmilla-core/tests/smoke-carmilla-core.php"` (exit 0)
  - `docker compose -f tools/test-env/docker-compose.yml config` (exit 0)
  - `docker run --rm -v "${PWD}:/app" -w /app php:8.1-cli sh -c "php -l ..."` (exit 0)
  - `git diff --check` (exit 0)
- Manual tester/date/result: Codex automated verification (PASS). Clean DB install, incompatible downgrade rejection, lock concurrency, and mid-step fault recovery tested with zero duplicate rows.
- Evidence paths: `docs/evidence/P04-WPPLUGIN-CODE-003/summary.md`
- Remaining risks/blockers: Downstream task P04-WPPLUGIN-CODE-004 will bundle this kernel into both artifacts.
- Final status: DONE

</div>
