# P00-PWA-AUTH-WP-021C02 — isolated request policy evidence

Date: 2026-10-17. Source-only implementation; no route, hook, response header, cookie, deployment or WordPress database change.

## Scope and decisions

- `CB_Web_Request_Policy` takes trusted server configuration, derives the cookie name from the identical canonical site/blog identity as `021C01`, and requires exact configured origin, POST, JSON and custom header. It generates only decisions/options: it cannot issue a cookie or change CORS itself. Cookie options are host-only, Secure, HttpOnly, SameSite Strict and Path `/`, expiring at the supplied absolute deadline.
- This implementation deliberately accepts only same-scheme, **same-host** trusted browser origins (different explicit ports are allowed); verified sibling subdomains are not supported. It accepts exactly `application/json` and preflight requests for exactly the two specified headers. These conservative limits must be checked against the real client before integration, not silently relaxed.
- Direct transport requires server-provided HTTPS. Proxy mode requires explicit trusted direct peer IPs, an administrator assertion that the proxy strips client forwarding headers, and a single `X-Forwarded-Proto: https`; it rejects direct fallback and other forwarding metadata. The assertion is not proof of correct proxy or firewall configuration. Configure and verify those separately; the policy cannot inspect actual TLS or network isolation.
- HTTP is rejected except with explicit opt-in, environment `local`, `localhost` site/origin, no proxy metadata, matching Host and actual loopback server and client addresses. The integration must also ensure no hidden proxy/forwarding and synthetic-only accounts. Secure cookie attributes are never weakened; browser acceptance remains unverified.
- Origin/header inputs must preserve every occurrence. `021C07` must verify its real server/WordPress integration rejects ambiguous duplicate headers **before** any layer collapses them, or enforce that rejection at a trusted ingress. It must not assume that `$_SERVER` or a framework header map retains duplicates. Matching the policy's trusted site/blog configuration with the independently verified WP adapter is mandatory.
- The name's 128-bit truncated hash avoids accidental practical collisions but this isolated component cannot discover other WordPress installations or establish a global collision registry. The integration/deployment must detect duplicate or colliding configured identities in its authoritative installation namespace and fail closed; otherwise the collision requirement in `021A` is not proven. Cookie names do not isolate mutually untrusted applications on the same host.

## Reproducible automated checks

Run from repository root:

```powershell
C:\xampp\php\php.exe -l wordpress/carmilla-bridge/includes/class-cb-web-request-policy.php
C:\xampp\php\php.exe -l wordpress/carmilla-bridge/tests/smoke-web-request-policy.php
C:\xampp\php\php.exe wordpress/carmilla-bridge/tests/smoke-web-request-policy.php
C:\xampp\php\php.exe wordpress/carmilla-bridge/tests/smoke.php
C:\xampp\php\php.exe wordpress/carmilla-bridge/tests/smoke-security.php
docker run --rm --network none --mount 'type=bind,source=D:\Android\AndroidStudioProjects\kmp-shop\wordpress\carmilla-bridge,target=/bridge,readonly' php:8.1-cli php /bridge/tests/smoke-web-request-policy.php
docker run --rm --network none --mount 'type=bind,source=D:\Android\AndroidStudioProjects\kmp-shop\wordpress\carmilla-bridge,target=/bridge,readonly' wordpress:php8.3-apache php /bridge/tests/smoke-web-request-policy.php
```

- XAMPP PHP 8.2.12: policy **93/93 passed**, both new files linted, existing Bridge smoke and security smoke passed.
- Isolated containers with a read-only source mount and networking disabled: PHP 8.1 and PHP 8.3, **93/93 passed each**. These containers are removed automatically; they contain no user WordPress installation or persistent data.
- Cases include hostile/missing/null/multiple origin, Host spoofing, repeated/malformed headers, content types and methods, exact credentialed CORS and preflight decisions, production HTTP rejection, default-off local exception and loopback checks, unknown/malformed proxy metadata, cookie isolation/attributes and expiration.

No browser, actual network stack, real proxy, production TLS, duplicate-header ingress behavior or live WordPress route was tested. This is not a pentest or release evidence. Human network QA remains required in `021C07`; PWA end-to-end testing remains `P00-PWA-AUTH-QA-023`.
