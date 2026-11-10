# P02-CI-CODE-010C3B1D — shipped direct-DB security triage

Date: 2026-11-10. `node docs/evidence/P02-CI-CODE-010C3B1D/build-ledger.cjs` verified the pinned shipped Plugin Check report SHA-256 `50f0c694447c4b9aab40b1d66c4091cecdad0614ba18c8f70208c3f2b36fd0bd` and wrote `ledger.json`: 11 instances, each with exact location/message, variable origin, guard, SQL injection risk, disposition, source trace, test path and nearby WPCS PreparedSQL entry. All 11 have nearby WPCS entries, but the scanners' DirectDB and PreparedSQL rules are different and were not equated by ID.

Nine findings use internal WordPress table identifiers, validated ASCII identifiers, hash-generated constraint names, or bound `%d`/`%s` values. `CB_User_Integrity::inspect` validates table names before the DDL installer is allowed to run. Two export queries assemble a fixed, `esc_sql`-escaped denied-post-type list; request data is absent, so immediate SQL injection risk is low. Their placeholder conversion remains explicitly assigned to existing `P07-MIGRATION-SEC-CODE-028`, matching the WPCS deferred security work. This review did not perform that future migration change or assert it passes. Booking transaction and migration runtime tests cited per ledger row were not rerun here.

Executed checks:

```text
node docs/evidence/P02-CI-CODE-010C3B1D/build-ledger.cjs
findings=11 deferredToP07=2 wpcsNearby=11

C:\xampp\php\php.exe wordpress/carmilla-bridge/tests/smoke-migration-export-no-plaintext.php
PASS encrypted export and decrypt
PASS write failure cleans encrypted temp without plaintext
PASS legacy plaintext requires operator cleanup
exit 0
```

No product SQL or scanner rule was changed. The Plugin Check gate remains failing until its later disposition and remediation tasks are complete.
