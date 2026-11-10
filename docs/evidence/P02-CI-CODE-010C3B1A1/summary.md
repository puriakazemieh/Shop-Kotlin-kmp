# P02-CI-CODE-010C3B1A1 — shipped auth/plugin input triage

Date: 2026-11-10. Input: shipped Bridge ZIP Plugin Check report from CI run `42751751194`, uncompressed SHA-256 `50f0c694447c4b9aab40b1d66c4091cecdad0614ba18c8f70208c3f2b36fd0bd`. `node docs/evidence/P02-CI-CODE-010C3B1A1/build-ledger.cjs` checked the hash and generated `ledger.json`: 18 findings, nine distinct path/line/column locations, 18 corresponding WPCS entries. WPCS columns differ, so matching uses path, line, code, and occurrence order; the two scanners are not asserted equivalent.

All 18 instances have a source-specific `context_justified` disposition in the ledger. The controller's three server values and Host header are used by `CB_Web_Request_Policy::transport_ok` and its header parser: string and IP checks, control-character rejection, canonical site-origin equality, and exact transport decisions. The cookie is a 64-character hex credential checked by `CB_Web_Session_Store::digest` before rotation or revocation. The plugin compares method with a literal, narrows root aliases to four fixed prefixes before WordPress REST dispatch, and passes Bearer credentials to signed-token decoders. These values are not free-form display or SQL values; changing their bytes by generic text sanitization could break authentication. Source locations and the existing focused test paths are recorded per finding in the ledger. No scanner finding was suppressed or product code changed.

Verification performed here:

```text
node docs/evidence/P02-CI-CODE-010C3B1A1/build-ledger.cjs
findings=18 locations=9 wpcsMatched=18

C:\xampp\php\php.exe wordpress/carmilla-bridge/tests/smoke-web-request-policy.php
Checks: 97; failures: 0. Exit: 0
```

The 97 checks exercise the request policy in isolation. The root alias and refresh tests cited in the ledger are existing coverage paths, not rerun as installed HTTP or database checks in this review. The previously recorded WPCS dispositions were independently inspected against current source; they were not treated as a scanner waiver. The remaining security findings belong to the ordered follow-up triage cards. No claim is made that the overall Plugin Check gate passes.
