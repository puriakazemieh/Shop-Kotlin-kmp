# P04-WPPLUGIN-CODE-033 — App Builder control plane در Bridge

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
- Blocks: P04-CI-CODE-019
- Requirement source: Master row P04-WPPLUGIN-CODE-033 و App Builder boundary ADR

## هدف قابل اندازه‌گیری

Bridge pairing،site identity،feature/branding manifest،build request status و artifact metadata/delivery link را به‌عنوان control plane امن مدیریت کند.

## خروجی مورد انتظار

request idempotent و auditشده به runner بیرونی؛هیچ Gradle/Xcode یا signing secret روی WordPress اجرا/ذخیره نشود؛tenant/site isolation برقرار باشد.

## خارج از محدوده

- پیاده‌سازی build runner،ساخت/sign واقعی APK/IPA،Play/App Store publish و نگه‌داری signing key در WordPress.

## Preconditions

- Task READY؛Bridge standalone و co-install arbitration DONE؛contract runner و threat model تصویب‌شده.

## Allowed files/directories

- `wordpress/carmilla-bridge/**`
- contract/adapter محدود `wordpress/packages/carmilla-core/**`
- `wordpress/**/tests/**`،`tools/test-env/**`
- `docs/evidence/P04-WPPLUGIN-CODE-033/**` و status همین Task

## Forbidden actions

- اجرای shell/build arbitrary،ذخیره signing credential،URL دانلود public دائمی،ارسال secret در log یا تماس production.

## مراحل پیاده‌سازی

1. state machine request و threat model/pairing contract را characterization کن.
2. capability/nonce/token و site ownership را اعمال کن.
3. branding/feature manifest validation و idempotency key پیاده کن.
4. runner adapter fake و artifact metadata/signed-expiring link interface اضافه کن.
5. retry/replay/unauthorized/cross-site/audit tests و UI admin synthetic را اجرا کن.

## Automated tests با command و expected result

```powershell
bash wordpress/build-bridge-zip.sh
docker compose -f tools/test-env/docker-compose.yml config
git diff --check
```

- Expected: fake-runner state tests سبز؛replay/cross-site denied؛هیچ process execution یا signing secret در WP؛audit redacted.

## Manual tests با environment/data/steps/expected

- Bridge + fake runner؛دو site synthetic و admin/non-admin.
- pair،manifest preview،submit/retry/cancel و artifact-expiry را اجرا کن.
- Expected: state صحیح،non-admin/cross-site denied،لینک منقضی و native build صفر؛سپس AWAITING_MANUAL_QA.

## Acceptance Criteria

- [ ] WordPress فقط control plane است.
- [ ] pairing/request/artifact قرارداد versioned و idempotent است.
- [ ] least privilege،site isolation،replay protection و audit سبزند.
- [ ] Manual/Security QA تأیید شده است.

## Security/Privacy/Migration checks

- signing secret ممنوع؛token encryption/rotation،SSRF allowlist،expiring link،audit redaction و retention بررسی شود.

## Evidence

- `docs/evidence/P04-WPPLUGIN-CODE-033/`: threat model،fake-runner tests،state/audit reports و QA.

## Rollback

control plane feature flag؛لغو requestهای pending در fake/staging؛داده build metadata بدون policy حذف نشود.

## Completion record

- Started/Completed at:
- Changed files:
- Commands and exit codes:
- Manual/security reviewer/date/result:
- Evidence paths:
- Remaining risks/blockers:
- Final status: TODO | CODE_COMPLETE | AWAITING_MANUAL_QA | DONE | BLOCKED
