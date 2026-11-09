# Isolated PR detekt/WPCS positive and negative proof — 2026-11-09

PR: https://github.com/puriakazemieh/Shop-Kotlin-kmp/pull/33, head `codex/p02-ci-phase-triage`, base `develop`. The first PR opened event did not schedule Actions; a subsequent synchronize event did. The synthetic commit's synchronize event also did not schedule the quality workflow, so the PR was closed and reopened without merge to trigger the negative run. No production branch was changed.

| Scenario | Commit / run | Required jobs | Report artifacts | Result |
| --- | --- | --- | --- | --- |
| Positive exact phase gate | `86f559c5287c925d1e138f01b825cd6453ec4b7e`, [run 42601296282](https://github.com/puriakazemieh/Shop-Kotlin-kmp/actions/runs/42601296282) | `detekt` job `129106159349`: success; `WordPress Coding Standards` job `129106159418`: success | `detekt-report` artifact `13665279050`, `wpcs-report` artifact `13666161177` | WPCS log: style current 5,395/baseline 5,425/new 0/resolved 30; Security/DB current 168/unreviewed 0. PHPCS itself exits 2 due existing findings; exact checker exits 0. |
| Synthetic new WPCS violation | `8e12bcdad9e2f6836f86524b1d76fafab4bfe807`, [run 42602066787](https://github.com/puriakazemieh/Shop-Kotlin-kmp/actions/runs/42602066787) | `detekt` job `129108712932`: success; `WordPress Coding Standards` job `129108713138`: failure at baseline check | `detekt-report` artifact `13666750802`, `wpcs-report` artifact `13666272132` | Temporary `wordpress/carmilla-bridge/ci-synthetic-wpcs.php` produced one new `Squiz.Commenting.FileComment.MissingPackageTag` at line 2:77; WPCS log: style current 5,396/new 1, checker exit 1. |

The two required jobs and their report uploads were inspected via GitHub Actions job/step and artifact APIs; the WPCS job logs above give the actual comparison result. The *whole* workflow is red in both runs because the separate `WordPress Theme Check` job fails on existing findings. Plugin/Theme Check baseline work is explicitly queued in `P02-CI-CODE-010C3`; this D card proves only the combined detekt/WPCS jobs and must not be represented as a green full quality workflow.

The synthetic file is deleted in the rollback commit after the negative proof. A final clean-head CI run and its job/artifact inspection are still required before D is DONE.
