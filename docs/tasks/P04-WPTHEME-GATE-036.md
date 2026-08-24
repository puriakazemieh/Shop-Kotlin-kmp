# P04-WPTHEME-GATE-036 — Gate مستقل Carmilla Theme

## Prompt اجرای همین Task

```text
نقش تو Implementer و Verifier فقط همین Task است.
Repository: D:\Android\AndroidStudioProjects\kmp-shop
Master checklist: D:\Android\AndroidStudioProjects\kmp-shop\docs\MASTER_IMPLEMENTATION_CHECKLIST_FA.md
Source audit: D:\Android\AndroidStudioProjects\kmp-shop\docs\PROJECT_AUDIT_AND_PUBLICATION_PLAN_FA.md
Task ID: P04-WPTHEME-GATE-036
AGENTS.md،dependencyها،artifact checksum و تمام Evidence را review کن.
فقط Gate؛source/artifact/production را تغییر نده؛Evidence ناقص=NOT_EVALUATED/FAIL؛بدون authority انسانی DONE نکن و جلو نرو.
پاسخ نهایی: Outcome،Evidence reviewed،Acceptance،Decision،Checklist،Risks،Rollback.
```

- Status: TODO
- Phase/Area/Type: P04 / WPTHEME / GATE
- Priority/Risk/Size: P0 / HIGH / S
- Owner: HUMAN
- Completion authority: Product Owner + WordPress/QA Lead
- Depends on: P04-WPPLUGIN-DOC-023، P04-QA-MANUAL-021
- Blocks: P04-WORDPRESS-GATE-037
- Requirement source: Master row P04-WPTHEME-GATE-036 و Theme standalone contract

## هدف قابل اندازه‌گیری

آمادگی ZIP مستقل Carmilla Theme برای RC داخلی بر اساس feature parity،Elementor/Woo compatibility،QA و P0 defects ارزیابی شود.

## خروجی مورد انتظار

تصمیم امضاشده PASS/FAIL برای Theme SKU بدون اتکا به Bridge.

## خارج از محدوده

- اصلاح کد،Bridge Gate،coexistence final Gate و marketplace publish.

## Preconditions

- Theme artifact checksumدار و docs/automated/manual Evidence همان artifact موجود باشد.

## Allowed files/directories

- `docs/evidence/P04-WPTHEME-GATE-036/**`
- status/checkbox همین Gate در `docs/**`

## Forbidden actions

- تغییر source،نادیده‌گرفتن P0،اعلام PASS بدون Theme-only UAT یا استفاده از Bridge برای پوشاندن feature ناقص.

## مراحل پیاده‌سازی

1. dependencyها و artifact hash را تطبیق بده.
2. feature matrix،Theme-only install،Elementor،Woo،RTL/accessibility/privacy و lifecycle reports را review کن.
3. defect/open-riskها را severity بده.
4. authority انسانی PASS/FAIL را ثبت کند.

## Automated tests با command و expected result

- CI و `P04-QA-AUTO-020` برای همان checksum باید سبز باشند.

## Manual tests با environment/data/steps/expected

- Evidence `P04-QA-MANUAL-021` و `P04-WPTHEME-CODE-025` review شود.
- Expected: بدون Bridge همه capabilityهای enabled کار کنند و P0/fatal/data loss صفر باشد.

## Acceptance Criteria

- [ ] Theme-only feature parity و install/upgrade اثبات شده است.
- [ ] Elementor/Woo/RTL/accessibility Evidence سبز است.
- [ ] Bridge dependency صفر و P0 باز صفر است.
- [ ] decision انسانی ثبت شده است.

## Security/Privacy/Migration checks

- nonce/capability/privacy/retention و عدم حذف داده هنگام Theme switch تأیید شود.

## Evidence

- `docs/evidence/P04-WPTHEME-GATE-036/`: decision،artifact hash،linked reports،reviewers و risks.

## Rollback

در FAIL انتشار متوقف و defect Task ساخته شود؛Gate کد را rollback نمی‌کند.

## Completion record

- Reviewed at:
- Artifact/version/checksum:
- Reviewers:
- Evidence paths:
- Decision/reason:
- Remaining risks/blockers:
- Final status: TODO | AWAITING_MANUAL_QA | DONE | BLOCKED
