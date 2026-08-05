# Evidence Summary: P00-PROGRAM-ADR-003

## Task Outcome
The scope for the initial release has been defined and documented in a new ADR (Architecture Decision Record).
**خلاصه فارسی**: تصمیم بر این شد که نسخه ۱.۰ به صورت متمرکز فقط شامل بخش «فروشگاه» با بک‌اِند وردپرس (پوسته + PWA) باشد. ماژول‌های پیچیده (مانند LMS و کلینیک) و سرور Spring صراحتاً به نسخه‌های بعدی منتقل شدند تا پایداری و امنیت اولین عرضه تضمین شود.

## Metadata
- **Timestamp**: 2026-07-29T16:07:00.000000000+03:30
- **Executor**: AI
- **Status**: CODE_COMPLETE (Awaiting Manual Review)

## Files Created/Modified
- `docs/architecture/adr/ADR-001-INITIAL-RELEASE-SCOPE.md` (NEW)

## Decision Summary
The release v1.0 will focus exclusively on:
1. **Shop-only** vertical.
2. **WordPress** backend.
3. **Theme + PWA** outputs.

All other verticals (LMS, Clinic) and platforms (Native Mobile, Desktop, Spring) are explicitly moved to the future roadmap to ensure a stable and secure first launch.

## Manual Verification Required
1. Review the content of [ADR-001](file:///D:/Android/AndroidStudioProjects/kmp-shop/docs/architecture/adr/ADR-001-INITIAL-RELEASE-SCOPE.md).
2. Confirm if the "Shop-only" strategy aligns with your business goals for the first release.
