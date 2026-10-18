# P00-QA-COMPAT-026 — isolated install of freshly built WordPress packages

- Status: DONE; Size: M; Owner: AI; Risk: MEDIUM
- Depends on: `P00-QA-COMPAT-024`, `P00-QA-COMPAT-025`; the owner explicitly authorized independent Phase 00 work even when the main chain is not ready.
- Build the current Theme and Bridge ZIPs with the repository's scripts on a new isolated source copy, never on the user's checkout or retained sites. Preserve the resulting test artifacts outside the repository. Record hashes and inspect both ZIPs for the bundled shared core.
- Install both ZIPs through WordPress tooling in a fresh disposable synthetic installation with WooCommerce 8.5.2; exercise the existing bounded product/cart and in-process REST checks and record exact versions, commands and outcomes.
- No real customer data, external payment, release publication, retained-site modifications, or assertion of complete compatibility. Fail closed on setup or missing success markers. Results and limitations go under `docs/evidence/P00-QA-COMPAT-026/`.
- This is an isolated automated package-install probe. Actual browser/upgrade/standalone-product testing and user approval remain separate; do not close `P00-T07` or a human gate based on this check.

## Completion — 2026-10-18

- Both ZIPs were built on a separate source copy and their 22 shared-core files matched the source by path and hash. WP-CLI installed and activated the ZIPs in an isolated synthetic site; seven checks passed on WordPress 6.4.3/PHP 8.1.27/WooCommerce 8.5.2.
- Exact hashes, commands, version details, safety and limitations: [verification.md](../evidence/P00-QA-COMPAT-026/verification.md). Browser, upgrade, separate product modes and human approval remain open; no retained site was changed.
