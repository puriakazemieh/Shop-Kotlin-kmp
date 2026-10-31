# P02-QA-CODE-007C5 checkpoint — 2026-10-31

Baseline `e2b1815c`. Three new PHP 8.1/WP 6.4.3/Woo 8.5.2 CI cells each run one guarded real-WP smoke. Token uses `carmilla_token_qa` plus only `CARMILLA_TOKEN_DISPOSABLE=yes` and a random token prefix. Migration and adapter use separate job services of `carmilla_adapter_qa`, with only `CARMILLA_ADAPTER_DISPOSABLE=yes` and a fresh random adapter prefix in each. The fixture-only database user is `synthetic_qa`; WP root is `/usr/src/wordpress`, Bridge is `/work`, and each job is disposed by GitHub Actions. No customer site or retained DB is referenced.

Docker PHP 8.1 without network: all three tests exited 2 without their respective disposable opt-in; lint on each exited 0. GitNexus cannot resolve the YAML workflow (`risk: UNKNOWN`), so the explicit test dispatch and per-test environment assignments were text-reviewed. Actual CI is pending; no token, migration or adapter PASS is claimed yet.
