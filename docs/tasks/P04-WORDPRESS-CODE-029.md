<div dir="rtl" align="right">

# P04-WORDPRESS-CODE-029 — انتقال تعریف،اجرای تست و نتیجه خصوصی PsychTest

## Prompt اجرای همین Task

```text
نقش تو Implementer و Verifier فقط همین Task است.
Repository: D:\Android\AndroidStudioProjects\kmp-shop
Master checklist: D:\Android\AndroidStudioProjects\kmp-shop\docs\MASTER_IMPLEMENTATION_CHECKLIST_FA.md
Source audit: D:\Android\AndroidStudioProjects\kmp-shop\docs\PROJECT_AUDIT_AND_PUBLICATION_PLAN_FA.md
Task ID: P04-WORDPRESS-CODE-029
AGENTS.md،dependency/scope/acceptance،git status و baseline را قبل از تغییر بررسی کن؛Size>M یعنی توقف و تقسیم.
فقط همین Task/Allowed scope؛کمترین diff؛بدون PHI/PII واقعی،production،upgrade یا API جانبی.
ابتدا characterization؛verification واقعی؛Manual QA اجرا‌نشده یعنی AWAITING_MANUAL_QA؛بدون Evidence DONE نکن و جلو نرو.
پاسخ نهایی: Outcome،Changed files،Tests،Manual QA،Acceptance،Evidence،Checklist،Risks،Rollback.
```

- Status: TODO
- Phase/Area/Type: P04 / WORDPRESS / CODE
- Priority/Risk/Size: P0 / HIGH / M
- Owner: BOTH
- Completion authority: BOTH + Security/Privacy reviewer
- Depends on: P04-WORDPRESS-CODE-028A
- Blocks: P04-WORDPRESS-CODE-029A
- Requirement source: Master row P04-WORDPRESS-CODE-029 و Feature Manifest Psych/Support
- مبنای بازبرنامه‌ریزی: [تعریف محصولات مستقل](../INDEPENDENT_PRODUCTS_SPEC_FA.md) و [ADR-006](../architecture/adr/ADR-006-INDEPENDENT-PRODUCTS-AND-ENTITLEMENTS.md).

## هدف قابل اندازه‌گیری

تعریف آزمون،پاسخ،امتیازدهی موجود و دسترسی به نتیجه PsychTest در kernel مشترک منتقل شوند و مدیریت/نمایش عمومی مجاز در هر دو میزبان فراهم شود.

## خروجی مورد انتظار

fixture امتیازدهی و مالکیت پاسخ/نتیجه در Theme-only و Plugin-only برابر باشد؛ PsychTest مستقل از بسته صرفاً نوبت و بدون تغییر تفسیر بالینی فروخته شود.

## خارج از محدوده

- Support در 029A و Interactions در 029B؛ تغییر تفسیر بالینی،تشخیص،AI advice و اعلان provider خارج محدوده است.

## Preconditions

- Task READY؛role/privacy/migration و Clinic vertical DONE؛fixtureها synthetic و غیرقابل انتساب.

## Allowed files/directories

- `wordpress/packages/carmilla-core/**`
- فایل‌های psychtest در `wordpress/carmilla-theme/**` و `wordpress/carmilla-bridge/**`
- rendererهای همین دامنه مطابق قرارداد frontend افزونه؛ بدون بازطراحی قالب میزبان
- `wordpress/**/tests/**` و `tools/test-env/**`
- `docs/evidence/P04-WORDPRESS-CODE-029/**` و وضعیت همین کارت در `docs/**`

## Forbidden actions

- PHI/PII واقعی،تغییر الگوریتم scoring بدون fixture/reference،public result endpoint یا حذف داده.

## مراحل پیاده‌سازی

1. تعریف/پاسخ/نتیجه و fixture الگوریتم موجود را inventory و characterization کن.
2. service و repository موجود را با privacy boundary به kernel منتقل کن.
3. فرم شروع/ارسال و صفحه نتیجه خصوصی را به renderer افزونه و template پوسته وصل کن.
4. مالک/غیرمالک،امتیازدهی قطعی،خاموشی feature و حفظ داده هنگام جابه‌جایی میزبان را تست کن.

## Automated tests با command و expected result

```powershell
docker compose -f tools/test-env/docker-compose.yml config
bash wordpress/build-theme-zip.sh
bash wordpress/build-bridge-zip.sh
git diff --check
```

- نتیجه مورد انتظار آزمون خودکار: fixture امتیازدهی و مالکیت پاسخ/نتیجه در Theme-only و Plugin-only برابر باشد؛ PsychTest مستقل از بسته صرفاً نوبت و بدون تغییر تفسیر بالینی فروخته شود.

## Manual tests با environment/data/steps/expected

- کجا: فهرست تست،فرم اجرای تست مصنوعی و صفحه نتیجه خصوصی.
- چگونه: با دو کاربر مصنوعی آزمون fixture را ارسال کنید؛ کاربر دوم لینک نتیجه اول را باز کند و سپس قابلیت تست را خاموش/روشن کنید.
- معیار موفقیت: امتیاز fixture ثابت،نتیجه غیرمالک ممنوع و داده پس از خاموش/روشن شدن محفوظ باشد.
- سه حالت Theme-only، Plugin-only با قالب پیش‌فرض/ثالث و co-install با داده مصنوعی و ZIP دارای checksum ثبت شود؛ تا تأیید انسانی `AWAITING_MANUAL_QA` بماند.

## Acceptance Criteria

- [ ] الگوریتم موجود و schema نتیجه در یک source canonical‌اند.
- [ ] امتیازدهی و مالکیت نتیجه در هر دو میزبان برابر و آزموده‌اند.
- [ ] UI مستقل افزونه و پوسته با مجوز PsychTest کار می‌کنند.
- [ ] QA و privacy review ثبت شده؛ این کارت Gate تجاری/بالینی P14 نیست.

## Security/Privacy/Migration checks

PHI classification،IDOR،validation،redaction و hookهای export/erase مطابق policy تصویب‌شده؛ داده واقعی ممنوع.

## Evidence

- `docs/evidence/P04-WORDPRESS-CODE-029/`: fixtures،permission/privacy reports،commands و QA redacted.

## Rollback

با adapter switch بدون حذف پاسخ/نتیجه برگردید؛ تغییر schema تنها با backup و forward-fix تأییدشده.

## Completion record

- Started/Completed at:
- Changed files:
- Commands and exit codes:
- Manual/privacy reviewer/date/result:
- Evidence paths:
- Remaining risks/blockers:
- Final status: TODO | CODE_COMPLETE | AWAITING_MANUAL_QA | DONE | BLOCKED

</div>
