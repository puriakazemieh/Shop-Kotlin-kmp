# P00-PWA-AUTH-WP-021C04B05 — guarded browser registration

- Status: DONE; Size: M; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C04B04` owner confirmation.
- Implement and test isolated guarded browser registration according to the accepted safety contract; honor WordPress policies and filters, enforce identity ownership, handle partial account creation truthfully, preserve legacy mobile behavior and never expose refresh credentials. No public route, live hook or mutation to preserved sites.
- Test real WordPress and MariaDB concurrency, collisions, invalid requests, failures and redaction; record evidence under `docs/evidence/P00-PWA-AUTH-WP-021C04B05/`. Actual network and browser QA remain separate tasks.

## Completion — 2026-10-20

Implemented an inactive guarded browser-registration service. It checks the
browser request policy before calling the supported writer, emits no refresh
credential in its body, and returns a recovery-required result if account
creation commits but session issuance fails. A real disposable WordPress/MariaDB
fixture passed origin rejection, successful redacted handoff, duplicate identity,
partial session failure and invalid-input checks. The pre-existing supported-writer
matrix supplies the concurrency and callback-failure coverage; see
[verification](../evidence/P00-PWA-AUTH-WP-021C04B05/verification.md). No route,
hook, cookie emission or preserved-site mutation was enabled.
