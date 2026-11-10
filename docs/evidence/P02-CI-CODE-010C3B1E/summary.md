# P02-CI-CODE-010C3B1E — security ledger reconciliation

Date: 2026-11-10. `node docs/evidence/P02-CI-CODE-010C3B1E/check-coverage.cjs` verified the pinned shipped Plugin Check report SHA-256 `50f0c694447c4b9aab40b1d66c4091cecdad0614ba18c8f70208c3f2b36fd0bd` and compared every `WordPress.Security.*` and `PluginCheck.Security.*` report record with the five category ledgers. Machine-readable output is `coverage.json`.

Result: 58 expected, 58 actual, 58 unique keys; zero missing, extra, duplicate, message mismatches, or invalid dispositions. Category counts: input auth/plugin 18, other input 9, nonce 13, output 7, DirectDB 11. WPCS reconciliation: 45 matching path/line/code entries, 11 nearby PreparedSQL entries from a different scanner rule, and two bundled-core findings absent from the earlier WPCS scan root. The latter two were independently source-reviewed in `C3B1A2`.

Dispositions: 56 context-justified with per-finding source traces; two static migration export queries assigned to existing `P07-MIGRATION-SEC-CODE-028` for placeholder conversion. This is full **triage coverage**, not a claim that the Plugin Check scan or phase gate passes. Later C3 tasks still decide and implement any genuine scanner or code remediation.
