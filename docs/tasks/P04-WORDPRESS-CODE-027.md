# P04-WORDPRESS-CODE-027 — انتقال Academy/LMS به Shared Core

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
- Depends on: P04-WORDPRESS-CODE-026
- Blocks: P04-WORDPRESS-CODE-028
- Requirement source: Master row P04-WORDPRESS-CODE-027 و Feature Manifest Academy

## هدف قابل اندازه‌گیری

Course،lesson/content،enrollment،progress،request،bundle/membership و certificate/verification در یک LMS domain مشترک برای هر دو artifact قرار گیرند.

## خروجی مورد انتظار

Theme-only و Bridge-only برای نقش دانشجو/مدرس/مدیر contract و state transition یکسان داشته باشند و duplicate route/CPT/write صفر باشد.

## خارج از محدوده

- video DRM/streaming vendor،پرداخت provider-specific،redesign دوره و Clinic/Psych.

## Preconditions

- Task READY؛Shared Core و content/store vertical DONE؛LMS capability contract freeze.

## Allowed files/directories

- `wordpress/packages/carmilla-core/**`
- فایل‌های academy/course در `wordpress/carmilla-theme/**` و `wordpress/carmilla-bridge/**`
- `wordpress/**/tests/**`،`tools/test-env/**`
- `docs/evidence/P04-WORDPRESS-CODE-027/**` و status همین Task

## Forbidden actions

- تغییر payment/media contract خارج Scope،دسترسی دوره بدون enrollment یا ثبت داده learner واقعی.

## مراحل پیاده‌سازی

1. route/CPT/state/role inventory و characterization بساز.
2. model و state transition canonical را در Shared Core تعریف کن.
3. read/writeها را منتقل و host adapterها را وصل کن.
4. authorization دانشجو/مدرس/مدیر و certificate verification را تست کن.
5. parity،deactivate/reactivate و feature toggle را در دو mode اجرا کن.

## Automated tests با command و expected result

```powershell
docker compose -f tools/test-env/docker-compose.yml config
bash wordpress/build-theme-zip.sh
bash wordpress/build-bridge-zip.sh
git diff --check
```

- Expected: lifecycle و role matrix سبز؛progress idempotent؛duplicate registration/data loss صفر.

## Manual tests با environment/data/steps/expected

- داده synthetic: یک course،دو lesson،student/instructor/admin و certificate.
- Theme-only و Bridge+Storefront: enrollment،progress،completion،certificate و permission منفی را اجرا کن.
- Expected: state و UI/API مطابق manifest؛تا تأیید انسان AWAITING_MANUAL_QA.

## Acceptance Criteria

- [ ] LMS یک source canonical دارد.
- [ ] role/state/negative tests سبزند.
- [ ] Theme/Bridge parity و feature toggle اثبات شده است.
- [ ] Manual QA Evidence تأیید شده است.

## Security/Privacy/Migration checks

- enrollment ownership،least privilege،certificate enumeration resistance و migration idempotent بررسی شود.

## Evidence

- `docs/evidence/P04-WORDPRESS-CODE-027/`: inventory،state tests،role matrix،commands و QA.

## Rollback

با adapter/feature switch به مسیر قبلی برگرد؛progress/enrollment/certificate حذف نشوند.

## Completion record

- Started/Completed at:
- Changed files:
- Commands and exit codes:
- Manual tester/date/result:
- Evidence paths:
- Remaining risks/blockers:
- Final status: TODO | CODE_COMPLETE | AWAITING_MANUAL_QA | DONE | BLOCKED
