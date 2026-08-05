# Evidence Summary: P00-ARCH-ADR-004

## Task Outcome
The Architectural Decision Record (ADR) for Hybrid Tenancy, Backend Profiles, and Feature Manifest has been drafted and is ready for approval.
**خلاصه فارسی**: معماری هسته پروژه بازتعریف شد. تمام واریانت‌های بیلد به دو پروفایل اصلی (`WORDPRESS` و `SPRING`) محدود شدند. مدل «مانیفست قابلیت‌ها» برای کنترل پویای دسترسی‌ها معرفی گردید و مرز دقیق میان پوسته وردپرس (صرفاً نمایش) و افزونه هسته (منطق و داده) تعیین شد.

## Metadata
- **Timestamp**: 2026-08-05T11:48:00.000000000+03:30
- **Executor**: AI
- **Status**: CODE_COMPLETE (Awaiting Manual Approval)

## Files Created/Modified
- `docs/architecture/adr/ADR-002-HYBRID-ARCHITECTURE-TENANCY-MANIFEST.md` (NEW)

## Decision Highlights
1. **Collapsed Profiles**: Only `WORDPRESS` and `SPRING` build profiles remain.
2. **Dynamic Capabilities**: Verticals are enabled via a hybrid Manifest model.
3. **Clean WordPress Split**: Core Plugin owns data/logic; Theme is presentation-only.
4. **Overlay Strategy**: Customer customization is externalized from the core repo.

## Manual Action Required
1. Review [ADR-002](file:///D:/Android/AndroidStudioProjects/kmp-shop/docs/architecture/adr/ADR-002-HYBRID-ARCHITECTURE-TENANCY-MANIFEST.md).
2. Sign-off by changing the status in `docs/tasks.md` to `DONE` after review.
