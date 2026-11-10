# P02-CI-CODE-010C3B2G2 — academy/blog/clinic DB triage

Date: 2026-11-10. `node docs/evidence/P02-CI-CODE-010C3B2G2/build-ledger.cjs` reproduced six exact findings from the pinned shipped-file Plugin Check report (uncompressed SHA-256 `50f0c694447c4b9aab40b1d66c4091cecdad0614ba18c8f70208c3f2b36fd0bd`). Each path+line+code has one prior WPCS match and an existing owner card. `ledger.json` retains full identity, message, source risk, and target.

Two lesson-question commentmeta filters belong to `P13-LMS-PERF-CODE-029`, which owns bounded pagination and the 30,000-comment SQL scenario. Two featured-blog postmeta filters belong to `P04-WP-CONTENT-PERF-CODE-057`, which owns measured query bounds. Two admin patient-message commentmeta filters belong to `P14-CLINIC-PERF-CODE-034D`, which owns pagination, isolation, and 30,000-message traversal. All six remain performance work in those phases; this review does not pass their acceptance gates or change product code.
