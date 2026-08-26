# P02-CORE-CODE-015B — حل Dependency Inversion‌های ماژول‌های Profile و Admin

## Prompt اجرای همین Task

```text
نقش تو Implementer و Verifier فقط همین Task است.

Repository:
D:\Android\AndroidStudioProjects\kmp-shop

Master checklist:
D:\Android\AndroidStudioProjects\kmp-shop\docs\MASTER_IMPLEMENTATION_CHECKLIST_FA.md

Source audit:
D:\Android\AndroidStudioProjects\kmp-shop\docs\PROJECT_AUDIT_AND_PUBLICATION_PLAN_FA.md

Task ID:
P02-CORE-CODE-015B

قبل از تغییر:
1. AGENTS.md و هر دستور ارجاع‌شده‌ای که واقعاً وجود دارد را بخوان.
2. Task، dependency، scope، acceptance و source reference را کامل بخوان.
3. git status را بررسی و تغییرات موجود کاربر را حفظ کن.
4. baseline test مشخص‌شده را اجرا کن.

قواعد:
- فقط همین Task را انجام بده.
- کمترین diff لازم را بساز.
- خارج از Allowed scope تغییر نده.
- dependency upgrade،refactor جانبی یا تغییر API contract انجام نده.
- secret یا داده واقعی ایجاد/ثبت نکن.

شرایط توقف:
- تداخل با تغییرات حل‌نشده کاربر

پاسخ نهایی: Outcome،Changed files،Automated tests،Manual test status،Acceptance Criteria،Evidence paths،Checklist status change،Remaining risks/blockers و Rollback instructions.
```

- Status: TODO
- Phase/Area/Type: P02 / CORE / CODE
- Priority/Risk/Size: P1/MEDIUM / M
- Owner: AI
- Completion authority: BOTH
- Depends on: P02-CORE-CODE-015A
- Blocks: P02-CI-OPS-016
- Requirement source: شکستن تسک P02-CORE-CODE-015 به زیرتسک‌ها

## هدف قابل اندازه‌گیری
حذف وابستگی‌های متقابل (Cross-Feature Dependencies) از ماژول‌های `feature:profile` و `feature:admin:products` به سایر ماژول‌های Feature و استفاده از Navigator Interfaceها یا آرگومان‌های Navigation.

## خروجی مورد انتظار
ماژول‌های `feature:profile` و `feature:admin:products` دیگر در `build.gradle.kts` خود به ماژول‌های Feature وابستگی مستقیم نداشته باشند و تست‌های معماری اضافه‌شده در مرحله قبل با موفقیت اجرا شوند.

## خارج از محدوده
- هر Feature یا ماژول نامرتبط.

## Allowed files/directories
- feature/profile/**
- feature/admin/products/**
- core/**
- docs/**

## Evidence
- مسیر: docs/evidence/P02-CORE-CODE-015B/

## Completion record
- Started at:
- Completed at:
- Changed files:
- Commands and exit codes:
- Manual tester/date/result:
- Evidence paths:
- Remaining risks/blockers:
- Final status: TODO | CODE_COMPLETE | AWAITING_MANUAL_QA | IN_REVIEW | DONE | BLOCKED
