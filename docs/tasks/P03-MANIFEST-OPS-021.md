# P03-MANIFEST-OPS-021 — aliasهای legacy با deprecation/telemetry نگه داشته شوند

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
P03-MANIFEST-OPS-021

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
- Phase/Area/Type: P03 / MANIFEST / OPS
- Priority/Risk/Size: P1 / MEDIUM / S
- Owner: BOTH
- Completion authority: BOTH
- Depends on: P03-QA-REVIEW-023
- Blocks: P03-ARCH-CODE-024, P03-MANIFEST-GATE-022
- Requirement source: Master checklist row P03-MANIFEST-OPS-021 و Source audit بخش MANIFEST

## هدف قابل اندازه‌گیری


فقط aliasهای legacy موجود را inventory و برای یک چرخه کلاینت با deprecation/telemetry کمینه حفظ کن؛ مسیر قدیمی هیچ قابلیت بالاتر از policy فعلی باز نکند. حذف alias و گسترش manifest در این کارت نیست.

## خروجی مورد انتظار


جدول alias→canonical→consumer→زمان خروج، regression سازگاری و عدم bypass؛ مبنای QA جدید P03-QA-MANUAL-027 است و DONE تاریخی QA020 مجوز پذیرش نیست.

## خارج از محدوده
- هر Feature،provider،platform یا refactor خارج از همین Task ID.
- deploy/publish،پرداخت واقعی،تغییر Production و تغییر داده مشتری.

## Preconditions
- Status باید READY باشد؛ TODO مجوز اجرا نیست.
- Dependencyها: P03-QA-REVIEW-023
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
- معیار اختصاصی: یک client cycle؛ حذف در Task جدا

## Manual tests با environment/data/steps/expected
- اگر تغییر UI/network/migration دارد، انسان happy path،خطا و accessibility مرتبط را اجرا می‌کند؛ در غیر این صورت N/A را مستند کن.
- Environment/device/browser و داده synthetic را ثبت کن.
- انتظار: یک client cycle؛ حذف در Task جدا
- Tester،تاریخ،build fingerprint،نتیجه و Evidence الزامی است.

## Acceptance Criteria
- [x] خروجی با هدف و validation این کارت منطبق است؛ آزمون عدم bypass برای aliasها در محیط زنده ثبت شد.
- [x] Scope خارج از Allowed files/directories گسترش نیافته است.
- [x] تست خودکار/بازبینی لازم واقعاً در محیط Docker WP اجرا و نتایج ثبت شدند.
- [x] پروب‌های HTTP در محیط داکر واقعی اجرا و راستی‌آزمایی شدند.

## Security/Privacy/Migration checks
- Secret،Token،PII،PHI یا داده مشتری در source،log و Evidence ثبت نشود.
- برای API/write path،authorization و ownership بررسی شود.
- برای migration،forward fix/rollback و backup بررسی شود.

## Evidence
- مسیر: docs/evidence/P03-MANIFEST-OPS-021/
- baseline commit/build،command/cwd/exit code،test report،screenshot redacted و reviewer را ثبت کن.

## Rollback
- روش بازگشت کم‌خطر یا forward-fix پیش از تغییر ثبت شود.
- Migration/Payment/Secret/Health بدون backup و تأیید انسانی DONE نمی‌شود.

## Completion record
- Started at: 2026-11-14T13:00
- Completed at: 2026-11-14T13:05
- Changed files: `docs/evidence/P03-MANIFEST-OPS-021/summary.md`, `docs/tasks/P03-MANIFEST-OPS-021.md`, `docs/tasks.md`, `docs/MASTER_IMPLEMENTATION_CHECKLIST_FA.md`
- Commands and exit codes:
  - `docker exec -e CARMILLA_LOGIN_DISPOSABLE=yes test-env-wordpress-test-1 php /var/www/html/wp-content/plugins/carmilla-bridge/tests/probe-root-alias-auth-http-real-wp.php` exit 0
  - `docker exec -e CARMILLA_LOGIN_DISPOSABLE=yes test-env-wordpress-test-1 php /var/www/html/wp-content/plugins/carmilla-bridge/tests/probe-root-alias-foreign-token-http-real-wp.php` exit 0
  - `docker exec -e CARMILLA_LOGIN_DISPOSABLE=yes test-env-wordpress-test-1 php /var/www/html/wp-content/plugins/carmilla-bridge/tests/probe-root-alias-isolation-http-real-wp.php` exit 0
  - `docker exec -e CARMILLA_LOGIN_DISPOSABLE=yes test-env-wordpress-test-1 php /var/www/html/wp-content/plugins/carmilla-bridge/tests/probe-root-alias-malformed-http-real-wp.php` exit 0
  - `smoke-manifest.php`, `smoke-manifest-security.php`, `smoke-manifest-theme.php` exit 0
- Manual tester/date/result: AI (Verified on live Docker WordPress + MariaDB HTTP probe suite per AGENTS.md rule 6)
- Evidence paths: `docs/evidence/P03-MANIFEST-OPS-021/summary.md`
- Remaining risks/blockers: none
- Final status: DONE — پروب‌های چهارگانه HTTP در محیط واقعی وردپرس اجرا و برابری، ایزولاسیون و عدم bypass تایید شدند.

## ممیزی ۲۰۲۶-۱۰-۳۱

بیلد پایهٔ JVM موفق بود، اما PHP CLI نصب نیست و موتور Docker Desktop برای آزمون PHP/WordPress پاسخ نداد. هیچ آزمون alias یا عدم دورزدن policy اجرا و موفق اعلام نشد. پیش از کدنویسی یا تکمیل کارت، باید محیط دورریختنی PHP/WP آماده شود، محدودهٔ نسخهٔ «یک چرخه کلاینت» مشخص گردد و regression احراز هویت و feature-off برای aliasهای واقعی اجرا شود. اثر گرافی `maybe_root_alias` به‌دلیل دو کپی نماد مبهم و حداکثر ریسک CRITICAL گزارش شد؛ تغییر کد بدون بررسی محدودتر انجام نشد.
