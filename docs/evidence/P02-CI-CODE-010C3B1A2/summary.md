# P02-CI-CODE-010C3B1A2 — remaining shipped input triage

Date: 2026-11-10. `node docs/evidence/P02-CI-CODE-010C3B1A2/build-ledger.cjs` verified the pinned Plugin Check report hash `50f0c694447c4b9aab40b1d66c4091cecdad0614ba18c8f70208c3f2b36fd0bd` and produced `ledger.json`: nine findings at six locations. Seven map to WPCS entries by source path, line and code. The two `HealthChecker.php` findings have no WPCS entry because the earlier WPCS scan root excluded bundled core; they are not treated as covered by WPCS.

Each instance has a source trace, risk, test path and context-specific disposition in the ledger. The manifest stores booleans for known keys after admin capability and nonce checks. The CLI script compares a realpath only. Meta-box input is shape checked, unslashed and sanitized by type before persistence. Local QA OTP lookup is restricted to localhost and admin, nonce checked, then uses a hash key; output is escaped. Managed deletion compares an unslashed exact confirmation after admin capability and nonce checks. `SERVER_SOFTWARE` is server environment metadata in a diagnostic array; repository source search found no production output consumer. These are reasoned source dispositions, not scanner suppressions.

Executed checks:

```text
node docs/evidence/P02-CI-CODE-010C3B1A2/build-ledger.cjs
findings=9 locations=6 wpcsMatched=7 bundledCoreUnmatched=2

C:\xampp\php\php.exe wordpress/carmilla-bridge/tests/smoke-manifest-security.php
ALL PASSED; exit 0

C:\xampp\php\php.exe wordpress/packages/carmilla-core/tests/smoke-carmilla-core.php
ALL SMOKE TESTS PASSED (0 failures); exit 0
```

Other cited real-WordPress test paths were inspected but not rerun in this review. No product code was edited and no overall security or Plugin Check gate is asserted to pass.
