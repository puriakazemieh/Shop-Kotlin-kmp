# Guarded browser registration verification

Date: 2026-10-20

Environment: disposable Docker WordPress 7.1.3, PHP 8.3.35 and MariaDB 10.6.

`smoke-web-registration-real-wp.php` passed five checks:

1. an untrusted origin is rejected before any account write;
2. a clean identity creates a supported account and exposes only an opaque,
   one-time cookie handoff, never a refresh credential in the body;
3. a duplicate mobile returns a conflict without session/cookie issuance;
4. session issuance failure preserves the committed account, marks recovery and
   exposes no credential; and
5. malformed input is rejected before the writer runs.

This component is intentionally unregistered. Existing writer evidence covers the
normal WordPress callback, post-callback conflict, collision and failure behavior.
Network and browser activation remain separate owner-controlled work.
