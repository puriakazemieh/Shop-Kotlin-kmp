# P02-QA-CODE-007C5 checkpoint — 2026-10-31

Baseline `e2b1815c`. Three new PHP 8.1/WP 6.4.3/Woo 8.5.2 CI cells each run one guarded real-WP smoke. Token uses `carmilla_token_qa` plus only `CARMILLA_TOKEN_DISPOSABLE=yes` and a random token prefix. Migration and adapter use separate job services of `carmilla_adapter_qa`, with only `CARMILLA_ADAPTER_DISPOSABLE=yes` and a fresh random adapter prefix in each. The fixture-only database user is `synthetic_qa`; WP root is `/usr/src/wordpress`, Bridge is `/work`, and each job is disposed by GitHub Actions. No customer site or retained DB is referenced.

Docker PHP 8.1 without network: all three tests exited 2 without their respective disposable opt-in; lint on each exited 0. GitNexus cannot resolve the YAML workflow (`risk: UNKNOWN`), so the explicit test dispatch and per-test environment assignments were text-reviewed. Actual CI is pending; no token, migration or adapter PASS is claimed yet.

## Actual CI result

Local workflow commit `33e855d7` was mirrored through the GitHub connector on clean branch `codex/p02-c5-ci`, remote commit `583c501d`, in [draft PR #30](https://github.com/puriakazemieh/Shop-Kotlin-kmp/pull/30). The direct Git push credential was unavailable. GitNexus in the remote worktree matched `583c501d` after indexing.

[WordPress Integration run 41198628730](https://github.com/puriakazemieh/Shop-Kotlin-kmp/actions/runs/41198628730) passed all 23 jobs, including the three new token, session migration and WP adapter jobs. The migration job `124529793888` logged `PASS repeat preserves existing session` and `PASS existing synthetic WordPress account and post preserved`. Each job used its own disposable MariaDB service and prefix; GitHub Actions cleanup stopped the service. The negative-gate configuration remains from 007B. This CI-only change did not modify product network or migration code; no human behavior acceptance is required for this child. The separate lint workflow is not inferred green.
