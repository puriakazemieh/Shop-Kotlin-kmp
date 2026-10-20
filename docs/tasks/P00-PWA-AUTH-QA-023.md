# P00-PWA-AUTH-QA-023 — end-to-end session and P00-T05

- Status: BLOCKED; Size: M; Owner: BOTH; Risk: HIGH
- Depends on: P00-PWA-AUTH-WEB-022.
- Build and deploy only to the preserved local QA environment after backups. Verify real browser login, registration, authenticated requests, refresh, logout, cookie attributes, cross-origin rejection and absence of tokens in persistent browser storage. Test product and cart using synthetic data.
- Keep `AWAITING_MANUAL_QA` until owner confirms precise instructions and observed result; store redacted evidence and build fingerprint. Do not mark payment or release gates complete.
