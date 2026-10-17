# P00-PWA-AUTH-WP-021C04 — isolated web login and registration handlers

- Status: READY; Size: M; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C03`.
- Implement and test unregistered handlers/services for web login, registration and OTP login using the reviewed store, guard and browser token helpers. Use WordPress authentication filters and preserve existing mobile behavior. No refresh credential or session identifier may appear in JSON, URL or logs; failures must not issue credentials, and partial user creation must be reported accurately without deleting the account.
- Do not add any route, hook, cookie emission or live HTTP behavior; exercise the handlers through isolated synthetic tests until all safeguards are integrated together. Test valid/invalid login and OTP, duplicate and failed registration, storage failure and response redaction. If the scope exceeds M, split before implementation.
- Record redacted evidence in `docs/evidence/P00-PWA-AUTH-WP-021C04/`. Network activation and owner manual QA remain `021C07`.
