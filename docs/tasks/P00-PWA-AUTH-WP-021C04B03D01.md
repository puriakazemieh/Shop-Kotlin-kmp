# P00-PWA-AUTH-WP-021C04B03D01 — supported writer API manual QA

- Status: DONE; Size: S; Owner: BOTH; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C04B03D` (owner-authorized limited integration only).
- The supported writer is now used by the existing password and first-OTP registration paths. Verify only with a disposable WordPress build and a new PWA build pointing to it; do not use a retained customer account or production site.

## Manual QA

1. Open the PWA login screen against the disposable WordPress test site and create one account using a new ASCII email and a new test mobile number.
2. Confirm that the app enters the authenticated state only after the successful registration response.
3. Log out locally, then try creating a second account using the same test mobile and a different email.
4. Confirm that the second request reports a duplicate/registration failure, stays unauthenticated, and does not show a profile or token-backed session.
5. Record only the status/result; do not paste credentials, tokens, mobile numbers, or screenshots containing them into evidence.

Success means the first account registers once, the second request cannot authenticate, and no retained WordPress installation was used. This QA does not prove migration, external-writer protection, native deletion recovery, or the parent B03D acceptance criteria.

## Owner confirmation — 2026-10-20

The owner confirmed the supplied disposable-PWA registration check passed.
The implementation evidence records a successful initial registration and a
rejected duplicate response without retaining identity values, credentials or
tokens. This closes this child QA card only; it does not close its broader
identity/deletion parent.
