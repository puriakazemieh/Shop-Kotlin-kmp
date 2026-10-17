# P00-PWA-AUTH-WP-021C04B05 — guarded browser registration

- Status: TODO; Size: M; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C04B04` owner confirmation.
- Implement and test isolated guarded browser registration according to the accepted safety contract; honor WordPress policies and filters, enforce identity ownership, handle partial account creation truthfully, preserve legacy mobile behavior and never expose refresh credentials. No public route, live hook or mutation to preserved sites.
- Test real WordPress and MariaDB concurrency, collisions, invalid requests, failures and redaction; record evidence under `docs/evidence/P00-PWA-AUTH-WP-021C04B05/`. Actual network and browser QA remain separate tasks.
