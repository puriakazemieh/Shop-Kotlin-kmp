# P00-QA-COMPAT-024 — isolated WooCommerce 8.5 reference probe

- Status: DONE; Size: M; Owner: AI; Risk: MEDIUM
- Depends on: `P00-QA-MANUAL-018`; independently run a narrow software-reference check while the PWA identity chain remains blocked. The owner authorized advancing work that had not been READY.
- On a newly created disposable WordPress 6.4/PHP 8.1/MariaDB synthetic fixture only, obtain the official WooCommerce 8.5.x release, record exact version, source and SHA-256, and test activation with the existing Carmilla Theme and Bridge. Exercise a synthetic product/cart and Woo Store API when feasible. Record exact observed results, failures and configuration; never infer compatibility from install alone.
- Preserve all retained QA WordPress installations, their databases, images and volumes. Do not modify local QA installations, product support declarations or release artifacts. Do not perform payment, send messages, use real data or expose a disposable database publicly. Stop and document environmental failures without inventing a pass.
- Record reproduction, hashes, exact versions and limitations under `docs/evidence/P00-QA-COMPAT-024/`. This probe neither defines the commercial minimum nor resolves the WP/PHP floor discrepancy, upgrade/rollback, owner UI review or release gate. No user manual QA is required for the disposable, non-deployed probe; `P00-T07` remains open until its full criteria and relevant owner decisions are met.

## Completion — 2026-10-18

- Fresh disposable WordPress 6.4.3/PHP 8.1.27/MariaDB 10.6.28, official WooCommerce 8.5.2 and current Theme/Bridge source with shared core: activation and seven synthetic product/cart/REST checks passed. Exact source hash, commands and limitations: [verification.md](../evidence/P00-QA-COMPAT-024/verification.md).
- The retained QA environments and data were not changed. This is not a ZIP artifact test, checkout/payment, full compatibility matrix or human UI review; P00-T07 stays open.
