# PWA guarded OTP-route correction

Date: 2026-10-20. Scope: disposable localhost PWA and WordPress only.

## Finding and correction

The PWA client used the browser-session form of the OTP-login path. That
endpoint is deliberately not active in the WordPress Bridge. The guarded
WordPress OTP exchange is exposed only at the direct path
`api/auth/login-with-otp`.

`AuthApiImpl.loginWithOtp` now sends the request to that direct guarded path.
A JS-focused request test asserts the complete URL formed from a browser base
URL. It does not contain a real code, token, mobile number, credential, or
network request.

## Verification performed

1. The disposable WordPress local-OTP smoke check passed: capture one
   synthetic code, accept it once, and reject its reuse (3 checks, 0 failures).
2. The PWA development bundle completed successfully after the change.
3. Kotlin/JS test sources compiled. The browser test runner itself is not a
   passing acceptance signal yet: its webpack setup cannot resolve the existing
   `skiko.mjs` test artifact. This infrastructure fault occurs after test-source
   compilation and is tracked separately from the guarded route assertion.

## Remaining owner confirmation

`P00-PWA-AUTH-WP-021C04C03` remains `AWAITING_MANUAL_QA`. The owner must
perform the disposable browser flow without recording an OTP, token, password,
or personal mobile number: first use succeeds once, reuse is rejected, and
three wrong attempts lock the code. Persistent object-cache mode must report
temporary unavailability rather than issue or accept a code.
