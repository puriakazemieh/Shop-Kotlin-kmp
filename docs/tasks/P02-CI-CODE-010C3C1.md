# P02-CI-CODE-010C3C1 — Exact Plugin Check baseline checker

- Status: DONE
- Phase/Area/Type: P02 / CI / CODE
- Priority/Risk/Size: P1 / HIGH / M
- Owner: AI
- Depends on: P02-CI-CODE-010C3B3, P02-SEC-CODE-010C3B3C1, P02-SEC-CODE-010C3B3D1
- Blocks: P02-CI-CODE-010C3C2

## Scope and acceptance

Build a version-pinned exact fingerprint baseline from C3B1/B2/B3 source-backed ledgers and a checker for the delivery-shaped Plugin Check report. Require path/line/column/code/message/severity match; reject unknown/new findings, malformed/duplicate/absent ledgers or report, scanner/action version drift and unowned dispositions. Allow previously approved findings to disappear. Keep deferred owner/task references explicit. Test positive pinned input, removals, synthetic new security/DB/other findings, tampering and missing input. No product source fix or workflow change. Evidence: `docs/evidence/P02-CI-CODE-010C3C1/`.

## Completion record

- Final status: DONE on 2026-11-10 for the local exact checker. All 315 pinned findings and 19 ledger hashes are verified, four P02 fix fingerprints must disappear, and 15 positive/negative scenarios passed. Evidence: `docs/evidence/P02-CI-CODE-010C3C1/`. Fresh CI and wallet checkout QA remain separate gates.
