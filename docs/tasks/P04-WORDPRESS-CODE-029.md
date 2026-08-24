# P04-WORDPRESS-CODE-029 — انتقال PsychTest/Support/Interactions به Shared Core

## Prompt اجرای همین Task

```text
نقش تو Implementer و Verifier فقط همین Task است.
Repository: D:\Android\AndroidStudioProjects\kmp-shop
Master checklist: D:\Android\AndroidStudioProjects\kmp-shop\docs\MASTER_IMPLEMENTATION_CHECKLIST_FA.md
Source audit: D:\Android\AndroidStudioProjects\kmp-shop\docs\PROJECT_AUDIT_AND_PUBLICATION_PLAN_FA.md
Task ID: P04-WORDPRESS-CODE-029
AGENTS.md،dependency/scope/acceptance،git status و baseline را قبل از تغییر بررسی کن؛Size>M یعنی توقف و تقسیم.
فقط همین Task/Allowed scope؛کمترین diff؛بدون PHI/PII واقعی،production،upgrade یا API جانبی.
ابتدا characterization؛verification واقعی؛Manual QA اجرا‌نشده یعنی AWAITING_MANUAL_QA؛بدون Evidence DONE نکن و جلو نرو.
پاسخ نهایی: Outcome،Changed files،Tests،Manual QA،Acceptance،Evidence،Checklist،Risks،Rollback.
```

- Status: TODO
- Phase/Area/Type: P04 / WORDPRESS / CODE
- Priority/Risk/Size: P0 / HIGH / M
- Owner: BOTH
- Completion authority: BOTH + Security/Privacy reviewer
- Depends on: P04-WORDPRESS-CODE-028
- Blocks: P04-WPTHEME-CODE-030
- Requirement source: Master row P04-WORDPRESS-CODE-029 و Feature Manifest Psych/Support

## هدف قابل اندازه‌گیری

PsychTest definition/answer/scoring/result و Support ticket/favorite/comment/review/interactions در Shared Core واحد با policy جداگانه privacy قرار گیرند.

## خروجی مورد انتظار

scoring deterministic،result privacy و ticket ownership در Theme/Bridge یکسان؛duplicate route/CPT/write و اطلاعات حساس در log صفر.

## خارج از محدوده

- تشخیص پزشکی،تفسیر clinical جدید،AI advice،notification provider و redesign UI.

## Preconditions

- Task READY؛role/privacy/migration و Clinic vertical DONE؛fixtureها synthetic و غیرقابل انتساب.

## Allowed files/directories

- `wordpress/packages/carmilla-core/**`
- فایل‌های psychtest/support/interactions دو artifact
- `wordpress/**/tests/**`،`tools/test-env/**`
- `docs/evidence/P04-WORDPRESS-CODE-029/**` و status همین Task

## Forbidden actions

- PHI/PII واقعی،تغییر الگوریتم scoring بدون fixture/reference،public result endpoint یا حذف داده.

## مراحل پیاده‌سازی

1. inventory/scoring fixtures/permission characterization بساز.
2. model و services canonical را با privacy boundary تعریف کن.
3. adapterهای دو artifact را وصل و registrations تکراری را خاموش کن.
4. scoring determinism،result/ticket ownership،rate/validation و export/erase policy را تست کن.
5. parity،feature toggle و lifecycle را ثبت کن.

## Automated tests با command و expected result

```powershell
docker compose -f tools/test-env/docker-compose.yml config
bash wordpress/build-theme-zip.sh
bash wordpress/build-bridge-zip.sh
git diff --check
```

- Expected: fixture scoring ثابت؛non-owner denied؛logs/evidence redacted؛parity و duplicate inventory سبز.

## Manual tests با environment/data/steps/expected

- دو user synthetic؛یک test fixture و یک support ticket بساز؛submit/result/view-non-owner و ticket reply را در دو mode اجرا کن.
- Expected: score fixture صحیح،result/ticket خصوصی،UI/API مطابق manifest؛سپس AWAITING_MANUAL_QA.

## Acceptance Criteria

- [ ] scoring fixtures و ownership tests سبزند.
- [ ] یک source canonical و parity دو artifact وجود دارد.
- [ ] privacy/export/erase رفتار مستند و آزموده شده است.
- [ ] QA و privacy review تأیید شده است.

## Security/Privacy/Migration checks

- PHI classification،IDOR،rate limit،redaction،retention و opt-in cleanup بررسی شود.

## Evidence

- `docs/evidence/P04-WORDPRESS-CODE-029/`: fixtures،permission/privacy reports،commands و QA redacted.

## Rollback

adapter switch بدون حذف answer/result/ticket؛در schema change فقط forward-fix/backup تأییدشده.

## Completion record

- Started/Completed at:
- Changed files:
- Commands and exit codes:
- Manual/privacy reviewer/date/result:
- Evidence paths:
- Remaining risks/blockers:
- Final status: TODO | CODE_COMPLETE | AWAITING_MANUAL_QA | DONE | BLOCKED
