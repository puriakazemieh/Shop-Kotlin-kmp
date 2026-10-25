# P00-PWA-AUTH-WP-021C07C — protected-route network matrix and owner handoff

- Status: READY; Size: M; Owner: BOTH; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C07B`.
- Run the complete route-network matrix on an explicitly labelled synthetic WordPress fixture: all seven browser-only routes, OPTIONS/POST/method/origin/duplicate-header handling, local TLS exception only where configured, CORS/no-store/cookie behavior, refresh replay, logout, password change and failures. Verify mobile endpoints retain their original contract.
- Prepare a safe deployable QA configuration and exact owner instructions: URL, synthetic account/setup, how to inspect browser cookies/network requests, and success criteria. Keep this card `AWAITING_MANUAL_QA` until the owner confirms. Evidence goes in `docs/evidence/P00-PWA-AUTH-WP-021C07C/`; PWA lifecycle remains `WEB-022`/`QA-023`.
