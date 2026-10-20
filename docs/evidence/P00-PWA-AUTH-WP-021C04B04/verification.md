# Reversible synthetic identity migration verification

Date: 2026-10-20

Environment: a newly created disposable Docker WordPress 7.1.3 / PHP 8.3.35 /
MariaDB 10.6 installation. The Bridge plugin was activated only in that fixture.

`smoke-identity-synthetic-migration-real-wp.php` passed six checks:

1. a bounded exact-clean audit produces a migration plan;
2. the run journal and identity markers commit together;
3. only `cb_mobile` owners receive attributable markers;
4. an untouched run removes only its own markers;
5. a cross-owner collision fails closed with no marker write; and
6. an invalid run identifier is refused.

The primitive is inactive. It has no route, scheduled hook, normalization,
collision repair, unique index or production-site execution. Its owner QA remains
required before guarded browser registration may start.
