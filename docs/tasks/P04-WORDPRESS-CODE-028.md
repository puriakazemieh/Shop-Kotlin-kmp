<div dir="rtl" align="right">

# P04-WORDPRESS-CODE-028 — انتقال معرفی متخصص و زمان‌های قابل ارائه به هسته مشترک

## Prompt اجرای همین Task

```text
نقش تو Implementer و Verifier فقط همین Task است.
Repository: D:\Android\AndroidStudioProjects\kmp-shop
Master checklist: D:\Android\AndroidStudioProjects\kmp-shop\docs\MASTER_IMPLEMENTATION_CHECKLIST_FA.md
Source audit: D:\Android\AndroidStudioProjects\kmp-shop\docs\PROJECT_AUDIT_AND_PUBLICATION_PLAN_FA.md
Task ID: P04-WORDPRESS-CODE-028
قبل از تغییر AGENTS.md،dependency/scope/acceptance،git status و baseline را بررسی کن؛Size>M یعنی توقف و child Task.
فقط همین Task/Allowed scope/کمترین diff؛بدون secret،PHI واقعی،production،payment واقعی،upgrade یا contract جانبی.
ابتدا characterization؛verification واقعی؛Manual QA اجرا‌نشده را تیک نزن؛بدون Evidence DONE نکن و به Task بعدی نرو.
پاسخ نهایی: Outcome،Changed files،Tests،Manual QA،Acceptance،Evidence،Checklist،Risks،Rollback.
```

- Status: TODO
- Phase/Area/Type: P04 / WORDPRESS / CODE
- Priority/Risk/Size: P0 / HIGH / M
- Owner: BOTH
- Completion authority: BOTH + Security/Privacy reviewer
- Depends on: P04-WORDPRESS-CODE-027B
- Blocks: P04-WORDPRESS-CODE-028A
- Requirement source: Master row P04-WORDPRESS-CODE-028 و Feature Manifest Clinic
- مبنای بازبرنامه‌ریزی: [تعریف محصولات مستقل](../INDEPENDENT_PRODUCTS_SPEC_FA.md) و [ADR-006](../architecture/adr/ADR-006-INDEPENDENT-PRODUCTS-AND-ENTITLEMENTS.md).

## هدف قابل اندازه‌گیری

کاتالوگ ارائه‌دهنده خدمت و availability با منطقه زمانی و مدیریت دسترسی در kernel مشترک منتقل شود؛ رزرو/لغو نوبت در 028A جداست.

## خروجی مورد انتظار

صفحه معرفی متخصص و زمان‌های قابل ارائه و پنل مدیریت آن در دو محصول مستقل پاسخ یکسان بدهند؛ خرید معرفی/نوبت نیازمند خرید تست یا پرونده بالینی نباشد.

## خارج از محدوده

- رزرو/لغو/تعارض نوبت در 028A؛ پیام/جلسه/پرونده خصوصی و آمادگی تجاری در P14.
- توصیه پزشکی،provider و داده سلامت واقعی خارج محدوده‌اند.

## Preconditions

- Task READY؛privacy/role/migration foundation DONE؛داده تست کاملاً synthetic.

## Allowed files/directories

- `wordpress/packages/carmilla-core/**`
- فایل‌های clinic/therapist/availability در `wordpress/carmilla-theme/**` و `wordpress/carmilla-bridge/**`
- rendererهای همین دامنه مطابق قرارداد frontend افزونه؛ بدون بازطراحی قالب میزبان
- `wordpress/**/tests/**` و `tools/test-env/**`
- `docs/evidence/P04-WORDPRESS-CODE-028/**` و وضعیت همین کارت در `docs/**`

## Forbidden actions

- PHI/PII واقعی،رزرو/payment production،تغییر retention بدون review یا migration destructive.

## مراحل پیاده‌سازی

1. route/meta/capability معرفی متخصص و availability را جدا از پرونده و نوبت inventory کن.
2. repository/service کاتالوگ و زمان‌بندی را با حفظ شناسه به kernel منتقل کن.
3. صفحه متخصص،فهرست و مدیریت availability دو میزبان را به همین خدمات متصل کن.
4. timezone،بازه نامعتبر،دسترسی غیرمجاز و وضعیت feature بسته/روشن را تست کن.

## Automated tests با command و expected result

```powershell
docker compose -f tools/test-env/docker-compose.yml config
bash wordpress/build-theme-zip.sh
bash wordpress/build-bridge-zip.sh
git diff --check
```

- نتیجه مورد انتظار آزمون خودکار: صفحه معرفی متخصص و زمان‌های قابل ارائه و پنل مدیریت آن در دو محصول مستقل پاسخ یکسان بدهند؛ خرید معرفی/نوبت نیازمند خرید تست یا پرونده بالینی نباشد.

## Manual tests با environment/data/steps/expected

- کجا: فهرست متخصص‌ها،صفحه یک متخصص مصنوعی و فرم مدیریت زمان‌ها.
- چگونه: متخصص با دو بازه زمانی بسازید؛ timezone و دسترسی مدیر/کاربر عادی را بررسی و بسته صرفاً معرفی/نوبت را بدون PsychTest فعال کنید.
- معیار موفقیت: نمایش و زمان‌ها در هر دو میزبان برابر،بازه نامعتبر و ویرایش غیرمجاز رد و هیچ داده پرونده خصوصی نمایان نشود.
- سه حالت Theme-only، Plugin-only با قالب پیش‌فرض/ثالث و co-install با داده مصنوعی و ZIP دارای checksum ثبت شود؛ تا تأیید انسانی `AWAITING_MANUAL_QA` بماند.

## Acceptance Criteria

- [ ] کاتالوگ و availability یک implementation با شناسه ثابت دارند.
- [ ] نمایش عمومی و مدیریت روی هر دو میزبان قابل استفاده‌اند.
- [ ] timezone و authorization منفی آزموده شده‌اند.
- [ ] تفکیک معرفی/نوبت از خدمات خصوصی مطابق کاتالوگ حفظ شده است.

## Security/Privacy/Migration checks

اطلاعات عمومی متخصص از پرونده خصوصی جدا باشد؛ validation زمان و کنترل capability و log بدون PHI رعایت شود.

## Evidence

- `docs/evidence/P04-WORDPRESS-CODE-028/`: state diagram،race/IDOR tests،commands و redacted QA.

## Rollback

به adapter پیشین برگردید؛ شناسه متخصص و availability حفظ و نوبت‌های موجود دست‌کاری نشوند.

## Completion record

- Started/Completed at:
- Changed files:
- Commands and exit codes:
- Manual/privacy reviewer/date/result:
- Evidence paths:
- Remaining risks/blockers:
- Final status: TODO | CODE_COMPLETE | AWAITING_MANUAL_QA | DONE | BLOCKED

</div>
