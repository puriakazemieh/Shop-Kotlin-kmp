# P07-SEED-SEC-CODE-030 — Validate seed media URL scheme and destination

- Status: TODO
- Phase/Area/Type: P07 / Seed Import / Security CODE
- Priority/Risk/Size: P1 / HIGH / S
- Owner: AI
- Depends on: P07-SEED-CODE-002
- Blocks: P07-SEED-GATE-026

## Scope and acceptance

Review the bundled-core importer media URL path from manifest input through `media_sideload_image()`. It currently parses the URL and checks host against an allowlist without an explicit HTTPS scheme check. Require a safe scheme and destination policy that remains valid across redirects and DNS resolution, including a synthetic attacker-controlled URL/redirect fixture. Preserve legitimate allowed-domain imports and fail closed. Evidence: `docs/evidence/P07-SEED-SEC-CODE-030/`.

## Completion record

- Final status: TODO; owner from P02 C3B3C source review. No SSRF safety is inferred from the existing host-only check.
