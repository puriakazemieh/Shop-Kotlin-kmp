# P03-MANIFEST-CODE-011C — last-known-good محدود و namespaced

- Status: TODO
- Owner: AI
- Depends on: P03-MANIFEST-CODE-011B
- Blocks: P03-MANIFEST-CODE-011D
- Size: M

## هدف

manifest معتبر remote با expiry محدود و namespace backend/tenant ذخیره شود و دادهٔ stale یا نامعتبر دوباره فعال نشود.

## پذیرش

- cache فقط پس از validation کامل نوشته می‌شود.
- تغییر backend یا tenant cache قبلی را مصرف نمی‌کند.
- expiry و invalidation در test واحد اثبات می‌شوند.
- Evidence در `docs/evidence/P03-MANIFEST-CODE-011C/` ثبت شود.

## تکمیل

- Commands and exit codes:
- Evidence paths:
- Final status: TODO | DONE | BLOCKED
