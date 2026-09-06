<div dir="rtl" align="right">

# P04-WORDPRESS-CODE-027 — انتقال کاتالوگ دوره و محتوای درس به هسته مشترک

## Prompt اجرای همین Task

```text
نقش تو Implementer و Verifier فقط همین Task است.
Repository: D:\Android\AndroidStudioProjects\kmp-shop
Master checklist: D:\Android\AndroidStudioProjects\kmp-shop\docs\MASTER_IMPLEMENTATION_CHECKLIST_FA.md
Source audit: D:\Android\AndroidStudioProjects\kmp-shop\docs\PROJECT_AUDIT_AND_PUBLICATION_PLAN_FA.md
Task ID: P04-WORDPRESS-CODE-027
AGENTS.md،dependency،scope،acceptance،git status و baseline را قبل از تغییر بررسی کن؛Size>M یعنی توقف و child Task.
فقط همین Task و Allowed scope؛کمترین diff؛بدون upgrade/API جانبی/secret/داده واقعی/production.
ابتدا characterization test؛verification واقعی؛Manual QA اجرا‌نشده یعنی AWAITING_MANUAL_QA؛بدون Evidence DONE نکن و به Task بعدی نرو.
پاسخ نهایی: Outcome،Changed files،Tests،Manual QA،Acceptance،Evidence،Checklist،Risks،Rollback.
```

- Status: TODO
- Phase/Area/Type: P04 / WORDPRESS / CODE
- Priority/Risk/Size: P0 / HIGH / M
- Owner: BOTH
- Completion authority: BOTH؛ Manual QA الزامی
- Depends on: P04-WORDPRESS-CODE-026B
- Blocks: P04-WORDPRESS-CODE-027A
- Requirement source: Master row P04-WORDPRESS-CODE-027 و Feature Manifest Academy
- مبنای بازبرنامه‌ریزی: [تعریف محصولات مستقل](../INDEPENDENT_PRODUCTS_SPEC_FA.md) و [ADR-006](../architecture/adr/ADR-006-INDEPENDENT-PRODUCTS-AND-ENTITLEMENTS.md).

## هدف قابل اندازه‌گیری

مدیریت Course و lesson/content و مسیر فهرست/جزئیات دوره در دو میزبان از هسته واحد استفاده کنند؛ این کارت فقط کاتالوگ و محتوای درس را منتقل می‌کند.

## خروجی مورد انتظار

دوره و ساختار درس با شناسه ثابت در Theme-only و Plugin-only نمایش/مدیریت شوند؛ محتوای محافظت‌شده بدون مجوز آموزشی قبلی افشا نشود؛ ثبت‌نام/پیشرفت و آزمون جدا بمانند.

## خارج از محدوده

- ثبت‌نام/پیشرفت در 027A و آزمون/تکلیف/گواهی در 027B؛ تکمیل تجاری LMS در P13.
- عضویت/باندل،provider پرداخت،DRM و طراحی مجدد خارج این slice هستند.

## Preconditions

- Task READY؛Shared Core و content/store vertical DONE؛LMS capability contract freeze.

## Allowed files/directories

- `wordpress/packages/carmilla-core/**`
- فایل‌های academy/course/lesson در `wordpress/carmilla-theme/**` و `wordpress/carmilla-bridge/**`
- rendererهای همین دامنه مطابق قرارداد frontend افزونه؛ بدون بازطراحی قالب میزبان
- `wordpress/**/tests/**` و `tools/test-env/**`
- `docs/evidence/P04-WORDPRESS-CODE-027/**` و وضعیت همین کارت در `docs/**`

## Forbidden actions

- تغییر payment/media contract خارج Scope،دسترسی دوره بدون enrollment یا ثبت داده learner واقعی.

## مراحل پیاده‌سازی

1. CPT/meta/routeهای کاتالوگ و درس را با fixture دوره و دو درس inventory کن.
2. خدمات مدیریت کاتالوگ/محتوا و adapterهای هر دو host را منتقل کن.
3. صفحه فهرست و جزئیات دوره را در renderer افزونه و template پوسته وصل کن؛ جزئیات انتشار از کاتالوگ قابلیت خوانده شود.
4. CRUD مدیر/مدرس مجاز،دسترسی غیرمجاز،نمایش مهمان و alias مسیر قبلی را آزمون کن.

## Automated tests با command و expected result

```powershell
docker compose -f tools/test-env/docker-compose.yml config
bash wordpress/build-theme-zip.sh
bash wordpress/build-bridge-zip.sh
git diff --check
```

- نتیجه مورد انتظار آزمون خودکار: دوره و ساختار درس با شناسه ثابت در Theme-only و Plugin-only نمایش/مدیریت شوند؛ محتوای محافظت‌شده بدون مجوز آموزشی قبلی افشا نشود؛ ثبت‌نام/پیشرفت و آزمون جدا بمانند.

## Manual tests با environment/data/steps/expected

- کجا: فهرست و صفحه دوره و مدیریت درس روی پوسته تنها و افزونه با قالب دیگر.
- چگونه: یک دوره مصنوعی با درس عمومی و محافظت‌شده بسازید؛ با مدیر و مهمان نمایش/ویرایش را امتحان و قابلیت آموزش را خاموش کنید.
- معیار موفقیت: فهرست و جزئیات مجاز برابر باشد؛ مهمان به درس خصوصی دسترسی نداشته باشد؛ خاموشی داده را حذف نکند.
- سه حالت Theme-only، Plugin-only با قالب پیش‌فرض/ثالث و co-install با داده مصنوعی و ZIP دارای checksum ثبت شود؛ تا تأیید انسانی `AWAITING_MANUAL_QA` بماند.

## Acceptance Criteria

- [ ] Course و lesson/content یک implementation دارند.
- [ ] صفحات فهرست/جزئیات و مدیریت روی هر دو میزبان کار می‌کنند.
- [ ] حریم محتوای محافظت‌شده و شناسه/aliasهای قبلی محفوظ‌اند.
- [ ] این انتقال پایه با Gate فروش LMS در P13 اشتباه نشده است.

## Security/Privacy/Migration checks

least privilege،محافظت محتوای خصوصی درس،validation و migration غیرمخرب شناسه‌ها بررسی شود.

## Evidence

- `docs/evidence/P04-WORDPRESS-CODE-027/`: inventory،state tests،role matrix،commands و QA.

## Rollback

adapter قبلی قابل بازگشت باشد؛ رکورد دوره،درس،ثبت‌نام و پیشرفت موجود حذف یا بازنویسی مخرب نشوند.

## Completion record

- Started/Completed at:
- Changed files:
- Commands and exit codes:
- Manual tester/date/result:
- Evidence paths:
- Remaining risks/blockers:
- Final status: TODO | CODE_COMPLETE | AWAITING_MANUAL_QA | DONE | BLOCKED

</div>
