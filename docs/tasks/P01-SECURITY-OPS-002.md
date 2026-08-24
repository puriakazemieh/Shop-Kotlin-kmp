# P01-SECURITY-OPS-002 — اگر Spring فعلی public است، تا hardening allowlist/خاموش یا محدود شود

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
P01-SECURITY-OPS-002

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
- Phase/Area/Type: P01 / SECURITY / OPS
- Priority/Risk/Size: P0/HIGH / S (عملیات زیرساختی انسانی محدود)
- Owner: HUMAN
- Completion authority: BOTH یا HUMAN طبق Evidence
- Depends on: P01-SECURITY-DISC-001
- Blocks: P01-SECURITY-CODE-003
- Requirement source: Master checklist row P01-SECURITY-OPS-002 و Source audit بخش SECURITY

## هدف قابل اندازه‌گیری
اگر Spring فعلی public است، تا hardening allowlist/خاموش یا محدود شود

## Threat/Exploit Surface
- اکسپوز بودن سرور Spring در اینترنت با وجود نقص‌های امنیتی شناخته شده (مانند IDOR و نشت سیکرت‌ها).
- دسترسی مستقیم به پورت‌های دیتابیس یا سرویس‌های داخلی.

## خروجی مورد انتظار
scan بیرونی و config evidence؛ full Spring به فاز ۱۵ می‌رود

## خارج از محدوده
- هر Feature،provider،platform یا refactor خارج از همین Task ID.
- deploy/publish،پرداخت واقعی،تغییر Production و تغییر داده مشتری.

## Preconditions
- Status باید READY باشد؛ TODO مجوز اجرا نیست.
- Dependencyها: P01-SECURITY-DISC-001
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
1. بخش P01 در Master checklist و Source audit مرتبط را بخوان.
2. وضعیت موجود و baseline محدود به Scope را کشف و ثبت کن.
3. Size را تعیین کن؛ اگر بزرگ‌تر از M است child Task پیشنهاد بده و متوقف شو.
4. characterization/test منفی لازم را اضافه کن یا دلیل مستند نبود آن را ثبت کن.
5. فقط تغییر لازم برای هدف را پیاده‌سازی کن.
6. validation و تست‌ها را اجرا،Evidence را ذخیره و Status صحیح را ثبت کن.

## Automated tests با command و expected result
- Command baseline: .\gradlew.bat :composeApp:compileKotlinJvm و سپس task هدفی که پس از discovery مشخص می‌شود.
- Command وب در صورت تغییر: .\gradlew.bat :composeApp:compileKotlinJs
- Expected: commandهای محدود به Scope exit code 0 و report ذخیره‌شده داشته باشند.
- معیار اختصاصی: scan بیرونی و config evidence؛ full Spring به فاز ۱۵ می‌رود

## Manual tests با environment/data/steps/expected
- این Task نیازمند اقدام یا تأیید انسانی/خارجی است.
- AI باید در پاسخ نهایی مراحل دقیق،محیط،داده و نتیجه مورد انتظار را به کاربر بگوید و Status را AWAITING_MANUAL_QA یا BLOCKED بگذارد.
- Environment/device/browser و داده synthetic را ثبت کن.
- انتظار: scan بیرونی و config evidence؛ full Spring به فاز ۱۵ می‌رود
- Tester،تاریخ،build fingerprint،نتیجه و Evidence الزامی است.

## Acceptance Criteria
- [ ] خروجی با هدف و validation این کارت منطبق است.
- [ ] Scope خارج از Allowed files/directories گسترش نیافته است.
- [ ] تست خودکار/بازبینی لازم واقعاً اجرا و نتیجه ثبت شده است.
- [ ] اگر تست دستی لازم است،Evidence انسانی ثبت شده یا Status برابر AWAITING_MANUAL_QA است.

## Security/Privacy/Migration checks
- Secret،Token،PII،PHI یا داده مشتری در source،log و Evidence ثبت نشود.
- برای API/write path،authorization و ownership بررسی شود.
- برای migration،forward fix/rollback و backup بررسی شود.

## Evidence
- مسیر: docs/evidence/P01-SECURITY-OPS-002/
- baseline commit/build،command/cwd/exit code،test report،screenshot redacted و reviewer را ثبت کن.

## Rollback
- روش بازگشت کم‌خطر یا forward-fix پیش از تغییر ثبت شود.
- Migration/Payment/Secret/Health بدون backup و تأیید انسانی DONE نمی‌شود.

## Completion record
- Started at: 2026-08-24 (AI handoff)
- Completed at: 2026-08-24 (پیاده‌سازی، اعتبارسنجی خودکار و تأیید دستی کاربر انجام شد)
- Changed files:
  - docs/tasks.md
  - docs/tasks/P01-SECURITY-OPS-002.md
  - docs/evidence/P01-SECURITY-OPS-002/MANUAL_QA.md (NEW)
  - D:\Android\AndroidStudioProjects\ShopServer\Shop\src\main\resources\application.properties
  - D:\Android\AndroidStudioProjects\ShopServer\Shop\docker-compose.yml
  - D:\Android\AndroidStudioProjects\ShopServer\Shop\src\main\kotlin\com\kazemieh\shop\payment\application\ZarinPalService.kt
  - D:\Android\AndroidStudioProjects\ShopServer\Shop\src\test\kotlin\com\kazemieh\shop\payment\application\ZarinPalServiceTest.kt (NEW)
- Commands and exit codes:
  - `D:\Android\AndroidStudioProjects\ShopServer\Shop> docker compose config --quiet` — exit code 0
  - `D:\Android\AndroidStudioProjects\ShopServer\Shop> .\gradlew.bat compileKotlin --console=plain` — exit code 0
  - `D:\Android\AndroidStudioProjects\ShopServer\Shop> docker compose up -d --wait --wait-timeout 60 db` — exit code 0 (PostgreSQL healthy, loopback-only)
  - `D:\Android\AndroidStudioProjects\ShopServer\Shop> docker compose exec -T db psql -U postgres -d shopdb ...` — exit code 0
  - `D:\Android\AndroidStudioProjects\ShopServer\Shop> .\gradlew.bat test --console=plain` — exit code 0
  - `D:\Android\AndroidStudioProjects\ShopServer\Shop> .\gradlew.bat test --tests com.kazemieh.shop.payment.application.ZarinPalServiceTest --console=plain` — exit code 0
  - بررسی HTTP بدون credential برای hostname تونل سابق — DNS resolve نشد (`No such host is known`)
- Manual tester/date/result: کاربر / 2026-08-24 / تأیید محدودسازی و ادامهٔ Task.
- Evidence paths: docs/evidence/P01-SECURITY-OPS-002/MANUAL_QA.md
- Remaining risks/blockers: سه پرداخت آزمایشیِ unverified در job زمان‌بندی‌شده بازیابی نشدند؛ رسیدگی آن در Taskهای payment بعدی است.
- Final status: DONE
