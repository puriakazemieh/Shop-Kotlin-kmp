# P04-WORDPRESS-CODE-026 — انتقال Content/Pages/Media/Store به Shared Core

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
- Depends on: P04-WPPLUGIN-CODE-018
- Blocks: P04-WORDPRESS-CODE-027
- Requirement source: Master row P04-WORDPRESS-CODE-026 و `plans/002-shared-wordpress-feature-kernel.md`

## هدف قابل اندازه‌گیری

منطق canonical نوشته،برگه،رسانه،کاتالوگ،محصول،سبد و سفارش از پیاده‌سازی تکراری Theme/Bridge به Shared Core منتقل شود.

## خروجی مورد انتظار

یک قرارداد versioned و parity یکسان در Theme-only و Bridge-only؛WooCommerce منبع canonical commerce و route/CPT تکراری صفر.

## خارج از محدوده

- LMS،Clinic،PsychTest،Support،provider پرداخت و redesign UI.
- breaking API change بدون version جدید.

## Preconditions

- Task READY؛kernel bootstrap و قرارداد REST/role/Woo tasks DONE.
- inventory قبل از تغییر برای route/CPT/hook/options این vertical ثبت شده باشد.

## Allowed files/directories

- `wordpress/packages/carmilla-core/**`
- `wordpress/carmilla-theme/inc/**`
- `wordpress/carmilla-bridge/includes/**`
- `wordpress/**/tests/**`
- `tools/test-env/**`
- `docs/evidence/P04-WORDPRESS-CODE-026/**`
- status همین Task در `docs/**`

## Forbidden actions

- تغییر domainهای خارج Scope،SQL مستقیم Woo order،حذف داده یا حذف adapter قبل از parity.

## مراحل پیاده‌سازی

1. inventory و characterization برای CRUD/permissions/error envelope بساز.
2. canonical interfaces و adapters را در Shared Core تعریف کن.
3. read paths سپس write paths را با Woo CRUD رسمی منتقل کن.
4. Theme Host و Bridge Host را به همان interfaces متصل کن.
5. route/CPT/hook inventory و contract parity را در سه mode بررسی کن.
6. داده fixture را deactivate/reactivate و theme switch کرده و checksum بگیر.

## Automated tests با command و expected result

```powershell
docker compose -f tools/test-env/docker-compose.yml config
bash wordpress/build-theme-zip.sh
bash wordpress/build-bridge-zip.sh
git diff --check
```

- Expected: build/lint/integration exit code 0؛CRUD و contract tests دو artifact یکسان؛duplicate registration و SQL مستقیم Woo صفر.

## Manual tests با environment/data/steps/expected

- Environment: Theme-only و Bridge+Storefront؛داده synthetic شامل post/page/media/product.
- Steps: create/edit/list/delete-safe،cart/order sandbox و theme switch را اجرا کن.
- Expected: داده و پاسخ canonical یکسان،permission درست و data loss صفر؛سپس AWAITING_MANUAL_QA تا تأیید.

## Acceptance Criteria

- [ ] characterization قبل از extraction موجود است.
- [ ] یک implementation canonical و دو host adapter وجود دارد.
- [ ] parity و lifecycle tests سبزند.
- [ ] Evidence انسانی تأیید شده است.

## Security/Privacy/Migration checks

- ownership و capability همه writeها؛media validation؛order با Woo CRUD؛migration idempotent و داده synthetic.

## Evidence

- `docs/evidence/P04-WORDPRESS-CODE-026/`: inventories،contracts،commands،checksums و Manual QA.

## Rollback

adapter قبلی تا عبور parity حذف نشود؛rollback با feature switch و بدون rollback destructive داده.

## Completion record

- Started/Completed at:
- Changed files:
- Commands and exit codes:
- Manual tester/date/result:
- Evidence paths:
- Remaining risks/blockers:
- Final status: TODO | CODE_COMPLETE | AWAITING_MANUAL_QA | DONE | BLOCKED
