# P00-API-ERROR-CONTRACT-028 — verification

2026-10-25. The PWA error decoder now preserves both numeric WordPress and
transitional string status values. Error-code mapping never uses the visible
"unknown error" text: an unrecognised or malformed server envelope reports a
safe invalid-response message, while transport failures report a safe temporary
service message. Server-provided messages, exception details, credentials and
identifiers are not rendered by this mapping.

## Automated evidence

```text
./gradlew.bat --no-daemon :core:common:jvmTest :core:network:jvmTest \
  --tests com.kazemieh.network.common.ApiErrorContractJvmTest --offline
BUILD SUCCESSFUL
UserMessageTest: 2 tests, 0 failures, 0 errors
ApiErrorContractJvmTest: 2 tests, 0 failures, 0 errors
```

The WordPress disposable fixture ran:

```text
PASS weak password has a distinct safe error code
PASS invalid email has a distinct safe error code
PASS synthetic account can be created for duplicate check
PASS duplicate email has a distinct safe error code
Checks: 4; failures: 0.
```

The test creates one random synthetic account only to exercise the duplicate
case and deletes it before exit. It prints no identifier or credential.

## Fixture preparation

- `carmilla-p00-session-migration-fixture-20261025`: isolated MariaDB, no host
  port or bind mount. Its migration smoke test passed all 16 checks.
- `carmilla-p00-browser-session-fixture-20261025`: separate isolated MariaDB,
  no host port or bind mount. Browser-session PHP primitives passed 21 login
  checks and 17 refresh/logout checks.

These fixtures do not activate public browser-session routes and do not change
the retained WordPress site.

## Owner QA

Use only the PWA QA build connected to the disposable WordPress fixture. Check
one wrong password, duplicate email, short password, wrong OTP and unavailable
OTP. Each attempt must stop loading and show the matching Persian explanation;
the UI must not show server details or the generic unknown-error text.

### Partial confirmation — 2026-10-25

The owner confirmed that the freshly deployed local PWA rendered the expected
WordPress-backed content. Automated verification on the same deployment also
confirmed the `BAD_CREDENTIALS` response contract. The four remaining manual
error scenarios above are still required before this task can be marked DONE.
