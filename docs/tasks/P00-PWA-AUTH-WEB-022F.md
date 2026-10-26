# P00-PWA-AUTH-WEB-022F — browser guarded credential endpoints

- Status: DONE; Size: S; Owner: BOTH; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021`.
- For the JavaScript/PWA target only, send password login, registration, forgotten-password and password-reset requests to the guarded `/api/auth/web/*` WordPress contract. Keep native clients and direct mobile-OTP endpoints unchanged until a reviewed browser OTP endpoint exists.
- Add a browser-level request test proving the guarded routes are used and that the mobile OTP route is not silently rewritten. Preserve the request-header and cookie requirements supplied by `WebAuthRequestPlugin`.
- Implementation is ready for owner confirmation. The focused browser test is present; the local test runner could not replace its stale `build/test-results/.../output.bin` because another Windows process held that generated file. Kotlin/JS compilation and the PWA webpack build both completed successfully. See [evidence](../evidence/P00-PWA-AUTH-WEB-022F/evidence-map.md).

## Completion — 2026-10-26

The owner confirmed successful password login in the isolated WordPress PWA fixture and confirmed that the login remained active after reload. This completes the guarded credential-route acceptance for this focused card only. Startup restoration, missing/expired-cookie behavior, logout and cross-tab behavior remain scoped to the following lifecycle cards.
