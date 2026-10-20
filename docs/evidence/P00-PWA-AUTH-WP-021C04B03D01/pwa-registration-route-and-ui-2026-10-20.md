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

## Owner QA result

On 2026-10-20 the owner confirmed that the supplied disposable-PWA registration
flow and the compact form behavior passed. No identity value, credential,
code or token was retained in this evidence.
