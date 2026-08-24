# P04-WPPLUGIN-GATE-024 — Gate مستقل Carmilla Bridge/App Builder

## Prompt اجرای همین Task

```text
نقش تو Implementer و Verifier فقط همین Task است.
Repository: D:\Android\AndroidStudioProjects\kmp-shop
Master checklist: D:\Android\AndroidStudioProjects\kmp-shop\docs\MASTER_IMPLEMENTATION_CHECKLIST_FA.md
Source audit: D:\Android\AndroidStudioProjects\kmp-shop\docs\PROJECT_AUDIT_AND_PUBLICATION_PLAN_FA.md
Task ID: P04-WPPLUGIN-GATE-024

قبل از تغییر AGENTS.md،Task و dependencyها را بخوان؛git status و baseline evidence را ثبت کن.
فقط Gate را ارزیابی کن؛کد،dependency،contract،production و داده واقعی را تغییر نده.
هر معیار فاقد Evidence برابر FAIL/NOT_EVALUATED است؛تست دستی اجرا‌نشده را تیک نزن.
بدون authority انسانی DONE نکن و به Task بعدی نرو.
پاسخ نهایی: Outcome،Evidence reviewed،Manual QA،Acceptance،Checklist change،Risks و Rollback.
```

- Status: TODO
- Phase/Area/Type: P04 / WPPLUGIN / GATE
- Priority/Risk/Size: P0 / HIGH / S
- Owner: HUMAN
- Completion authority: HUMAN Product Owner + WordPress/QA Lead
- Depends on: P04-WPPLUGIN-DOC-023، P04-WPPLUGIN-MANUAL-034
- Blocks: P04-WORDPRESS-GATE-037
- Requirement source: Master row P04-WPPLUGIN-GATE-024 و dual-standalone product contract

## هدف قابل اندازه‌گیری

استقلال Bridge/App Builder از Carmilla Theme و آمادگی artifact برای RC داخلی بر اساس Evidence ارزیابی شود.

## خروجی مورد انتظار

تصمیم `PASS` یا `FAIL` امضاشده با defect/blockerها؛ این Gate مالک bugهای presentation قالب مانند Elementor نیست.

## خارج از محدوده

- اصلاح کد یا بستن defect بدون Task جدا.
- ارزیابی نهایی Theme standalone یا coexistence که Gateهای جدا دارند.
- انتشار marketplace/production.

## Preconditions

- dependencyها DONE و Evidence ماتریس Bridge-only موجود باشد.
- ZIP Bridge همان artifact تست‌شده،versioned و checksumدار باشد.

## Allowed files/directories

- `docs/evidence/P04-WPPLUGIN-GATE-024/**`
- status/checkbox همین Gate در `docs/**`

## Forbidden actions

- تغییر source،ساخت artifact جدید،نادیده‌گرفتن P0 یا تأیید شفاهی بدون Evidence.

## مراحل پیاده‌سازی

1. dependency/status و hash artifact را تطبیق بده.
2. نتایج Storefront و قالب ثالث،Android/PWA،REST/CRUD،security/privacy و lifecycle را review کن.
3. بررسی کن App Builder فقط control plane است و native build روی WordPress اجرا نمی‌شود.
4. defectهای باز را بر اساس severity فهرست کن.
5. authority انسانی تصمیم PASS/FAIL را ثبت کند.

## Automated tests با command و expected result

- reports `P04-QA-AUTO-020` و CI باید سبز،قابل رهگیری و متعلق به همان checksum باشند؛ اجرای مجدد بدون دلیل لازم نیست.

## Manual tests با environment/data/steps/expected

- Evidence تسک `P04-WPPLUGIN-MANUAL-034` روی Storefront و Theme ثالث review شود.
- Expected: activation و CRUD/sync بدون Carmilla،navigation مطابق manifest،data loss/fatal/P0 صفر.

## Acceptance Criteria

- [ ] Bridge ZIP بدون Carmilla Theme نصب/فعال شده است.
- [ ] قرارداد feature/client روی Android و PWA تأیید شده است.
- [ ] security/privacy/lifecycle و any-theme compatibility Evidence دارند.
- [ ] P0 باز صفر و تصمیم انسانی ثبت شده است.

## Security/Privacy/Migration checks

- wildcard CORS با credentials،permission callback باز،secret در diagnostics یا migration بدون rollback قابل قبول نیست.

## Evidence

- `docs/evidence/P04-WPPLUGIN-GATE-024/`: decision،artifact hash،linked reports،reviewers و open-risk register.

## Rollback

Gate فقط تصمیم مستندی است؛در FAIL artifact منتشر نشود و defectها به Taskهای مستقل برگردند.

## Completion record

- Reviewed at:
- Artifact/version/checksum:
- Reviewers:
- Evidence paths:
- Decision/reason:
- Remaining risks/blockers:
- Final status: TODO | AWAITING_MANUAL_QA | DONE | BLOCKED
