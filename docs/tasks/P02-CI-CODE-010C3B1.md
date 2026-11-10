# P02-CI-CODE-010C3B1 — Triage Plugin Check security findings

- Status: TODO (decomposed; B1A1 first READY)
- Phase/Area/Type: P02 / CI / SECURITY-REVIEW
- Priority/Risk/Size: P1 / HIGH / L (58 findings across 19 files plus WPCS reconciliation; decomposed)
- Owner: AI
- Depends on: P02-CI-CODE-010C3B1E
- Blocks: P02-CI-CODE-010C3B2

## Scope and acceptance

For every `WordPress.Security.*` and `PluginCheck.Security.*` finding in the pinned **shipped-file** Plugin Check report, record exact path/line/column/code/message, source-backed risk, context/fix/later-phase disposition, and test or task ID. Compare overlapping WPCS Security dispositions without assuming the scanners are equivalent. The preliminary raw-source report has 56 such non-test findings, including seven `PluginCheck.Security.DirectDB.UnescapedDBParameter` errors; recalculate after C3A's package audit. No blanket whitelist. Escaping, nonce, input validation, credentials and direct-access warnings remain blocking until individually justified or fixed. Allowed: read-only product source, `tools/code-quality/**`, `docs/**`. Evidence: `docs/evidence/P02-CI-CODE-010C3B1/` with exact ledger, counts, representative code traces and required fix cards. No product fix in this review.

## Completion record

- Final status: TODO after size gate. Pinned shipped-file Plugin Check report in `docs/evidence/P02-CI-CODE-010C3A1/` has 58 security findings across 19 files. Ordered A1 → A2 → B → C → D → E review cards precede this aggregate completion; no finding has been waived or approved by decomposition.
