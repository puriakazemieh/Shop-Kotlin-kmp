<div dir="rtl" align="right">

# P04-WPPLUGIN-ADR-002 — قرارداد هسته مشترک، دو میزبان و انتخاب نسخه سازگار

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
P04-WPPLUGIN-ADR-002

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
- Phase/Area/Type: P04 / WPPLUGIN / ADR
- Priority/Risk/Size: P0 / HIGH / S
- Owner: BOTH
- Completion authority: BOTH
- Depends on: P04-PRODUCT-ADR-038, P03-MANIFEST-GATE-022
- Blocks: P04-ENTITLEMENT-DATA-039
- Requirement source: Master checklist row P04-WPPLUGIN-ADR-002 و Source audit بخش WPPLUGIN
- مبنای بازبرنامه‌ریزی: [تعریف محصولات مستقل](../INDEPENDENT_PRODUCTS_SPEC_FA.md) و [ADR-006](../architecture/adr/ADR-006-INDEPENDENT-PRODUCTS-AND-ENTITLEMENTS.md).

## هدف قابل اندازه‌گیری

مرز بسته‌های هسته مشترک، Theme Host، Plugin Host، نمایش عمومی افزونه، API مشترک و اتصال اپ‌ساز مطابق ADR-006 تصویب شود؛ ترتیب بارگذاری WordPress و authority نسخه/schema دقیق باشد.

## خروجی مورد انتظار

سند تصمیم و نمودار bootstrap نشان دهند هر ZIP بدون محصول دیگر کار می‌کند و نصب هم‌زمان یک هسته سازگار دارد؛ مالکیت داده سایت و مجوز خرید از انتخاب میزبان جدا باشند.

## خارج از محدوده
- هر Feature،provider،platform یا refactor خارج از همین Task ID.
- deploy/publish،پرداخت واقعی،تغییر Production و تغییر داده مشتری.

## Preconditions
- Status باید READY باشد؛ TODO مجوز اجرا نیست.
- Dependencyها: P04-PRODUCT-ADR-038, P03-MANIFEST-GATE-022
- git status و baseline پیش از تغییر ثبت شوند.

## Allowed files/directories

- `docs/architecture/**`
- `docs/contracts/**`
- `docs/evidence/P04-WPPLUGIN-ADR-002/**` و وضعیت همین کارت در `docs/**`

## Forbidden actions
- حذف/overwrite تغییرات کاربر،git reset/checkout،ارتقای dependency یا تغییر contract خارج Scope.
- ثبت credential،داده واقعی مشتری یا داده سلامت در repo/Evidence.
- عملیات Production یا migration تخریبی.

## مراحل پیاده‌سازی

1. زمان بارگذاری افزونه، پوسته و hookهای ثبت CPT/REST را از سورس و WordPress بررسی کن.
2. قرارداد candidate registration، انتخاب نسخه با API/schema و توقف ماژول ناسازگار را بنویس؛ first-loaded یا بیشترین نسخه به‌تنهایی کافی نیست.
3. مرز kernel، تنظیمات ظاهری پوسته، renderer افزونه و shared build adapter را مشخص کن.
4. سناریوی میزبان تنها، هر دو، خاموشی آخرین میزبان و انتقال به میزبان دیگر را با ADR-006 تطبیق بده.
5. تصمیم معماری و پرسش‌های حل‌شده را با نام reviewer ثبت کن؛ اجرای دامنه‌ها و runner واقعی در این کارت نیست.

## Automated tests با command و expected result
- تست خودکار لازم نیست؛ reviewer انسانی باید صحت Evidence و خروجی را بررسی کند.
- معیار اختصاصی: Shared Core/hostها/version authority و منع native build روی WordPress تصویب شده باشد.

## Manual tests با environment/data/steps/expected

- کجا: سند ADR معماری و جدول مالکیت کنار تعریف محصولات مستقل.
- چگونه: سناریوهای Theme-only، Plugin-only و co-install را روی نمودار دنبال و هر write path و مالک schema را بررسی کنید.
- معیار موفقیت: هیچ وابستگی اجباری بین دو محصول، نصب companion/MU-plugin یا مالکیت اختصاصی Bridge برای داده مشترک باقی نماند؛ بازبینی مستند، تأیید محصول اجراشده محسوب نشود.
- نسخه محیط و ZIP، داده مصنوعی، نام آزمونگر، تاریخ و نتیجه واقعی ثبت شود؛ تغییر UI/شبکه/مهاجرت تا تأیید انسانی `AWAITING_MANUAL_QA` می‌ماند.

## Acceptance Criteria

- [ ] kernel، هر دو میزبان، renderer و build adapter مرز روشن دارند.
- [ ] زمان انتخاب نسخه و رفتار mismatch/API/schema مشخص است.
- [ ] استقلال دو ZIP و دوام داده با خاموشی میزبان‌ها تعریف شده است.
- [ ] بازبینی معماری ثبت شده و ADR-005 فقط مرجع تاریخی است.

## Security/Privacy/Migration checks
- Secret،Token،PII،PHI یا داده مشتری در source،log و Evidence ثبت نشود.
- برای API/write path،authorization و ownership بررسی شود.
- برای migration،forward fix/rollback و backup بررسی شود.

## Evidence
- مسیر: docs/evidence/P04-WPPLUGIN-ADR-002/
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
