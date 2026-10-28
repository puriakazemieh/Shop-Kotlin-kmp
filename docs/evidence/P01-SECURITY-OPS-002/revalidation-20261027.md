# P01-SECURITY-OPS-002 — local re-validation (2026-10-27)

## Scope

This evidence covers only the locally available Spring Boot checkout and its
Docker PostgreSQL service. It does not assert the security posture of a remote
deployment.

## Observed controls

- `docker compose config --quiet` completed successfully (re-run on 2026-10-27, exit code 0).
- `docker compose up -d --wait --wait-timeout 60 db` completed successfully;
  PostgreSQL was healthy.
- Docker and the Windows listener inspection both reported the database port
  as `127.0.0.1:5432`, not a wildcard interface.
- `docker compose exec -T db pg_isready -U postgres -d shopdb` succeeded.
- `application.properties` defaults `server.address` to loopback.
- Current local JUnit XML reports have zero failures for application,
  web-session, identity and payment tests.

## Scope decision

The product owner confirmed there is no public Spring deployment in the
current local-build scope. The independent external-surface test is deferred
to `P10-RELEASE-SEC-020` before public release.
