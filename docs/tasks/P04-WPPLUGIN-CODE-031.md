<div dir="rtl" align="right">

# P04-WPPLUGIN-CODE-031 — یکپارچه‌سازی frontend و مدیریت آماده افزونه روی قالب ثالث

## Prompt اجرای همین Task

```text
نقش تو Implementer و Verifier فقط همین Task است.
Repository: D:\Android\AndroidStudioProjects\kmp-shop
Master checklist: D:\Android\AndroidStudioProjects\kmp-shop\docs\MASTER_IMPLEMENTATION_CHECKLIST_FA.md
Source audit: D:\Android\AndroidStudioProjects\kmp-shop\docs\PROJECT_AUDIT_AND_PUBLICATION_PLAN_FA.md
Task ID: P04-WPPLUGIN-CODE-031
AGENTS.md،dependency/scope/acceptance،git status و baseline را قبل از تغییر بررسی کن؛Size>M یعنی توقف و تقسیم.
فقط همین Task/Allowed scope/کمترین diff؛بدون Theme-specific hack،upgrade/API جانبی،secret،production یا deploy.
ابتدا characterization؛verification واقعی؛Network/UI بدون Manual QA برابر AWAITING_MANUAL_QA؛بدون Evidence DONE نکن و جلو نرو.
پاسخ نهایی: Outcome،Changed files،Tests،Manual QA،Acceptance،Evidence،Checklist،Risks،Rollback.
```

- Status: TODO
- Phase/Area/Type: P04 / WPPLUGIN / CODE
- Priority/Risk/Size: P0 / HIGH / M
- Owner: BOTH
- Completion authority: BOTH؛ Security و Manual QA الزامی
- Depends on: P04-WPTHEME-CODE-030
- Blocks: P04-WORDPRESS-DATA-042
- Requirement source: Master row P04-WPPLUGIN-CODE-031 و Bridge any-theme contract
- مبنای بازبرنامه‌ریزی: [تعریف محصولات مستقل](../INDEPENDENT_PRODUCTS_SPEC_FA.md) و [ADR-006](../architecture/adr/ADR-006-INDEPENDENT-PRODUCTS-AND-ENTITLEMENTS.md).

## هدف قابل اندازه‌گیری

صفحات و فرم‌های عمومی،مدیریت و API خروجی کارت‌های دامنه و renderer آماده در Plugin-only یکپارچه شوند؛ کاربر سایت بدون Carmilla Theme و بدون اپ از فیچر مجاز استفاده کند.

## خروجی مورد انتظار

روی قالب پیش‌فرض،Storefront و یک قالب ثالث آزموده‌شده،مسیر عمومی و مدیریت قابلیت‌های آماده مستقل باشد؛ اتصال Android/PWA فقط smoke قرارداد و نه Gate انتشار کلاینت باشد.

## خارج از محدوده

- پیاده‌سازی تازه همه verticalها،بازطراحی قالب میزبان،runner واقعی و Gate تجاری Android/iOS/Desktop/LMS/Clinic خارج محدوده‌اند.

## Preconditions

- Task READY؛Theme integration و verticalهای Shared Core DONE؛client contract fixtures آماده.

## Allowed files/directories

- `wordpress/packages/carmilla-core/**`
- فایل‌های frontend/admin/integration در `wordpress/carmilla-theme/**` و `wordpress/carmilla-bridge/**`
- rendererهای همین دامنه مطابق قرارداد frontend افزونه؛ بدون بازطراحی قالب میزبان
- `wordpress/**/tests/**` و `tools/test-env/**`
- `docs/evidence/P04-WPPLUGIN-CODE-031/**` و وضعیت همین کارت در `docs/**`

## Forbidden actions

- echo کردن Origin غیرمجاز،wildcard credentials،permission callback باز،Theme-specific DOM/CSS injection یا secret در diagnostics.

## مراحل پیاده‌سازی

1. ماتریس capability→renderer→فرم عمومی→مدیریت→API را با خروجی دامنه‌ها تطبیق بده.
2. navigation و صفحه‌های آماده را به shell افزونه و effective manifest متصل کن؛ CSS محدود به اجزای افزونه باشد.
3. بسته پایه/ترکیبی و theme switch را regression کن؛ نمایش قالب میزبان حفظ شود.
4. قرارداد یک کلاینت تست را smoke کن؛ missing vertical یا frontend جدید را به کارت آن ارجاع بده.

## Automated tests با command و expected result

```powershell
bash wordpress/build-bridge-zip.sh
docker compose -f tools/test-env/docker-compose.yml config
.\gradlew.bat :composeApp:compileKotlinJs :composeApp:compileKotlinJvm
git diff --check
```

- نتیجه مورد انتظار آزمون خودکار: روی قالب پیش‌فرض،Storefront و یک قالب ثالث آزموده‌شده،مسیر عمومی و مدیریت قابلیت‌های آماده مستقل باشد؛ اتصال Android/PWA فقط smoke قرارداد و نه Gate انتشار کلاینت باشد.

## Manual tests با environment/data/steps/expected

- کجا: صفحات عمومی افزونه،نوبت‌های من/دوره‌های مجاز و پیشخوان روی قالب دیگر.
- چگونه: فقط Plugin ZIP را نصب کنید؛ با کاربر مصنوعی مسیر عمومی موجود یک فیچر را کامل و با مدیر همان داده را مشاهده کنید؛ قالب میزبان را عوض کنید.
- معیار موفقیت: فیچر بدون اپ و بدون Carmilla Theme قابل استفاده،CSS محدود،داده ثابت و UI/API با مجوز بسته هماهنگ باشد.
- سه حالت Theme-only، Plugin-only با قالب پیش‌فرض/ثالث و co-install با داده مصنوعی و ZIP دارای checksum ثبت شود؛ تا تأیید انسانی `AWAITING_MANUAL_QA` بماند.

## Acceptance Criteria

- [ ] API-only به‌عنوان استقلال کامل افزونه پذیرفته نشده است.
- [ ] صفحات/فرم‌های آماده و پیشخوان روی قالب‌های آزموده‌شده کار می‌کنند.
- [ ] مجوز و وضعیت feature در UI و API یکسان‌اند.
- [ ] regression و QA مستقل ثبت شده‌اند؛ انتشار کلاینت/دامنه‌های آینده ادعا نشده است.

## Security/Privacy/Migration checks

- JWT/nonce/capability،CORS allowlist،IDOR،rate limit،redaction و schema lifecycle بررسی شود.

## Evidence

- `docs/evidence/P04-WPPLUGIN-CODE-031/`: Theme matrix،contract/security reports،client smoke و ZIP hash.

## Rollback

Bridge adapter/endpoint تغییرات را revert یا feature flag کن؛site data حذف نشود.

## Completion record

- Started/Completed at:
- Changed files:
- Commands and exit codes:
- Manual/security reviewer/date/result:
- Evidence paths:
- Remaining risks/blockers:
- Final status: TODO | CODE_COMPLETE | AWAITING_MANUAL_QA | DONE | BLOCKED

</div>
