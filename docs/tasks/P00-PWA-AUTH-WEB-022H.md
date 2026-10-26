# P00-PWA-AUTH-WEB-022H — browser sign-out and multi-tab lifecycle

- Status: BLOCKED; Size: M; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WEB-022G`.
- Wire browser sign-out to the guarded server logout before local state is cleared, fail safely when server revocation cannot be confirmed, and coordinate auth-state changes across tabs without exposing credentials. Verify logout does not trigger a refresh retry and leaves protected routes inaccessible after reload.
- Preserve the existing native sign-out behavior. Add automated coverage and a focused manual QA step.
