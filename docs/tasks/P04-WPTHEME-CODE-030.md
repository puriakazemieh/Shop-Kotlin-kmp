# P04-WPTHEME-CODE-030 — یکپارچه‌سازی کامل Carmilla Theme standalone

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
- Depends on: P04-WORDPRESS-CODE-029
- Blocks: P04-WPPLUGIN-CODE-031
- Requirement source: Master row P04-WPTHEME-CODE-030 و dual-standalone Theme contract

## هدف قابل اندازه‌گیری

Carmilla Theme ZIP بدون Bridge برای تمام capabilityهای موجود پروژه،صفحه/منو/admin setting/feature toggle و stateهای UI کامل داشته باشد.

## خروجی مورد انتظار

نصب تمیز Theme-only با Content/Store/Academy/Clinic/PsychTest/Support فعال طبق manifest؛هیچ CTA/route به Bridge اجباری نباشد.

## خارج از محدوده

- بازنویسی domain logic Shared Core،provider certification،App Builder و redesign کلی.

## Preconditions

- Task READY؛چهار vertical Shared Core و Theme hierarchy/Elementor/accessibility DONE.
- capability matrix freeze و fixtureهای demo synthetic آماده باشند.

## Allowed files/directories

- `wordpress/carmilla-theme/**`
- adapter/config محدود `wordpress/packages/carmilla-core/**`
- `wordpress/**/tests/**`،`tools/test-env/**`
- `docs/evidence/P04-WPTHEME-CODE-030/**` و status همین Task

## Forbidden actions

- ایجاد dependency runtime به Bridge،کپی مجدد domain logic در Theme Host یا حذف feature بدون ADR.

## مراحل پیاده‌سازی

1. capability-to-screen/admin/navigation matrix را با وضعیت موجود مقایسه کن.
2. characterization برای link/template/toggleهای ناقص بساز.
3. Theme Host را برای هر capability به Shared Core وصل و missing UI/state را تکمیل کن.
4. prerequisiteهای Woo/Elementor/providers را actionable و fail-closed کن.
5. Theme-only clean install و regression viewport/RTL/accessibility را اجرا کن.

## Automated tests با command و expected result

```powershell
bash wordpress/build-theme-zip.sh
docker compose -f tools/test-env/docker-compose.yml config
git diff --check
```

- Expected: ZIP مستقل روی WordPress تمیز نصب؛manifest coverage 100% featureهای declared؛link/template fatal و dependency Bridge صفر.

## Manual tests با environment/data/steps/expected

- Theme ZIP فقط،Woo و Elementor در سناریوهای حاضر/غایب؛fixture هر vertical.
- همه منوها،admin CRUD،frontend،toggle،empty/error و responsive را اجرا کن.
- Expected: همه capabilityهای enabled قابل استفاده؛نبود prerequisite پیام روشن؛سپس AWAITING_MANUAL_QA.

## Acceptance Criteria

- [ ] Theme به Bridge وابستگی runtime ندارد.
- [ ] capability matrix و UI/admin coverage کامل است.
- [ ] prerequisite و feature toggle رفتار صحیح دارند.
- [ ] automated و Manual QA Evidence سبز است.

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
