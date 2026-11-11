# P02-CI-CODE-010C3C4D — Reconcile exact current Plugin Check fingerprints

- Status: TODO
- Phase/Area/Type: P02 / CI / CODE+QA
- Priority/Risk/Size: P1 / HIGH / M
- Owner: AI
- Depends on: P02-SEC-CODE-010C3C4A, P02-CI-CODE-010C3C4B, P02-SEC-CODE-010C3C4C
- Blocks: P02-CI-CODE-010C3C

## Scope and acceptance

Rescan the actual delivery-shaped Bridge ZIP using the pinned scanner and WordPress versions. Reconcile every current finding against the source-backed reviewed ledger, including shifted positions from earlier security fixes and any justified read-only GET warning. Update the exact checker/baseline only for individually reviewed findings; preserve rejection of every new security, DB and other finding, missing report, version drift and malformed ledger. Prove an approved current report passes and a synthetic new finding fails locally; record report hash, diff, commands and evidence in `docs/evidence/P02-CI-CODE-010C3C4D/`. No blanket waiver or product source fix in this card.

## Completion record

- Final status: DONE. The retained delivery-ZIP report was reconciled against all historical findings; three exact read-only tab warnings received individual source-backed ledger entries. Current full report passed, a synthetic new finding failed, and 19 checker scenarios passed. Evidence: `docs/evidence/P02-CI-CODE-010C3C4D/`. The parent gate remains BLOCKED by distinct WooCommerce checkout QA.
