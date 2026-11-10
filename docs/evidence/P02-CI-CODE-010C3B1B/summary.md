# P02-CI-CODE-010C3B1B — shipped nonce triage

Date: 2026-11-10. The rebuild command `node docs/evidence/P02-CI-CODE-010C3B1B/build-ledger.cjs` verified uncompressed Plugin Check report SHA-256 `50f0c694447c4b9aab40b1d66c4091cecdad0614ba18c8f70208c3f2b36fd0bd` and produced `ledger.json`: 13 findings at 13 columns/locations, all reconciled with prior WPCS by path, line, code and occurrence. The ledger records exact scanner locations/messages, request boundary, source trace, risk, disposition and verification path per finding.

The nine `Recommended` findings read GET parameters for admin tab or fixed notice selection after a capability check. They do not perform the POST settings or deletion action; those actions have separate `check_admin_referer` gates. The four `Missing` findings inspect multipart files inside REST POST callbacks with `require_admin` or `require_login` permission callbacks. WordPress REST cookie authentication requires its REST nonce; signed Bearer credentials use an independent verification path. The nonce warning is therefore context-justified for these REST callbacks, not waived merely for being admin or GET. The source traces show the boundary for each location. No scanner suppression or product edit was made.

Checks executed:

```text
node docs/evidence/P02-CI-CODE-010C3B1B/build-ledger.cjs
findings=13 locations=13 wpcsMatched=13

C:\xampp\php\php.exe wordpress/carmilla-bridge/tests/smoke-manifest-security.php
ALL PASSED; exit 0
```

Existing real-WordPress paths in the ledger were inspected but not rerun here; the task's acceptance is source-backed triage. This is not a claim that Plugin Check, upload HTTP QA, or the phase gate passes.
