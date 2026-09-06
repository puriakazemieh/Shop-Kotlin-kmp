<div dir="rtl" align="right">

# P04-WORDPRESS-CODE-026 — انتقال نوشته، برگه و رسانه به هسته مشترک

## Prompt اجرای همین Task

```text
نقش تو Implementer و Verifier فقط همین Task است.
Repository: D:\Android\AndroidStudioProjects\kmp-shop
Master checklist: D:\Android\AndroidStudioProjects\kmp-shop\docs\MASTER_IMPLEMENTATION_CHECKLIST_FA.md
Source audit: D:\Android\AndroidStudioProjects\kmp-shop\docs\PROJECT_AUDIT_AND_PUBLICATION_PLAN_FA.md
Task ID: P04-WORDPRESS-CODE-026
قبل از تغییر AGENTS.md،Task/dependency/scope/acceptance،git status و baseline را بخوان/اجرا کن.
اگر Size بزرگ‌تر از M شد اجرا نکن و child Task پیشنهاد بده.
فقط همین Task،کمترین diff و Allowed scope؛dependency/API جانبی،secret،داده واقعی،production و deploy ممنوع.
ابتدا characterization test؛همه verificationها واقعی؛تست دستی اجرا‌نشده تیک نخورد و Status AWAITING_MANUAL_QA باشد.
بدون Evidence DONE نکن؛فقط همین Task را به‌روزرسانی کن و به Task بعدی نرو.
شرایط توقف: تداخل کاربر،baseline failure،contract نامشخص،migration مخرب یا نیاز خارج Scope.
پاسخ نهایی: Outcome،Changed files،Tests،Manual QA،Acceptance،Evidence،Checklist،Risks،Rollback.
```

- Status: TODO
- Phase/Area/Type: P04 / WORDPRESS / CODE
- Priority/Risk/Size: P0 / HIGH / M
- Owner: BOTH
- Completion authority: BOTH؛ Manual QA الزامی
- Depends on: P04-WPPLUGIN-CODE-045
- Blocks: P04-WORDPRESS-CODE-026A
- Requirement source: Master row P04-WORDPRESS-CODE-026 و `plans/002-shared-wordpress-feature-kernel.md`
- مبنای بازبرنامه‌ریزی: [تعریف محصولات مستقل](../INDEPENDENT_PRODUCTS_SPEC_FA.md) و [ADR-006](../architecture/adr/ADR-006-INDEPENDENT-PRODUCTS-AND-ENTITLEMENTS.md).

## هدف قابل اندازه‌گیری

خواندن/نوشتن نوشته، برگه و رسانه از خدمات canonical هسته در هر دو میزبان انجام شود؛ شناسه‌ها و قرارداد محتوای موجود حفظ شوند.

## خروجی مورد انتظار

fixture نوشته/برگه/رسانه از مدیریت و frontend هر میزبان و API مشترک پاسخ یکسان داشته باشد؛ permission،اعتبارسنجی رسانه و عدم ثبت تکراری اثبات شود.

## خارج از محدوده

- کاتالوگ/Woo در 026A و سبد/سفارش در 026B؛ آموزش/کلینیک/پرداخت provider در کارت‌های خود.
- redesign و مهاجرت مخرب خارج محدوده است.

## Preconditions

- Task READY؛kernel bootstrap و قرارداد REST/role/Woo tasks DONE.
- inventory قبل از تغییر برای route/CPT/hook/options این vertical ثبت شده باشد.

## Allowed files/directories

- `wordpress/packages/carmilla-core/**`
- فایل‌های content/pages/media در `wordpress/carmilla-theme/**` و `wordpress/carmilla-bridge/**`
- rendererهای همین دامنه مطابق قرارداد frontend افزونه؛ بدون بازطراحی قالب میزبان
- `wordpress/**/tests/**` و `tools/test-env/**`
- `docs/evidence/P04-WORDPRESS-CODE-026/**` و وضعیت همین کارت در `docs/**`

## Forbidden actions

- تغییر domainهای خارج Scope،SQL مستقیم Woo order،حذف داده یا حذف adapter قبل از parity.

## مراحل پیاده‌سازی

1. فقط route/CPT/meta و hookهای نوشته، برگه و رسانه را inventory و characterization کن.
2. خدمات مشترک محتوا و adapterهای میزبان‌ها را منتقل کن.
3. نمایش محتوا و فرم مدیریت همین دامنه را با renderer افزونه و template پوسته متصل کن.
4. تست CRUD،دسترسی غیرمجاز،رسانه نامعتبر و حفظ شناسه را در هر سه حالت اجرا کن.

## Automated tests با command و expected result

```powershell
docker compose -f tools/test-env/docker-compose.yml config
bash wordpress/build-theme-zip.sh
bash wordpress/build-bridge-zip.sh
git diff --check
```

- نتیجه مورد انتظار آزمون خودکار: fixture نوشته/برگه/رسانه از مدیریت و frontend هر میزبان و API مشترک پاسخ یکسان داشته باشد؛ permission،اعتبارسنجی رسانه و عدم ثبت تکراری اثبات شود.

## Manual tests با environment/data/steps/expected

- کجا: برگه/نوشته آزمایشی و کتابخانه رسانه در پیشخوان و frontend هر نصب.
- چگونه: یک نوشته و برگه مصنوعی بسازید، تصویر آزمایشی اضافه کنید، ویرایش و مشاهده عمومی را انجام دهید؛ عملیات کاربر فاقد مجوز را امتحان کنید.
- معیار موفقیت: محتوا و شناسه یکسان،آپلود نامعتبر رد،دسترسی غیرمجاز بسته و تغییر میزبان بدون حذف داده باشد.
- سه حالت Theme-only، Plugin-only با قالب پیش‌فرض/ثالث و co-install با داده مصنوعی و ZIP دارای checksum ثبت شود؛ تا تأیید انسانی `AWAITING_MANUAL_QA` بماند.

## Acceptance Criteria

- [ ] دامنه این کارت فقط نوشته،برگه و رسانه است.
- [ ] هر دو میزبان از خدمات یکسان با حفظ شناسه استفاده می‌کنند.
- [ ] frontend و مدیریت پایه این دامنه در هر دو میزبان قابل استفاده‌اند.
- [ ] تست قرارداد/permission و QA انسانی ثبت شده است.

## Security/Privacy/Migration checks

مالکیت نوشته/برگه و capability مدیریت رسانه،نوع/اندازه فایل،escaping و عدم ثبت credential در خروجی بررسی شود.

## Evidence

- `docs/evidence/P04-WORDPRESS-CODE-026/`: inventories،contracts،commands،checksums و Manual QA.

## Rollback

adapter قبلی تا عبور parity محفوظ بماند؛ بازگشت نام‌ها/شناسه‌های محتوا و رسانه را حذف نکند.

## Completion record

- Started/Completed at:
- Changed files:
- Commands and exit codes:
- Manual tester/date/result:
- Evidence paths:
- Remaining risks/blockers:
- Final status: TODO | CODE_COMPLETE | AWAITING_MANUAL_QA | DONE | BLOCKED

</div>
