# P02-SEC-CODE-010C2B2A1 — Fail closed on managed deletion reads

- Status: READY
- Phase/Area/Type: P02 / Security / CODE
- Priority/Risk/Size: P1 / HIGH / S
- Owner: AI
- Depends on: P02-CI-CODE-010C2B2
- Blocks: P02-SEC-CODE-010C2B2A2

## Scope and acceptance

`CB_Managed_User_Deletion` currently casts failed dependency/count reads to zero. Distinguish a valid zero from SQL error/null and return `unavailable` without deleting or reporting success. Test both dependency read failures and post-delete verification failure with isolated synthetic WordPress/DB faults; retain transaction rollback and cache behavior. Do not redesign concurrent deletion in this card. Allowed: `wordpress/carmilla-bridge/includes/class-cb-managed-user-deletion.php`, focused tests, `docs/**`. For the changed deletion flow, record exact installed synthetic QA and retain `AWAITING_MANUAL_QA` until owner confirmation. Evidence: `docs/evidence/P02-SEC-CODE-010C2B2A1/`.

## Completion record

- Final status: READY
