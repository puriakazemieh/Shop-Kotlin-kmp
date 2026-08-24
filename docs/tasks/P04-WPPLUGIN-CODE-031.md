# P04-WPPLUGIN-CODE-031 — یکپارچه‌سازی کامل Bridge standalone و any-theme

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
- Blocks: P04-WORDPRESS-CODE-032
- Requirement source: Master row P04-WPPLUGIN-CODE-031 و Bridge any-theme contract

## هدف قابل اندازه‌گیری

Bridge ZIP روی Carmilla،Storefront و یک قالب ثالث همه داده/featureهای manifest را به Android/PWA/Web و contract مشترک clientها ارائه و مدیریت کند.

## خروجی مورد انتظار

CRUD/sync/auth/navigation capability بدون دست‌کاری presentation قالب میزبان و بدون dependency به Carmilla Theme؛CORS/permissions fail-closed.

## خارج از محدوده

- native build pipeline،Theme UI،provider production و iOS/Desktop release.

## Preconditions

- Task READY؛Theme integration و verticalهای Shared Core DONE؛client contract fixtures آماده.

## Allowed files/directories

- `wordpress/carmilla-bridge/**`
- adapter/config محدود `wordpress/packages/carmilla-core/**`
- contract tests در `composeApp/**`/`core/**` فقط در صورت نیاز همین contract
- `wordpress/**/tests/**`،`tools/test-env/**`
- `docs/evidence/P04-WPPLUGIN-CODE-031/**` و status همین Task

## Forbidden actions

- echo کردن Origin غیرمجاز،wildcard credentials،permission callback باز،Theme-specific DOM/CSS injection یا secret در diagnostics.

## مراحل پیاده‌سازی

1. manifest-to-endpoint/client matrix و Theme dependency inventory بساز.
2. characterization tests برای سه Theme و auth/CRUD اضافه کن.
3. host adapterها و endpointهای ناقص را با Shared Core کامل کن.
4. CORS allowlist،permission/ownership،pagination/validation و diagnostics redaction را اثبات کن.
5. Android/PWA contract smoke و Theme-switch data survival را اجرا کن.

## Automated tests با command و expected result

```powershell
bash wordpress/build-bridge-zip.sh
docker compose -f tools/test-env/docker-compose.yml config
.\gradlew.bat :composeApp:compileKotlinJs :composeApp:compileKotlinJvm
git diff --check
```

- Expected: Bridge ZIP مستقل؛contract tests و compile سبز؛قالب میزبان unchanged؛CORS/permission negative tests سبز.

## Manual tests با environment/data/steps/expected

- Storefront و یک Theme ثالث + Bridge؛Android/PWA با داده synthetic.
- login،manifest،list/detail،CRUD مجاز/غیرمجاز،toggle و theme switch را اجرا کن.
- Expected: داده و navigation صحیح،ظاهر Theme میزبان سالم،unauthorized denied؛سپس AWAITING_MANUAL_QA.

## Acceptance Criteria

- [ ] runtime dependency به Carmilla Theme صفر است.
- [ ] manifest/client parity برای همه featureهای declared ثبت شده است.
- [ ] any-theme،CORS و ownership tests سبزند.
- [ ] Android/PWA Manual QA تأیید شده است.

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
