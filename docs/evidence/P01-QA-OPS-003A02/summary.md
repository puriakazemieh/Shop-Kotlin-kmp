# P01-QA-OPS-003A02 — Evidence

Date: 2026-10-28

## Local-only fixture

The `web-session-qa` Compose profile runs WordPress in the MariaDB network
namespace and exposes it only through a local proxy at `http://localhost:8092`.
The profile mounts a local MU-plugin that derives its session key from runtime
WordPress salts and accepts only the fixed local proxy peer and the exact PWA
origin `http://localhost:8081`.

## Verification

```text
docker compose -f tools/test-env/docker-compose.yml --profile web-session-qa exec -T \
  -e CARMILLA_LOCAL_WEB_SESSION_MIGRATE=yes wordpress-web-session-fixture \
  php /opt/carmilla-test-tools/initialize-web-session.php
```

Result: exit code 0; `Local browser-session migration: CURRENT`.

REST route discovery after fixture restart confirmed all of these routes:

- `/carmilla/v1/api/auth/web/login`
- `/carmilla/v1/api/auth/web/register`
- `/carmilla/v1/api/auth/web/refresh`
- `/carmilla/v1/api/auth/web/logout`

A synthetic browser-session login was sent to the local fixture with the
required browser request header. Result: HTTP 200, exact
`Access-Control-Allow-Origin: http://localhost:8081`,
`Access-Control-Allow-Credentials: true`, and `Cache-Control: no-store`.
No identifier, password, request body, cookie, token, or response body is
retained here.

The browser initially rejected the request during CORS preflight because the
generic allowlist response omitted `X-Carmilla-Web-Request`. The bridge now
includes that required non-simple header for already-allowlisted origins. A
retested preflight returned HTTP 204 with that header, and the subsequent
browser-session login returned HTTP 200.

## Manual QA URL

`http://localhost:8081/?wpApi=http%3A%2F%2Flocalhost%3A8092%2Fwp-json%2Fcarmilla%2Fv1%2F#/login`

## Rollback

Stop the local-only profile services with:

```text
docker compose -f tools/test-env/docker-compose.yml --profile web-session-qa stop wordpress-web-session-fixture wordpress-web-session-proxy
```
