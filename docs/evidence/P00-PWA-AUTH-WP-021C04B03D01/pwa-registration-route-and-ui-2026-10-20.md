# PWA registration route and compact-layout correction

Date: 2026-10-20. Scope: disposable localhost WordPress/PWA only.

## Correction

The JS client was selecting the inactive browser-session route for password
login and registration. The active WordPress Bridge contract exposes the
guarded direct routes instead. `AuthApiImpl` now uses the direct
`api/auth/login` and `api/auth/register` paths, matching the already-correct
OTP exchange path.

The authentication forms now scroll on a compact viewport. The password mask
uses an ASCII asterisk rather than a glyph that is missing from the deployed
web font.

## Verification

- Kotlin/JS source and test source compilation passed.
- A disposable direct registration request returned HTTP 201; an immediate
  duplicate request returned HTTP 409. No email address, password, token, or
  user identifier is recorded here.
- The development PWA bundle was rebuilt and copied to the local QA site.
- A browser inspection confirmed that the rebuilt registration form renders
  the account, OTP-mode, and existing-account actions.

## Remaining owner QA

This card remains `AWAITING_MANUAL_QA`. The owner must submit the browser
registration flow once with a new synthetic identity, then verify that a
second registration using the same identity is rejected and that the compact
page can scroll to every action. No retained site or personal data is in
scope.
