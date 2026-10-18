# P00-QA-COMPAT-027 — disposable packaged WooCommerce browser checkout probe

- Status: AWAITING_MANUAL_QA; Size: M; Owner: BOTH; Risk: HIGH
- Depends on: `P00-QA-COMPAT-026`; the owner authorized independent Phase 00 work even when the main authentication chain is blocked.
- On a new synthetic, disposable WordPress/WooCommerce installation, install the staged Theme and Bridge ZIPs and exercise product, cart and a browser checkout using only a synthetic offline payment method. Capture redacted evidence and exact versions. Block all real mail and external payment. Never use or alter the preserved sites, their databases or existing artifacts.
- Include failure guards, test actual browser interactions and inspect the synthetic order; do not equate this with PWA login, full version matrix, real payment, release approval or complete Phase 00.
- This is UI/network QA. Record automated results under `docs/evidence/P00-QA-COMPAT-027/` and leave the card `AWAITING_MANUAL_QA` until the owner confirms the clearly described manual scope. Do not change the status of `P00-T07` or a Gate based on automation alone. If setup cannot be isolated, mark BLOCKED with evidence.

## Automated execution — 2026-10-18

The bounded real-browser cart and synthetic offline checkout passed; the order was independently verified in the disposable database. The owner has not yet confirmed the manual step. Exact scope, retained temporary URL, warnings and limitations: [verification](../evidence/P00-QA-COMPAT-027/verification.md). This does not close T07 or a Gate.
