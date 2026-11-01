# P02-SEC-CODE-010C2B2B — Verify deletion observer DB topology

- Status: TODO
- Phase/Area/Type: P02 / Security / CODE
- Priority/Risk/Size: P1 / HIGH / M
- Owner: AI
- Depends on: P02-CI-CODE-010C2B2
- Blocks: P02-CI-CODE-010C2C

## Scope and acceptance

The native-deletion incident observer opens a second mysqli connection without proving it sees the same primary and schema as WordPress. Constrain to an explicitly supported single-site topology and verify equivalence, or fail closed without a false observer-success claim. Test standard topology and synthetic unsupported drop-in/multisite/custom table or mismatched DB cases. Keep observer read-only with respect to native deletion. If topology requires an owner decision or exceeds M, stop and decompose. Allowed: `wordpress/carmilla-bridge/includes/class-cb-deletion-incident-runtime.php`, focused tests, `docs/**`. Database integration QA and owner confirmation required. Evidence: `docs/evidence/P02-SEC-CODE-010C2B2B/`.

## Completion record

- Final status: TODO
