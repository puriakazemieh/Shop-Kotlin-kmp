# OTP error-message remediation

Date: 2026-10-20. Scope: disposable localhost WordPress/PWA only.

## Finding

The WordPress endpoint correctly rejects an invalid, expired or locked login
code with HTTP 401 and the stable error code `BAD_OTP`. The client did not map
that error code, so it displayed the generic unknown-error message.

The owner confirmed the three-invalid-attempt lockout behavior itself worked
on the disposable PWA. The observed defect was only the client-facing error
message.

## Correction

The shared client error mapping now maps `BAD_OTP` to the existing localized
message: «کد تایید اشتباه یا منقضی شده است». This remains deliberately
generic about whether a mobile identity exists and whether the code was
consumed, expired or locked.

## Verification

- Kotlin/JS compilation for the shared mapping and authentication feature passed.
- The PWA bundle was rebuilt and deployed to the disposable local QA site.
- A synthetic invalid-code request returned HTTP 401 with `BAD_OTP`.
- The deployed bundle contains the `BAD_OTP` mapping and the PWA cache version
  was advanced so the browser fetches the new bundle.

No mobile number, OTP value, token, credential or user identifier is recorded.
The card remains `AWAITING_MANUAL_QA` until the owner sees the corrected
localized message in the PWA.
