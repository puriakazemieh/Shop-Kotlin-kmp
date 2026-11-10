# P02-CI-CODE-010C3B2B — managed deletion DB triage

Date: 2026-11-10. `node docs/evidence/P02-CI-CODE-010C3B2B/build-ledger.cjs` verified pinned shipped Plugin Check report SHA-256 `50f0c694447c4b9aab40b1d66c4091cecdad0614ba18c8f70208c3f2b36fd0bd` and wrote `ledger.json`: 17 exact unique DB findings, all matched to WPCS by path, line and code.

The admin action checks capability, nonce, managed-account identity and installed user-integrity state before its transaction. Fixed START/COMMIT/ROLLBACK statements carry no request values. User/dependency rows are read live with prepared integer IDs; the row lock and pre-delete dependency checks must not use cache. `wpdb->delete` uses internal table names and explicit integer formats. SQL failures and nonzero post-delete verification return unavailable, never success. Each finding has a line-specific source reason and existing real-WordPress test path in the ledger. All 17 are context-justified for this source-review scope; no scanner suppression or product edit was made.

The ledger rebuild printed `findings=17 unique=17 wpcsMatched=17` and exited 0. The cited real-WordPress deletion tests were not rerun in this task. This is not a Plugin Check or Phase 02 gate PASS.
