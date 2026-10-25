# P00-PWA-AUTH-WEB-022A — scope bearer credentials to the API

- Status: BLOCKED; Size: M; Owner: BOTH; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021A`; the owner authorized independent Phase 00 work before the parent dependency is ready.
- Implement the contract's exact scheme, host, port and base-path bearer scope, including 401 challenges and redirects. Exercise real HTTP requests and negative cases with synthetic tokens, and retain expected requests to the configured backend. Do not activate unfinished WordPress web-session routes or claim browser login works.
- Preserve mobile and Spring compatibility for intended API requests. Record limitations and redacted evidence under `docs/evidence/P00-PWA-AUTH-WEB-022A/`.
- Network changes require `AWAITING_MANUAL_QA`; browser acceptance is deferred until a working integration and a deployable test build exist. The parent `WEB-022` and P00 Gate remain open.

## Implementation — 2026-10-18

Origin/path bearer guard and fail-closed JS redirect configuration were implemented. JVM real-local-network and Chrome Headless Fetch-option tests passed; Android compilation did not run to completion because an AAR was absent offline. Blocking browser redirects changes Spring as well and requires compatibility validation. Full limitations and explicit owner QA are in [verification](../evidence/P00-PWA-AUTH-WEB-022A/verification.md). No live PWA deployment or browser-session integration is claimed.
