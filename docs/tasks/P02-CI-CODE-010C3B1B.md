# P02-CI-CODE-010C3B1B — Triage shipped nonce findings

- Status: TODO
- Phase/Area/Type: P02 / CI / SECURITY-REVIEW
- Priority/Risk/Size: P1 / HIGH / M
- Owner: AI
- Depends on: P02-CI-CODE-010C3B1A2
- Blocks: P02-CI-CODE-010C3B1C

## Scope and acceptance

Review all `WordPress.Security.NonceVerification.*` findings in the pinned shipped-file report (13 preliminary instances). For each record exact location/code/message, caller and request trust boundary, source-backed risk, specific fix or reasoned disposition, and test/fix task ID. Compare overlapping WPCS entries. Do not treat authorization or a GET method as an automatic nonce waiver. No product fix or blanket waiver. Allowed: read-only product source, `tools/code-quality/**`, `docs/**`. Evidence: `docs/evidence/P02-CI-CODE-010C3B1B/`.

## Completion record

- Final status: TODO.
