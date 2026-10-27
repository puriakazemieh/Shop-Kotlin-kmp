# P00-PWA-AUTH-WEB-022C — scoped browser web-auth request header

- Status: DONE; Size: S; Owner: BOTH; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WEB-022B` automated client work; owner authorized independent work while backend integration remains blocked.
- For browser POST requests to the configured API's exact `/api/auth/web/` subtree, send the WP contract header `X-Carmilla-Web-Request: 1`. Do not add it to other origins, API paths, methods or native clients. Preserve existing origin/path bearer protections and avoid deriving backend trust from an untrusted request.
- Exercise real compiled browser engine with synthetic Fetch for positive and negative scopes; run JVM regression and app JS/JVM compilation. This is only a client prerequisite: it is not server-side CSRF protection or proof of live WP/Spring compatibility.
- Record evidence and owner manual checks under `docs/evidence/P00-PWA-AUTH-WEB-022C/`; because network behavior changes, remain `AWAITING_MANUAL_QA` until an integrated synthetic backend and owner verification exist.

## Automated work — 2026-10-19

- Implemented the scoped browser-only header and ran six Chrome Headless tests with substituted Fetch, seven JVM tests and the JS/JVM app compilers. [Evidence and owner steps](../evidence/P00-PWA-AUTH-WEB-022C/verification.md).
- No actual backend/preflight test, deployment or owner signoff; server-side guards and the parent remain open.

## Completion — 2026-10-27

- Client-side scoped request header implementation verified with automated tests.
- Superseded and fully integrated through the completed lifecycle chain (`WEB-022F`-`WEB-022H`), parent `WEB-022`, and owner manual verification under `P00-PWA-AUTH-QA-023`. Status marked DONE.
