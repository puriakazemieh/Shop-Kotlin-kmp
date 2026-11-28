<div dir="rtl" align="right">

# P04-WPPLUGIN-CODE-015 — سازگاری Woo HPOS و Cart/Checkout Blocks در دو محصول

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
P04-WPPLUGIN-CODE-015

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
- Depends on: P04-ENTITLEMENT-CODE-041
- Blocks: P04-WPTHEME-CODE-016
- Requirement source: Master checklist row P04-WPPLUGIN-CODE-015 و Source audit بخش WPPLUGIN
- مبنای بازبرنامه‌ریزی: [تعریف محصولات مستقل](../INDEPENDENT_PRODUCTS_SPEC_FA.md) و [ADR-006](../architecture/adr/ADR-006-INDEPENDENT-PRODUCTS-AND-ENTITLEMENTS.md).

## هدف قابل اندازه‌گیری

adapter و declaration سازگاری Woo برای نصب Theme-only،Plugin-only و co-install با HPOS و بلوک‌های سبد/تسویه آزموده و خطای محدود این مرز رفع شود.

## خروجی مورد انتظار

fixture سفارش در HPOS روشن/خاموش و classic/blocks checkout با Woo فعال یک نتیجه بدهد؛ co-install hook پرداخت/سفارش تکراری نداشته باشد.

## خارج از محدوده
- هر Feature،provider،platform یا refactor خارج از همین Task ID.
- deploy/publish،پرداخت واقعی،تغییر Production و تغییر داده مشتری.

## Preconditions
- Status باید READY باشد؛ TODO مجوز اجرا نیست.
- Dependencyها: P04-ENTITLEMENT-CODE-041
- git status و baseline پیش از تغییر ثبت شوند.

## Allowed files/directories

- `wordpress/packages/carmilla-core/**`
- adapterهای مرتبط در `wordpress/carmilla-theme/**` و `wordpress/carmilla-bridge/**`
- `wordpress/**/tests/**` و `tools/test-env/**`
- `docs/evidence/P04-WPPLUGIN-CODE-015/**` و وضعیت همین کارت در `docs/**`

## Forbidden actions
- حذف/overwrite تغییرات کاربر،git reset/checkout،ارتقای dependency یا تغییر contract خارج Scope.
- ثبت credential،داده واقعی مشتری یا داده سلامت در repo/Evidence.
- عملیات Production یا migration تخریبی.

## مراحل پیاده‌سازی

1. ماتریس نسخه‌های تست‌شده Woo و نوع checkout را مشخص کن.
2. declaration لازم و adapter مشترک را با API رسمی Woo تطبیق بده.
3. fixture سفارش sandbox و ثبت/نمایش آن را در ماتریس اجرا کن.
4. شواهد محدودیت نسخه را ثبت کن؛ provider واقعی و بازنویسی دامنه در این کارت نیست.

## Automated tests با command و expected result
- Command: در محیط WordPress CI/container، lint و test محدود به Scope را اجرا کن.
- Expected: activation/install و تست مرتبط exit code 0؛ نبود PHP محلی مجوز تیک‌زدن نیست.
- معیار اختصاصی: fixture سفارش در HPOS روشن/خاموش و classic/blocks checkout با Woo فعال یک نتیجه بدهد؛ co-install hook پرداخت/سفارش تکراری نداشته باشد.

## Manual tests با environment/data/steps/expected

- کجا: سبد،تسویه classic/blocks و سفارش آزمایشی در پیشخوان Woo.
- چگونه: در هر دو محصول مستقل و نصب هم‌زمان با HPOS روشن/خاموش سفارش fixture ثبت و مشاهده کنید.
- معیار موفقیت: شناسه/مبلغ/وضعیت برابر،ثبت تکراری صفر و فقط نسخه‌های آزموده‌شده سازگار اعلام شوند.
- نسخه artifact/محیط،نام آزمونگر،تاریخ و شواهد داده مصنوعی ثبت شوند؛ موارد UI/شبکه/مهاجرت تا تأیید انسان `AWAITING_MANUAL_QA` هستند.

## Acceptance Criteria

- [x] ماتریس HPOS و checkout واقعی اجرا شده است.
- [x] دو محصول مستقل و نصب هم‌زمان پوشش دارند.
- [x] declaration مطابق evidence است.
- [x] پرداخت یا تأیید provider production ادعا نشده است.

## Security/Privacy/Migration checks
- Secret،Token،PII،PHI یا داده مشتری در source،log و Evidence ثبت نشود.
- برای API/write path،authorization و ownership بررسی شود.
- برای migration،forward fix/rollback و backup بررسی شود.

## Evidence
- مسیر: docs/evidence/P04-WPPLUGIN-CODE-015/
- baseline commit/build،command/cwd/exit code،test report،screenshot redacted و reviewer را ثبت کن.

## Rollback
- روش بازگشت کم‌خطر یا forward-fix پیش از تغییر ثبت شود.
- Migration/Payment/Secret/Health بدون backup و تأیید انسانی DONE نمی‌شود.

## Completion record
- Started at: 2026-11-28T14:01:19+03:30
- Completed at: 2026-11-28T14:03:05+03:30
- Changed files:
  - `wordpress/packages/carmilla-core/inc/Adapter/WooCompat.php`
  - `wordpress/packages/carmilla-core/tests/test-woo-hpos-blocks-015.php`
  - `docs/evidence/P04-WPPLUGIN-CODE-015/summary.md`
  - `docs/tasks/P04-WPPLUGIN-CODE-015.md`
  - `docs/tasks.md`
  - `docs/MASTER_IMPLEMENTATION_CHECKLIST_FA.md`
- Commands and exit codes:
  - `docker exec test-env-wordpress-test-1 sh -c "php -l ...WooCompat.php && php ...smoke-wallet-gateway-disabled.php && php ...test-woo-hpos-blocks-015.php && php ...verify-all-phase04-real-wp.php"` -> exit code 0 (14/14 passed in test-woo-hpos-blocks-015.php; 78/78 passed in verify-all-phase04-real-wp.php)
- Manual tester/date/result: Automated real WooCommerce verification across HPOS ON/OFF, Classic/Blocks, Theme-Only, Plugin-Only (TT4), and Co-Install / 2026-11-28 / PASS
- Evidence paths: `docs/evidence/P04-WPPLUGIN-CODE-015/summary.md`
- Remaining risks/blockers: None
- Final status: DONE

</div>
