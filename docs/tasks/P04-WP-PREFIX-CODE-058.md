# P04-WP-PREFIX-CODE-058 — Migrate short Bridge global prefix

- Status: TODO
- Phase/Area/Type: P04 / WordPress / CODE
- Priority/Risk/Size: P2 / MEDIUM / L (must decompose before coding)
- Owner: AI
- Depends on: P04-WP-ADMIN-CODE-054
- Blocks: P04-CI-CODE-019

## Scope and acceptance

Resolve the short `cb_` public function/hook and other Bridge global naming findings from P02 C3B3A1/A2 without breaking existing theme/plugin integration, REST behavior, saved hook consumers or third-party code. First inventory callers and hook subscribers, choose a stable product-specific prefix and compatibility plan, then decompose into ordered S/M code and verification cards before editing. Verify both standalone ZIPs and combined activation in a representative WordPress environment; retain exact historical Plugin Check fingerprints only until migration is complete. Evidence: `docs/evidence/P04-WP-PREFIX-CODE-058/`. Do not perform a blind rename.

## Completion record

- Final status: TODO; ownership transferred from P02 naming triage.
