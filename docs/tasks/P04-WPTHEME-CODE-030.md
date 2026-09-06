<div dir="rtl" align="right">

# P04-WPTHEME-CODE-030 — یکپارچه‌سازی UI و مدیریت آماده‌شده پوسته مستقل

## Prompt اجرای همین Task

```text
نقش تو Implementer و Verifier فقط همین Task است.
Repository: D:\Android\AndroidStudioProjects\kmp-shop
Master checklist: D:\Android\AndroidStudioProjects\kmp-shop\docs\MASTER_IMPLEMENTATION_CHECKLIST_FA.md
Source audit: D:\Android\AndroidStudioProjects\kmp-shop\docs\PROJECT_AUDIT_AND_PUBLICATION_PLAN_FA.md
Task ID: P04-WPTHEME-CODE-030
قبل از تغییر AGENTS.md،dependencyها،scope،acceptance،git status و baseline را بررسی کن؛اگر Size>M شد توقف و child Task پیشنهاد بده.
فقط همین Task/Allowed scope/کمترین diff؛بدون upgrade،contract جانبی،secret،داده واقعی،deploy یا production.
ابتدا characterization؛verification واقعی؛UI بدون Manual QA برابر AWAITING_MANUAL_QA؛بدون Evidence DONE نکن و به Task بعدی نرو.
پاسخ نهایی: Outcome،Changed files،Tests،Manual QA،Acceptance،Evidence،Checklist،Risks،Rollback.
```

- Status: TODO
- Phase/Area/Type: P04 / WPTHEME / CODE
- Priority/Risk/Size: P0 / HIGH / M
- Owner: BOTH
- Completion authority: BOTH؛ Manual QA الزامی
- Depends on: P04-WORDPRESS-CODE-029B
- Blocks: P04-WPPLUGIN-CODE-031
- Requirement source: Master row P04-WPTHEME-CODE-030 و dual-standalone Theme contract
- مبنای بازبرنامه‌ریزی: [تعریف محصولات مستقل](../INDEPENDENT_PRODUCTS_SPEC_FA.md) و [ADR-006](../architecture/adr/ADR-006-INDEPENDENT-PRODUCTS-AND-ENTITLEMENTS.md).

## هدف قابل اندازه‌گیری

خروجی تکمیل‌شده کارت‌های دامنه و پنل قابلیت در Theme-only یکپارچه و regression شود؛ این کارت فقط اتصال navigation/template/adapterهای آماده و رفع خطای integration کوچک است.

## خروجی مورد انتظار

capabilityهای حاضر،مجاز و آماده فاز چهار در UI و مدیریت پوسته بدون Bridge کار کنند؛ شکاف یک vertical به کارت همان دامنه ارجاع شود و Gate P13/P14 ادعا نشود.

## خارج از محدوده

- ساخت همه UI/دامنه‌ها در یک کارت،بازنویسی kernel،provider certification،App Builder واقعی و redesign کلی خارج محدوده است.

## Preconditions

- Task READY؛چهار vertical Shared Core و Theme hierarchy/Elementor/accessibility DONE.
- capability matrix freeze و fixtureهای demo synthetic آماده باشند.

## Allowed files/directories

- `wordpress/packages/carmilla-core/**`
- فایل‌های integration/template/navigation در `wordpress/carmilla-theme/**` و `wordpress/carmilla-bridge/**`
- rendererهای همین دامنه مطابق قرارداد frontend افزونه؛ بدون بازطراحی قالب میزبان
- `wordpress/**/tests/**` و `tools/test-env/**`
- `docs/evidence/P04-WPTHEME-CODE-030/**` و وضعیت همین کارت در `docs/**`

## Forbidden actions

- ایجاد dependency runtime به Bridge،کپی مجدد domain logic در Theme Host یا حذف feature بدون ADR.

## مراحل پیاده‌سازی

1. ماتریس capability→صفحه→مدیریت→API را با شواهد کارت‌های دامنه تطبیق بده.
2. منو،template و adapter آماده را به وضعیت مؤثر kernel وصل کن.
3. بسته پایه،تک‌قابلیت و ترکیبی را بدون Bridge نصب و لینک/وضعیت خاموش را regression کن.
4. خطای محدود integration را اصلاح کن؛ UI یا منطق دامنه تکمیل‌نشده را به تسک مستقل برگردان.

## Automated tests با command و expected result

```powershell
bash wordpress/build-theme-zip.sh
docker compose -f tools/test-env/docker-compose.yml config
git diff --check
```

- نتیجه مورد انتظار آزمون خودکار: capabilityهای حاضر،مجاز و آماده فاز چهار در UI و مدیریت پوسته بدون Bridge کار کنند؛ شکاف یک vertical به کارت همان دامنه ارجاع شود و Gate P13/P14 ادعا نشود.

## Manual tests با environment/data/steps/expected

- کجا: صفحه خانه،منوها،پنل قابلیت و صفحات دامنه‌های آماده روی Theme-only.
- چگونه: ZIP پایه و یک ZIP ترکیبی را آزمایش کنید؛ یک فیچر مجاز را خاموش/روشن و لینک مستقیم فیچر غیرمجاز را باز کنید.
- معیار موفقیت: بدون Bridge مسیر خریداری‌شده قابل استفاده،مسیر غیرمجاز بسته و اطلاعات قبلی محفوظ باشد؛ capabilityهای خارج Gate فروش فعال اعلام نشوند.
- سه حالت Theme-only، Plugin-only با قالب پیش‌فرض/ثالث و co-install با داده مصنوعی و ZIP دارای checksum ثبت شود؛ تا تأیید انسانی `AWAITING_MANUAL_QA` بماند.

## Acceptance Criteria

- [ ] این کارت فقط integration خروجی آماده دامنه‌هاست.
- [ ] Theme-only بسته پایه و ترکیبی بدون Bridge اجرا می‌شود.
- [ ] وضعیت UI/admin/API با resolver یکسان است.
- [ ] شواهد regression و QA انسانی موجود و محدودیت انتشار صریح است.

## Security/Privacy/Migration checks

- form nonce/capability/escaping،health data privacy و داده‌نزدایی uninstall حفظ شود.

## Evidence

- `docs/evidence/P04-WPTHEME-CODE-030/`: coverage matrix،ZIP hash،install log،screenshots و QA.

## Rollback

host adapter/UI تغییرات را revert کن؛Shared Core data/schema را rollback destructive نکن.

## Completion record

- Started/Completed at:
- Changed files:
- Commands and exit codes:
- Manual tester/date/result:
- Evidence paths:
- Remaining risks/blockers:
- Final status: TODO | CODE_COMPLETE | AWAITING_MANUAL_QA | DONE | BLOCKED

</div>
