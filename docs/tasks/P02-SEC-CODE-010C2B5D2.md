# P02-SEC-CODE-010C2B5D2 — Harden root alias request dispatch

- Status: TODO
- Phase/Area/Type: P02 / Security / CODE
- Priority/Risk/Size: P1 / HIGH / M
- Owner: AI
- Depends on: P02-SEC-CODE-010C2B5D1
- Blocks: P02-SEC-CODE-010C2B5D3

## Scope and acceptance

Use D1 findings to preserve native REST authentication, permission and validation semantics in `CB_Plugin::maybe_root_alias`. Reject/normalize malformed query and header values without unsafe raw forwarding; do not force JSON over form requests; avoid private path data in telemetry. Test root/canonical HTTP parity for normal and malformed requests, including no-nonce cookie and bearer cases. No external account/data. Allowed: plugin alias dispatch, focused tests, `docs/**`. Evidence: `docs/evidence/P02-SEC-CODE-010C2B5D2/` with before/after cases and cleanup.

## Completion record

- Final status: TODO
