# P02-CORE-CODE-015A — تست‌های معماری برای جلوگیری از import معکوس بین ماژول‌های Feature

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
P02-CORE-CODE-015A

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
- Priority/Risk/Size: P1/MEDIUM / S
- Owner: AI
- Completion authority: BOTH
- Depends on: P02-ARCH-CODE-014
- Blocks: P02-CORE-CODE-015B
- Requirement source: شکستن تسک P02-CORE-CODE-015 به زیرتسک‌ها

## هدف قابل اندازه‌گیری
ایجاد تست معماری (با استفاده از ابزاری مثل Konsist یا یک اسکریپت بیلد/تست ساده) که تضمین کند ماژول‌های Feature (به‌خصوص profile و admin) به یکدیگر وابستگی ندارند (import معکوس یا حلقوی).

## خروجی مورد انتظار
اجرای موفقیت آمیز تست‌ها (و در صورت وجود تخلفات فعلی، ثبت آن‌ها در یک Baseline یا Exclude موقت تا در تسک بعدی برطرف شوند).

## خارج از محدوده
- حل وابستگی‌های فعلی در profile و admin (این موارد در 015B حل می‌شوند).

## Allowed files/directories
- build-logic/**
- core/**
- docs/**

## Evidence
- مسیر: docs/evidence/P02-CORE-CODE-015A/

## Completion record
- Started at:
- Completed at:
- Changed files:
- Commands and exit codes:
- Manual tester/date/result:
- Evidence paths:
- Remaining risks/blockers:
- Final status: TODO | CODE_COMPLETE | AWAITING_MANUAL_QA | IN_REVIEW | DONE | BLOCKED
