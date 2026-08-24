# P04-WORDPRESS-CODE-028 — انتقال Clinic/Therapist/Appointment به Shared Core

## Prompt اجرای همین Task

```text
نقش تو Implementer و Verifier فقط همین Task است.
Repository: D:\Android\AndroidStudioProjects\kmp-shop
Master checklist: D:\Android\AndroidStudioProjects\kmp-shop\docs\MASTER_IMPLEMENTATION_CHECKLIST_FA.md
Source audit: D:\Android\AndroidStudioProjects\kmp-shop\docs\PROJECT_AUDIT_AND_PUBLICATION_PLAN_FA.md
Task ID: P04-WORDPRESS-CODE-028
قبل از تغییر AGENTS.md،dependency/scope/acceptance،git status و baseline را بررسی کن؛Size>M یعنی توقف و child Task.
فقط همین Task/Allowed scope/کمترین diff؛بدون secret،PHI واقعی،production،payment واقعی،upgrade یا contract جانبی.
ابتدا characterization؛verification واقعی؛Manual QA اجرا‌نشده را تیک نزن؛بدون Evidence DONE نکن و به Task بعدی نرو.
پاسخ نهایی: Outcome،Changed files،Tests،Manual QA،Acceptance،Evidence،Checklist،Risks،Rollback.
```

- Status: TODO
- Phase/Area/Type: P04 / WORDPRESS / CODE
- Priority/Risk/Size: P0 / HIGH / M
- Owner: BOTH
- Completion authority: BOTH + Security/Privacy reviewer
- Depends on: P04-WORDPRESS-CODE-027
- Blocks: P04-WORDPRESS-CODE-029
- Requirement source: Master row P04-WORDPRESS-CODE-028 و Feature Manifest Clinic

## هدف قابل اندازه‌گیری

Therapist catalog،availability،appointment booking/cancel/status و ownership در Shared Core واحد و transaction-safe شوند.

## خروجی مورد انتظار

Theme-only و Bridge-only یک قرارداد booking و state machine داشته باشند؛double booking،IDOR،duplicate route/CPT و PHI leak صفر.

## خارج از محدوده

- medical advice،video consultation vendor،payment provider و PsychTest scoring.

## Preconditions

- Task READY؛privacy/role/migration foundation DONE؛داده تست کاملاً synthetic.

## Allowed files/directories

- `wordpress/packages/carmilla-core/**`
- فایل‌های clinic/therapist/appointment در دو artifact
- `wordpress/**/tests/**`،`tools/test-env/**`
- `docs/evidence/P04-WORDPRESS-CODE-028/**` و status همین Task

## Forbidden actions

- PHI/PII واقعی،رزرو/payment production،تغییر retention بدون review یا migration destructive.

## مراحل پیاده‌سازی

1. data/state/route/permission inventory و تست race/ownership بساز.
2. state machine و repository/service canonical را در Shared Core تعریف کن.
3. adapterهای Theme/Bridge را متصل و writeهای تکراری را غیرفعال کن.
4. concurrent booking،cancel/retry،timezone و unauthorized access را تست کن.
5. parity و lifecycle را در دو mode ثبت کن.

## Automated tests با command و expected result

```powershell
docker compose -f tools/test-env/docker-compose.yml config
bash wordpress/build-theme-zip.sh
bash wordpress/build-bridge-zip.sh
git diff --check
```

- Expected: state/ownership/concurrency tests سبز؛یک slot بیش از یک رزرو موفق نداشته باشد؛log فاقد PHI.

## Manual tests با environment/data/steps/expected

- دو user و یک therapist/slot synthetic؛رزرو هم‌زمان،مشاهده مالک/غیرمالک،لغو و retry در Theme-only و Bridge-only.
- Expected: یک رزرو موفق،غیرمالک denied،timezone/status صحیح؛سپس AWAITING_MANUAL_QA.

## Acceptance Criteria

- [ ] state machine و ownership canonical است.
- [ ] double-book/IDOR/privacy tests سبزند.
- [ ] parity دو artifact اثبات شده است.
- [ ] QA انسانی و privacy review ثبت شده است.

## Security/Privacy/Migration checks

- least privilege،IDOR،race،retention،redacted logs و migration rollback الزامی.

## Evidence

- `docs/evidence/P04-WORDPRESS-CODE-028/`: state diagram،race/IDOR tests،commands و redacted QA.

## Rollback

forward-fix یا adapter switch؛appointment/slot حذف یا status معکوس نشود؛backup و schema version ثبت شود.

## Completion record

- Started/Completed at:
- Changed files:
- Commands and exit codes:
- Manual/privacy reviewer/date/result:
- Evidence paths:
- Remaining risks/blockers:
- Final status: TODO | CODE_COMPLETE | AWAITING_MANUAL_QA | DONE | BLOCKED
