# P02-SEC-CODE-010C2B4B — Validate scalar meta-box input shape

- Status: TODO
- Phase/Area/Type: P02 / Security / CODE
- Priority/Risk/Size: P1 / MEDIUM / S
- Owner: AI
- Depends on: P02-SEC-CODE-010C2B4A
- Blocks: P02-PERF-REVIEW-010C2B4C

## Scope and acceptance

Characterize malformed array input for scalar meta-box fields, then reject/ignore non-scalar values before `wp_unslash` and type-specific sanitization. Preserve nonce, capability and valid scalar save behavior. Test actual `save_post` callback with synthetic posts and cleanup; inspect the pinned WPCS line after change. Allowed: `wordpress/carmilla-bridge/includes/meta-boxes.php`, focused tests, `docs/**`. Evidence: `docs/evidence/P02-SEC-CODE-010C2B4B/`.

## Completion record

- Final status: TODO
