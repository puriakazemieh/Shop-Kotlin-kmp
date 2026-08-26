# P03-MANIFEST-CODE-011E — اتصال DI و UI پیش از NavHost

- Status: TODO
- Owner: AI
- Depends on: P03-MANIFEST-CODE-011D
- Blocks: P03-MANIFEST-CODE-012
- Size: M

## هدف

coordinator پیش از `AppNavHost` به DI و Compose متصل شود و loading/error/retry UX قابل‌دسترسی فراهم شود.

## پذیرش

- NavHost فقط بعد از state امن bootstrap نمایش داده می‌شود.
- retry و error UI در test Compose یا بازبینی خودکار پوشش دارند.
- سناریوی دستی با دادهٔ synthetic به `P03-QA-MANUAL-020` منتقل و ثبت می‌شود.
- Evidence در `docs/evidence/P03-MANIFEST-CODE-011E/` ثبت شود.

## تکمیل

- Commands and exit codes:
- Evidence paths:
- Final status: TODO | DONE | BLOCKED
