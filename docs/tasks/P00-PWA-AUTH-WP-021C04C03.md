# P00-PWA-AUTH-WP-021C04C03 — atomic OTP route manual QA

- Status: AWAITING_MANUAL_QA; Size: S; Owner: BOTH; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C04C` (owner-authorized limited integration only).
- Verify only against a disposable WordPress build configured **without** a persistent object cache. Do not use a retained site, personal mobile number, or real SMS provider.

## Manual QA

1. The disposable localhost site now has the local-only test sink enabled. Open
   `Carmilla → کد آزمایشی ورود` in WordPress admin, then open the PWA login
   screen that points only to that site.
2. Request a login code for a new synthetic test mobile and submit that exact code once. Confirm the app enters an authenticated state.
3. Sign out locally and submit the exact same code again. Confirm it is rejected and no profile or token-backed session appears.
4. Request a new code for a different synthetic mobile. Submit an incorrect six-digit value three times, then submit the correct code. Confirm the correct code is rejected after the third failed attempt.
5. If the disposable site enables Redis/Memcached/object-cache drop-in, request a code once. Confirm the request reports that OTP is temporarily unavailable; it must not report success or send a code.
6. Record only pass/fail and HTTP status. Do not store codes, tokens, credentials, or personal mobile numbers.

Success means a code works exactly once, three invalid attempts lock it, and unsupported persistent-cache mode fails closed. This QA does not complete the blocked identity/deletion parent or prove compatibility with a production cache topology.

## Local sink verification — 2026-10-20

The sink is hard-limited to a localhost WordPress installation and an explicit
local option. It records no account index and displays a code only after an
administrator supplies the synthetic mobile number. Disposable route testing
passed code capture, one successful use and rejected reuse; see
[verification](../evidence/P00-PWA-AUTH-WP-021C04C03/local-sink-verification.md).
