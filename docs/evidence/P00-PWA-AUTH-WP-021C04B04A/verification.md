# Inactive exact-byte identity audit — synthetic evidence

2026-10-19. This is a bounded read-only research component, not an approved production identity policy or migration. It is not registered, bootstrapped, deployed or invoked on either retained WordPress installation. No real identities or credentials were written to evidence.

One statement reads user login, nonempty email and `cb_mobile` references, compares supported synthetic ASCII values by exact bytes, and returns aggregate counts only. Same-owner aliases are distinguished from cross-owner conflicts; `billing_phone` is excluded. It reports `INCOMPLETE` if more than the configured 1–10000 rows are found, `UNSUPPORTED` for invalid/unsupported values or orphan metadata, and `UNAVAILABLE` on query failure. `EXACT_MATCH_ONLY` is not an approval or a claim of global safety. Identifiers are processed transiently in the WordPress/PHP database result memory; the result does not contain them. This work does not authorize running the audit against real site data.

Three real isolated WordPress/MariaDB combinations each passed 10 checks: WordPress 6.4.3/PHP 8.1.27, 6.6.2/PHP 8.2.25 and 7.1.3/PHP 8.3.35 with MariaDB 10.6.28. Cases cover same-owner duplicate aliases, exclusion of billing contacts, byte-exact case distinction, cross-namespace and login-versus-mobile conflicts, complete versus truncated boundaries, source row invariance, non-ASCII and orphan records, bad limits and a missing-table query failure. Both files passed PHP 7.4 syntax checks; the fixture refused to bootstrap without its synthetic-only guard and random prefix. No real outage, persistent object-cache mode, multisite, custom database driver or large production dataset was tested.

```powershell
$taskWork = (Resolve-Path 'wordpress/carmilla-bridge').Path.Replace('\','/') + ':/work:ro'
foreach ($taskImage in @('wordpress:6.4-php8.1-apache','wordpress:6.6-php8.2-apache','wordpress:php8.3-apache')) {
    $taskPrefix = 'qa_' + [guid]::NewGuid().ToString('N').Substring(0,12) + '_'
    $taskOutput = docker run --rm --network container:carmilla-p00-identity-composite-20261018 --volume $taskWork --env CARMILLA_LOGIN_DISPOSABLE=yes --env "CARMILLA_LOGIN_TEST_PREFIX=$taskPrefix" $taskImage php /work/tests/smoke-identity-exact-audit-real-wp.php
    $taskExit = $LASTEXITCODE
    $taskOutput
    if ($taskExit -ne 0 -or -not ($taskOutput -match '^PASS inactive identity exact audit WP ')) { throw 'Audit test incomplete.' }
}
```

Each run creates randomly prefixed synthetic WordPress tables in the separate retained test fixture; it does not reset them or modify the user's sites. Docker image tags may change. Running the command without the fixture and guard is unsupported.

## Boundaries

The tested exact ASCII comparison is intentionally not normalization: case-equivalent addresses, Unicode and Persian/Arabic digits, telephone prefixes, real WordPress collation and lookup semantics remain unresolved. A single statement is not proof of a stable snapshot for nontransactional storage, nor does it guard later writes or detect writers changing immediately after the read. The query's sort can be expensive even though its returned rows are bounded. Query filters, driver overrides and other privileged PHP/SQL actors can invalidate assumptions. Counts and identifiers can be personal information; authorization, privacy, retention and real-site operation are not implemented. No collision resolution, migration, identity trigger, account merge, recovery or registration was added. Parent `021C04B04` and the Phase 00 gate remain incomplete.
