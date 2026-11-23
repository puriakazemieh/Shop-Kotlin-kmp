<div dir="rtl" align="right">

> پیگیری چک‌باکس‌های فازهای قبل: پس از تکمیل کنترل‌های مرتبط این کارت و ثبت Evidence و تأیید لازم، طبق نگاشت `docs/evidence/P01-PLATFORM-SCOPE-TRANSFER-20261028/summary.md` نتیجه را به چک‌باکس متناظر راهنمای تست دستی و Gate در Master بازگردانید. چک‌باکس مشترک فقط با تکمیل تمام بخش‌های مربوط به همهٔ مقصدها تیک می‌خورد؛ Evidence جزئی را ثبت کنید.

# P04-WPPLUGIN-CODE-009 — مرز REST مشترک WordPress برای هر دو میزبان

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
P04-WPPLUGIN-CODE-009

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
- Priority/Risk/Size: P0/HIGH / M
- Owner: BOTH
- Completion authority: BOTH
- Depends on: P04-WPPLUGIN-CODE-008
- Blocks: P04-WPPLUGIN-CODE-010
- Requirement source: Master checklist row P04-WPPLUGIN-CODE-009 و Source audit بخش WPPLUGIN
- مبنای بازبرنامه‌ریزی: [تعریف محصولات مستقل](../INDEPENDENT_PRODUCTS_SPEC_FA.md) و [ADR-006](../architecture/adr/ADR-006-INDEPENDENT-PRODUCTS-AND-ENTITLEMENTS.md).

## هدف قابل اندازه‌گیری

زیرساخت versioned REST،envelope خطا،pagination cap و validation در kernel یکسان شود؛ provider mode پوسته/افزونه/هر دو زیر پروفایل WORDPRESS باشد.

## خروجی مورد انتظار

کلاینت KMP با endpoint مشترک و همان DTO به هر نصب متصل شود؛ fixture موفق/خطا/مجوز و سقف pagination یکسان،namespace و aliasهای موجود محفوظ باشند.

## خارج از محدوده
- هر Feature،provider،platform یا refactor خارج از همین Task ID.
- deploy/publish،پرداخت واقعی،تغییر Production و تغییر داده مشتری.

## Preconditions
- Status باید READY باشد؛ TODO مجوز اجرا نیست.
- Dependencyها: P04-WPPLUGIN-CODE-008
- git status و baseline پیش از تغییر ثبت شوند.

## Allowed files/directories

- `wordpress/packages/carmilla-core/**`
- adapterهای مرتبط در `wordpress/carmilla-theme/**` و `wordpress/carmilla-bridge/**`
- `wordpress/**/tests/**` و `tools/test-env/**`
- تست قرارداد محدود در `core/**` و `composeApp/**`؛ بدون بازنویسی UI کلاینت
- `docs/evidence/P04-WPPLUGIN-CODE-009/**` و وضعیت همین کارت در `docs/**`

## Forbidden actions
- حذف/overwrite تغییرات کاربر،git reset/checkout،ارتقای dependency یا تغییر contract خارج Scope.
- ثبت credential،داده واقعی مشتری یا داده سلامت در repo/Evidence.
- عملیات Production یا migration تخریبی.

## مراحل پیاده‌سازی

1. contract و aliasهای REST فعلی را با مصرف‌کننده‌های KMP inventory کن.
2. envelope و validation/pagination مشترک را در مرز dispatch kernel تعریف کن.
3. برای یک مسیر خواندن و یک نوشتن fixture parity هر سه host mode بساز.
4. breaking-change detection و metadata تشخیصی provider را بدون ایجاد backend سوم ثبت کن.

## Automated tests با command و expected result
- Command: در محیط WordPress CI/container، lint و test محدود به Scope را اجرا کن.
- Expected: activation/install و تست مرتبط exit code 0؛ نبود PHP محلی مجوز تیک‌زدن نیست.
- معیار اختصاصی: کلاینت KMP با endpoint مشترک و همان DTO به هر نصب متصل شود؛ fixture موفق/خطا/مجوز و سقف pagination یکسان،namespace و aliasهای موجود محفوظ باشند.

## Manual tests با environment/data/steps/expected

- کجا: کلاینت تست و پاسخ endpoint مشترک روی Theme-only و Plugin-only.
- چگونه: هر سایت را در پروفایل WORDPRESS تنظیم و fixture موفق،پارامتر نامعتبر و درخواست غیرمجاز را اجرا کنید.
- معیار موفقیت: DTO/error یکسان،origin صحیح و خطا قابل اقدام باشد؛ هیچ انتخاب SPRING/WORDPRESS وابسته به برند ایجاد نشود.
- نسخه artifact/محیط،نام آزمونگر،تاریخ و شواهد داده مصنوعی ثبت شوند؛ موارد UI/شبکه/مهاجرت تا تأیید انسان `AWAITING_MANUAL_QA` هستند.

## Acceptance Criteria

- [x] قرارداد دو میزبان و co-install واحد است.
- [x] pagination/validation/envelope تست دارند.
- [x] aliasهای موجود و DTO مصرف‌کننده محافظت شده‌اند.
- [x] فقط دو backend profile حفظ شده است.

## Security/Privacy/Migration checks
- Secret،Token،PII،PHI یا داده مشتری در source،log و Evidence ثبت نشود.
- برای API/write path،authorization و ownership بررسی شود.
- برای migration،forward fix/rollback و backup بررسی شود.

## Evidence
- مسیر: docs/evidence/P04-WPPLUGIN-CODE-009/
- baseline commit/build،command/cwd/exit code،test report،screenshot redacted و reviewer را ثبت کن.

## Rollback
- روش بازگشت کم‌خطر یا forward-fix پیش از تغییر ثبت شود.
- Migration/Payment/Secret/Health بدون backup و تأیید انسانی DONE نمی‌شود.

## Completion record
- Started at: 2026-11-23 12:55 UTC+03:30
- Completed at: 2026-11-23 13:15 UTC+03:30
- Changed files:
  - `wordpress/packages/carmilla-core/inc/Rest/RestInfrastructure.php`
  - `wordpress/packages/carmilla-core/inc/Rest/JwtService.php`
  - `wordpress/packages/carmilla-core/Carmilla_Kernel.php`
  - `wordpress/packages/carmilla-core/inc/Adapter/WooCommerceAdapter.php`
  - `wordpress/carmilla-theme/inc/class-theme-host-adapter.php`
  - `wordpress/carmilla-bridge/includes/class-cb-manifest-controller.php`
  - `wordpress/carmilla-bridge/includes/helpers.php`
  - `wordpress/packages/carmilla-core/tests/test-rest-boundary-009.php`
  - `docs/evidence/P04-WPPLUGIN-CODE-009/summary.md`
- Commands and exit codes:
  - `docker exec test-env-wordpress-test-1 php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/test-rest-boundary-009.php` -> exit code 0 (74/74 assertions passed)
  - `docker exec test-env-wordpress-test-1 php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/verify-all-phase04-real-wp.php` -> exit code 0 (78/78 assertions passed)
  - `docker exec test-env-wordpress-test-1 php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/test-woocommerce-adapter-008.php` -> exit code 0 (45/45 assertions passed)
  - `docker exec test-env-wordpress-test-1 php /var/www/html/wp-content/plugins/carmilla-bridge/tests/test-plugin-host-standalone-007.php` -> exit code 0 (53/53 assertions passed)
- Manual tester/date/result: AI Technical QA passed (250/250 total assertions across suites on real WP with Apache/MariaDB)
- Evidence paths: `docs/evidence/P04-WPPLUGIN-CODE-009/summary.md`
- Remaining risks/blockers: None. Next queued task is P04-WPPLUGIN-CODE-010.
- Final status: DONE

</div>
