<div dir="rtl" align="right">

# P04-WPPLUGIN-CODE-033 — قرارداد مشترک اتصال سایت و pairing اپ‌ساز برای دو میزبان

## Prompt اجرای همین Task

```text
نقش تو Implementer و Verifier فقط همین Task است.
Repository: D:\Android\AndroidStudioProjects\kmp-shop
Master checklist: D:\Android\AndroidStudioProjects\kmp-shop\docs\MASTER_IMPLEMENTATION_CHECKLIST_FA.md
Source audit: D:\Android\AndroidStudioProjects\kmp-shop\docs\PROJECT_AUDIT_AND_PUBLICATION_PLAN_FA.md
Task ID: P04-WPPLUGIN-CODE-033
AGENTS.md،ADR،dependency/scope/acceptance،git status و baseline را قبل از تغییر بررسی کن؛Size>M یعنی توقف و child Task.
فقط همین Task/Allowed scope؛بدون native build روی WP،signing key،production delivery،upgrade جانبی یا API خارج version.
ابتدا threat/characterization tests؛verification واقعی؛Network/UI بدون Manual QA برابر AWAITING_MANUAL_QA؛بدون Evidence DONE نکن و جلو نرو.
پاسخ نهایی: Outcome،Changed files،Tests،Manual QA،Acceptance،Evidence،Checklist،Risks،Rollback.
```

- Status: TODO
- Phase/Area/Type: P04 / WPPLUGIN / CODE
- Priority/Risk/Size: P0 / HIGH / M
- Owner: BOTH
- Completion authority: BOTH + Security reviewer
- Depends on: P04-WORDPRESS-CODE-032
- Blocks: P04-WORDPRESS-CODE-033A
- Requirement source: Master row P04-WPPLUGIN-CODE-033 و App Builder boundary ADR
- مبنای بازبرنامه‌ریزی: [تعریف محصولات مستقل](../INDEPENDENT_PRODUCTS_SPEC_FA.md) و [ADR-006](../architecture/adr/ADR-006-INDEPENDENT-PRODUCTS-AND-ENTITLEMENTS.md).

## هدف قابل اندازه‌گیری

قرارداد نسخه‌دار هویت سایت،pairing و ارتباط امن با fake runner در kernel برای Theme-only و Plugin-only ساخته شود؛ این کارت فقط identity/pairing و interface مشترک است.

## خروجی مورد انتظار

هر میزبان مستقل با runner مصنوعی pair شود؛ اتصال سایت دیگر،token نامعتبر/replay و تغییر origin غیرمجاز رد شوند؛ هیچ خروجی واقعی یا UI کامل اپ‌ساز ادعا نشود.

## خارج از محدوده

- request/status/cancel/download در 033A؛ پنل Theme در 046 و Plugin در 047.
- اجرای build واقعی،امضا و انتشار Android/Web/PWA در P12،iOS در P16 و Desktop در P17.
- credential امضا یا اجرای Gradle/Xcode روی WordPress ممنوع است.

## Preconditions

- Task READY؛Bridge standalone و co-install arbitration DONE؛contract runner و threat model تصویب‌شده.

## Allowed files/directories

- `wordpress/packages/carmilla-core/**`
- فایل‌های builder/pairing/host adapter در `wordpress/carmilla-theme/**` و `wordpress/carmilla-bridge/**`
- rendererهای همین دامنه مطابق قرارداد frontend افزونه؛ بدون بازطراحی قالب میزبان
- `wordpress/**/tests/**` و `tools/test-env/**`
- `docs/evidence/P04-WPPLUGIN-CODE-033/**` و وضعیت همین کارت در `docs/**`

## Forbidden actions

- اجرای shell/build arbitrary،ذخیره signing credential،URL دانلود public دائمی،ارسال secret در log یا تماس production.

## مراحل پیاده‌سازی

1. قرارداد site identity،pairing،origin و token lifecycle را مطابق ADR-006 و تهدیدنامه مشخص کن.
2. adapter kernel مشترک و entrypoint هر دو host را به fake runner متصل کن.
3. حق استفاده از App Builder و target را از مجوز دامنه جدا کنترل کن.
4. retry/replay و cross-site را تست و قرارداد request/status/artifact کارت 033A را آماده کن.

## Automated tests با command و expected result

```powershell
bash wordpress/build-bridge-zip.sh
docker compose -f tools/test-env/docker-compose.yml config
git diff --check
```

- نتیجه مورد انتظار آزمون خودکار: هر میزبان مستقل با runner مصنوعی pair شود؛ اتصال سایت دیگر،token نامعتبر/replay و تغییر origin غیرمجاز رد شوند؛ هیچ خروجی واقعی یا UI کامل اپ‌ساز ادعا نشود.

## Manual tests با environment/data/steps/expected

- کجا: صفحه تشخیص اتصال سایت در هر یک از دو میزبان مستقل و fake runner آزمایشی.
- چگونه: دو سایت مصنوعی را جدا pair کنید؛ token سایت اول را در سایت دوم و origin غیرمجاز را آزمایش کنید.
- معیار موفقیت: pairing معتبر موفق و cross-site/replay رد شود؛ نبود محصول Carmilla دیگر مانع اتصال نباشد.
- سه حالت Theme-only، Plugin-only با قالب پیش‌فرض/ثالث و co-install با داده مصنوعی و ZIP دارای checksum ثبت شود؛ تا تأیید انسانی `AWAITING_MANUAL_QA` بماند.

## Acceptance Criteria

- [ ] قرارداد pairing در kernel واحد و هر دو host قابل استفاده است.
- [ ] site/product provenance و origin مجاز اعمال می‌شوند.
- [ ] cross-site،replay و token lifecycle آزموده‌اند.
- [ ] WordPress ابزار native/signing key ندارد؛ fake runner آماده‌بودن خروجی تجاری نیست.

## Security/Privacy/Migration checks

کمینه دسترسی،اعتبار origin،SSRF allowlist،token rotation و audit redacted؛ signing credential در WordPress ذخیره نشود.

## Evidence

- `docs/evidence/P04-WPPLUGIN-CODE-033/`: threat model،fake-runner tests،state/audit reports و QA.

## Rollback

اتصال آزمایشی غیرفعال و token pairing لغو شود؛ داده سایت و metadata موجود بدون policy پاک نشوند.

## Completion record

- Started/Completed at:
- Changed files:
- Commands and exit codes:
- Manual/security reviewer/date/result:
- Evidence paths:
- Remaining risks/blockers:
- Final status: TODO | CODE_COMPLETE | AWAITING_MANUAL_QA | DONE | BLOCKED

</div>
