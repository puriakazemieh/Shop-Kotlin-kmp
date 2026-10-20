# Atomic OTP route integration — synthetic verification

2026-10-20. The existing WordPress OTP routes now use `CB_OTP_Atomic_Consumer` as their only successful verification path. A code is authorized only when the database deletes the exact, unexpired transient row. Persistent external object-cache mode is rejected before issuing a code and verification also fails closed in that mode.

## Automated evidence

On isolated disposable MariaDB data, the route-level test passed on WordPress 6.4.3/PHP 8.1.27, WordPress 6.6.2/PHP 8.2.25, and WordPress 7.1.3/PHP 8.3.35. It verified successful first use, rejected sequential reuse without tokens, locked a code after three incorrect attempts, and rejected issuance when persistent object-cache mode was enabled. The underlying consumer test ran on the same three combinations and released two independent processes after both had verified the same code: exactly one consumed it. It also rejected malformed, expired, unsupported-cache, database read-failure, and database delete-failure cases.

All tests ran with a random `qa_..._` table prefix in the disposable Docker database. No retained WordPress site, real mobile number, OTP, credential, token, SMS provider, or public route was used.

## Remaining boundary

This limited integration does not complete parent `021C04C`: its dependency `021C04B` is still blocked by the broader identity/deletion safety work. It also does not support persistent object caches; administrators must keep this OTP route disabled until a compatible atomic storage design exists. Human browser/PWA confirmation is tracked separately in `021C04C03`.
