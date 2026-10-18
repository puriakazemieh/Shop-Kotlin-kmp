# P00-QA-COMPAT-025 — investigate declared WordPress/PHP minimum

- Status: DONE; Size: M; Owner: AI; Risk: MEDIUM
- Depends on: `P00-QA-COMPAT-024`; run independently on an isolated synthetic environment under the owner's authorization to continue.
- Investigate the headers declaring WordPress 6.3/PHP 7.4, distinct from the higher reference test environment. Obtain verifiable official inputs, attempt syntax and real activation/product/cart/API checks on an isolated combination if possible, and record exact versions, source hashes, commands and failures. This task must not update product support promises, production code or retained sites.
- Use only disposable containers/databases, synthetic data and read-only source mounts. Preserve existing WordPress sites, their data, images and volumes. Do not accept incomplete setup, missing final success markers or lint as proof of runtime compatibility.
- Record results and limitations in `docs/evidence/P00-QA-COMPAT-025/`. If required inputs or setup cannot be obtained, set `BLOCKED` and record the exact blocker rather than claiming pass. Any resulting commercial minimum, fuller QA matrix or human UI approval remains outside this task; `P00-T07` stays open.

## Completion — 2026-10-18

- WordPress 6.3.5/PHP 7.4.33/MariaDB 10.6.28/WooCommerce 8.5.2 activated from official test inputs in a disposable synthetic environment. Seven product/cart/REST checks passed; PHP 7.4 lint passed on source Theme, Bridge includes and shared core. Exact inputs, hash, commands and limits: [verification.md](../evidence/P00-QA-COMPAT-025/verification.md).
- This is one patch-level combination and source-copy test, not validation of all versions, packaged ZIPs, upgrade/rollback, checkout or browser. Retained sites were not modified; P00-T07 remains open.
