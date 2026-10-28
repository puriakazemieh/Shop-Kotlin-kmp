# P01-QA-OPS-003A01 — Evidence

Date: 2026-10-28

## Scope

The `web-session-qa` Compose profile starts a one-shot WordPress probe in the
MariaDB service's network namespace. Its WordPress primary connection and the
adapter's independent connection both use `127.0.0.1:3306`. The service is
removed after the command completes.

## Verification

```text
docker compose -f tools/test-env/docker-compose.yml --profile web-session-qa run --rm --no-deps wordpress-session-loopback-probe
```

Result: exit code 0.

```text
PASS: WordPress session adapter verified a loopback database fixture.
```

## Security boundaries

- No endpoint was registered by the probe.
- No browser-session migration ran.
- No fixed secret, token, cookie, password, or customer data was recorded.
- The existing adapter's loopback-only validation was left unchanged.

## Rollback

The one-shot probe is automatically removed. To stop the prerequisite local
database fixture, run `docker compose -f tools/test-env/docker-compose.yml stop db`.
