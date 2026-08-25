# P01-SECURITY-CODE-004 — Evidence

- Release manifest: `usesCleartextTraffic=false`.
- Production network config: cleartext globally disabled; tunnel/domain allowlist حذف شد.
- Debug-only overlay: HTTP فقط برای emulator `10.0.2.2` مجاز است.
- Manual QA: روی debug اتصال به `http://10.0.2.2` را بررسی کنید؛ سپس روی release، HTTP باید fail و HTTPS با certificate معتبر باید pass شود.
- Rollback: حذف overlay debug یا بازگرداندن manifest قبلی؛ cleartext release نباید دوباره فعال شود.
