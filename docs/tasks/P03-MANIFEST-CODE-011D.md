# P03-MANIFEST-CODE-011D — coordinator و ترتیب منابع bootstrap

- Status: TODO
- Owner: AI
- Depends on: P03-MANIFEST-CODE-011C
- Blocks: P03-MANIFEST-CODE-011E
- Size: M

## هدف

coordinator مستقل از UI ترتیب local → remote معتبر → last-known-good محدود را اعمال و stateهای loading/ready/error/retry را تولید کند.

## پذیرش

- remote بالاتر از compiled ceiling فعال نمی‌شود.
- خطای remote بدون از دست‌دادن fallback امن نمایش‌پذیر است.
- retry تنها remote را دوباره فراخوانی می‌کند.
- Evidence در `docs/evidence/P03-MANIFEST-CODE-011D/` ثبت شود.

## تکمیل

- Commands and exit codes:
- Evidence paths:
- Final status: TODO | DONE | BLOCKED
