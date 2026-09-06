<div dir="rtl" align="right">

# P04-WORDPRESS-GATE-037 — Gate سازگاری دو ZIP مستقل و نصب هم‌زمان

## Prompt اجرای همین Task

```text
نقش تو Implementer و Verifier فقط همین Task است.
Repository: D:\Android\AndroidStudioProjects\kmp-shop
Master checklist: D:\Android\AndroidStudioProjects\kmp-shop\docs\MASTER_IMPLEMENTATION_CHECKLIST_FA.md
Source audit: D:\Android\AndroidStudioProjects\kmp-shop\docs\PROJECT_AUDIT_AND_PUBLICATION_PLAN_FA.md
Task ID: P04-WORDPRESS-GATE-037
AGENTS.md،سه dependency Gate/UAT،artifact checksums و Evidence را review کن.
فقط تصمیم Gate؛source/artifact/production/publish را تغییر نده؛Evidence ناقص=FAIL/NOT_EVALUATED؛بدون authority انسانی DONE نکن و جلو نرو.
پاسخ نهایی: Outcome،Evidence reviewed،Acceptance،Decision،Checklist،Risks،Rollback.
```

- Status: TODO
- Phase/Area/Type: P04 / WORDPRESS / GATE
- Priority/Risk/Size: P0 / CRITICAL / S
- Owner: HUMAN
- Completion authority: Product Owner + Architecture + Security + QA Leads
- Depends on: P04-WPTHEME-GATE-036, P04-WPPLUGIN-GATE-024, P04-WORDPRESS-MANUAL-035
- Blocks: P05-PAYMENT-ADR-001, P12-BUILDER-ADR-001, P13-LMS-DISC-001, P14-CLINIC-DISC-001, P18-QA-AUTO-001
- Requirement source: Master row P04-WORDPRESS-GATE-037 و Phase 4 Gate

## هدف قابل اندازه‌گیری

هم‌زیستی دو محصول و قالب/افزونه مستقل با هسته و entitlement سازگار را بر همان artifactهای SKU ارزیابی کن؛ داده/route/cron/job canonical باشند.

## خروجی مورد انتظار

هر سه mode،دو ترتیب نصب/upgrade و SKU متفاوت با شواهد؛ بدون duplicate/data loss؛ حفظ خدمات API مستقل از روشن‌بودن builder؛ این Gate پایان همه قابلیت‌ها و پلتفرم‌ها نیست.

## خارج از محدوده

- publish ژاکت/راست‌چین،provider production،اصلاح defect یا تغییر roadmap بعدی.

## Preconditions

- Theme Gate و Bridge Gate PASS؛co-install/upgrade/rollback UAT پاس؛همه artifactها همان checksumهای Evidence.

## Allowed files/directories

- `docs/evidence/P04-WORDPRESS-GATE-037/**`
- status/checkbox همین Gate و readiness `P05-PAYMENT-ADR-001` در `docs/**`

## Forbidden actions

- تغییر source،release/publish واقعی،تغییر بیش از status همین Gate/Task بعدی یا پذیرش risk بحرانی بدون owner/date.

## مراحل پیاده‌سازی

1. قرارداد `docs/INDEPENDENT_PRODUCTS_SPEC_FA.md` و مانیفست بسته را با inventory ZIP همین SKU تطبیق بده؛ نتیجه این کنترل را همراه مراحل زیر ثبت کن.

1. سه dependency و checksums را verify کن.
2. Theme-only،Bridge-only،both،upgrade/mismatch/rollback و security/privacy reports را review کن.
3. feature parity و App Builder control-plane boundary را تطبیق بده.
4. P0/P1 defects و residual risks را review کن.
5. authorityها PASS/FAIL و شرط ورود به P05 را امضا کنند.

## Automated tests با command و expected result

- همه CI/matrix reports برای همان commit/artifact سبز،قابل رهگیری و بدون skipped blocker باشند.

## Manual tests با environment/data/steps/expected

- UATهای Theme،Bridge و coexistence با sign-off موجود باشند.
- Expected: duplicate/data loss/fatal/P0 صفر؛rollback recoverable؛دو محصول مستقل.

## Acceptance Criteria

- [ ] هر سه mode،دو ترتیب نصب/upgrade و SKU متفاوت با شواهد؛ بدون duplicate/data loss؛ حفظ خدمات API مستقل از روشن‌بودن builder؛ این Gate پایان همه قابلیت‌ها و پلتفرم‌ها نیست.

- [ ] هر سه mode و upgrade/rollback پاس‌اند.
- [ ] دو Gate محصول PASS هستند.
- [ ] ZIPها reproducible/versioned/checksumدارند.
- [ ] P0 صفر و risk register دارای owner/date است.
- [ ] تصمیم جمعی و readiness فاز بعد ثبت شده است.

## Security/Privacy/Migration checks

- release blockers امنیت،health privacy،CORS/auth/IDOR،migration rollback و secret scan بدون waiver نامعتبر پاس باشند.

## Evidence

- `docs/evidence/P04-WORDPRESS-GATE-037/`: final decision،checksums،linked reports،risk register و signatures.

## Rollback

در FAIL فاز ۵ READY نشود؛defectها به Task مستقل برگردند و RC منتشر نشود.

## Completion record

- Reviewed at:
- Commit/artifact checksums:
- Reviewers/signatures:
- Evidence paths:
- Decision/reason:
- Remaining risks/blockers:
- Final status: TODO | AWAITING_MANUAL_QA | DONE | BLOCKED

</div>
