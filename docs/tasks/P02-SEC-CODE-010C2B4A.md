# P02-SEC-CODE-010C2B4A — Fail closed on admin therapist slot read failure

- Status: DONE
- Phase/Area/Type: P02 / Security / CODE
- Priority/Risk/Size: P1 / HIGH / S
- Owner: AI
- Depends on: P02-CI-CODE-010C2B4
- Blocks: P02-SEC-CODE-010C2B4B

## Scope and acceptance

Characterize `CB_Admin_Content_Controller::therapist_slots` under a failed booking-lock SELECT, then return an explicit unavailable error instead of showing all slots free. Preserve the trusted table name and prepared therapist ID; do not cache live booking locks. Verify success, failure and recovery through the actual admin HTTP route in a disposable WordPress environment if the route can be enabled; otherwise record exact limitation and manual QA steps. Allowed: admin-content controller, focused tests, `docs/**`. Evidence: `docs/evidence/P02-SEC-CODE-010C2B4A/`.

## Completion record

- Final status: DONE (2026-11-03)
- Evidence: `docs/evidence/P02-SEC-CODE-010C2B4A/verification.md`; direct installed-WP and authenticated HTTP tests passed.
