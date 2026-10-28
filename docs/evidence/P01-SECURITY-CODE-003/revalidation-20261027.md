# P01-SECURITY-CODE-003 — Local revalidation (2026-10-27)

## Scope and safety

- Environment: local WordPress fixture at `http://localhost:8091`; no production service, customer data, or persistent test credential was used.
- A one-time synthetic local user was created only for this check and deleted after the test. Its identifier, password, token, cookie, and response body are intentionally not recorded.

## Observations

1. The browser PWA loaded at `http://localhost:8091/pwa/#/login`.
2. Browser-console review after reload found 37 entries and no entry matching the sensitive-field markers `Authorization`, `Bearer`, access/refresh token, `OTP`, password, payment, or the synthetic-account marker. One non-sensitive client error was present: `Fail to fetch`.
3. A direct local browser-auth request with the required non-simple request header reached the fixture and returned HTTP 200. The response had the expected high-level fields (`accessToken`, `user`) and an HttpOnly session cookie; their values were deliberately neither printed nor saved.
4. The same request without the required `X-Carmilla-Web-Request: 1` header returned HTTP 403, confirming that the local endpoint's request-policy guard was active.

## Automated verification

- Command: `.\\gradlew.bat :core:network:jvmTest --tests "com.kazemieh.network.common.ClientLoggingPolicyTest"`
- Working directory: repository root
- Result: exit code 0 (`BUILD SUCCESSFUL`). The focused logging-policy test passed.

## Result and remaining manual-QA condition

- **Partial pass:** the endpoint policy and the inspected error-path console did not expose the tested sensitive markers.
- **Not a completion pass:** the PWA's actual login/OTP flow could not be exercised because it failed before authentication with `Fail to fetch`. Consequently, token/OTP/payment/health-field redaction cannot be claimed for a successful in-app flow.
- Status remains `AWAITING_MANUAL_QA`. The failure must be resolved or reproduced with a working Android/Web client, then the real success and error flows must be inspected again without retaining credentials, tokens, OTPs, payment payloads, or health data.

## Cleanup

- The temporary local user was deleted immediately after the direct-auth check.
- The local temporary credential artifact was removed.
