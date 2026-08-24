# P04-WORDPRESS-CODE-032 — arbitration نصب هم‌زمان و compatibility kernel

## Prompt اجرای همین Task

```text
نقش تو Implementer و Verifier فقط همین Task است.
Repository: D:\Android\AndroidStudioProjects\kmp-shop
Master checklist: D:\Android\AndroidStudioProjects\kmp-shop\docs\MASTER_IMPLEMENTATION_CHECKLIST_FA.md
Source audit: D:\Android\AndroidStudioProjects\kmp-shop\docs\PROJECT_AUDIT_AND_PUBLICATION_PLAN_FA.md
Task ID: P04-WORDPRESS-CODE-032
قبل از تغییر AGENTS.md،ADR،dependency/scope/acceptance،git status و baseline را بررسی کن؛Size>M یعنی توقف.
فقط همین Task/Allowed scope/کمترین diff؛بدون upgrade جانبی،contract جدید،production یا migration destructive.
ابتدا تست collision/mismatch شکست‌خورده؛verification واقعی؛بدون Evidence DONE نکن و جلو نرو.
پاسخ نهایی: Outcome،Changed files،Tests،Manual QA،Acceptance،Evidence،Checklist،Risks،Rollback.
```

- Status: TODO
- Phase/Area/Type: P04 / WORDPRESS / CODE
- Priority/Risk/Size: P0 / CRITICAL / M
- Owner: BOTH
- Completion authority: BOTH + Architecture/Security reviewer
- Depends on: P04-WPPLUGIN-CODE-031
- Blocks: P04-WPPLUGIN-CODE-033
- Requirement source: Master row P04-WORDPRESS-CODE-032 و Shared Core version authority ADR

## هدف قابل اندازه‌گیری

وقتی Theme و Bridge هم‌زمان نصب‌اند،دقیقاً یک kernel سازگار و یک مجموعه canonical route/CPT/hook/migration boot شود و mismatch ناشناخته fail-closed باشد.

## خروجی مورد انتظار

bootstrap arbitration namespaced،semantic compatibility matrix،admin notice actionable و migration lock/idempotency با تست upgrade/downgrade.

## خارج از محدوده

- extraction feature جدید،UI redesign،auto-update channel و downgrade destructive schema.

## Preconditions

- Task READY؛دو artifact standalone و ADR version authority DONE؛inventory baseline سه mode موجود.

## Allowed files/directories

- `wordpress/packages/carmilla-core/**`
- bootstrap/host loaderهای Theme و Bridge
- build/test fixtures و `tools/test-env/**`
- `docs/evidence/P04-WORDPRESS-CODE-032/**` و status همین Task

## Forbidden actions

- global class/function collision،انتخاب silent نسخه ناسازگار،اجرای دو migration runner یا پاک‌کردن داده برای downgrade.

## مراحل پیاده‌سازی

1. collision tests برای فعال‌سازی با ترتیب‌های مختلف و version mismatch بساز.
2. candidate registration و authority selection مطابق ADR پیاده کن.
3. schema lock/version compatibility و notice fail-closed اضافه کن.
4. route/CPT/hook/migration counts را با authority-only مقایسه کن.
5. upgrade/downgrade compatible/incompatible و rollback را اجرا کن.

## Automated tests با command و expected result

```powershell
bash wordpress/build-theme-zip.sh
bash wordpress/build-bridge-zip.sh
docker compose -f tools/test-env/docker-compose.yml config
git diff --check
```

- Expected: هر ترتیب activation بدون fatal؛inventory both برابر authority-only؛mismatch ناسازگار بدون write و با notice؛migration یک بار.

## Manual tests با environment/data/steps/expected

- Theme و Bridge با نسخه‌های same/compatible/incompatible؛ترتیب activation را عوض کن و admin/frontend/API را باز کن.
- Expected: یک authority،notice روشن،data loss/duplicate/fatal صفر؛سپس AWAITING_MANUAL_QA.

## Acceptance Criteria

- [ ] انتخاب authority deterministic و versioned است.
- [ ] duplicate route/CPT/hook/migration صفر است.
- [ ] mismatch ناسازگار fail-closed و recoverable است.
- [ ] upgrade/rollback Evidence و review تأیید شده است.

## Security/Privacy/Migration checks

- migration lock،idempotency،backup/forward-fix و عدم log secret/PII الزامی.

## Evidence

- `docs/evidence/P04-WORDPRESS-CODE-032/`: activation/version matrix،inventories،migration logs و rollback report.

## Rollback

authority selector feature flag؛بدون schema downgrade destructive؛artifact compatible قبلی با checksum نگه‌داری شود.

## Completion record

- Started/Completed at:
- Changed files:
- Commands and exit codes:
- Manual/reviewer/date/result:
- Evidence paths:
- Remaining risks/blockers:
- Final status: TODO | CODE_COMPLETE | AWAITING_MANUAL_QA | DONE | BLOCKED
