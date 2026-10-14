# P09-TELEMETRY-QA-022 Evidence

Date: 2026-10-13

## Automated proof against local WordPress QA

| Scenario | Request | Result |
|---|---|---|
| Consented allowlisted event | `catalog_viewed`, `consent:true`, `pwa`, `local-default` | HTTP `202`, `{"accepted":true}` |
| Opt-out | same event with `consent:false` | HTTP `400` |
| Unknown event | `unapproved_event` with consent | HTTP `400` |
| Extra synthetic fields | allowlisted event plus `email` / `token` fields | HTTP `202`; controller stores only an aggregate bucket/count |

## Implementation checks

- `wordpress/carmilla-bridge/includes/class-cb-telemetry-controller.php` passed `php -l` in source and in `C:\xampp\htdocs\carmilla-p07-qa`.
- The bucket-pruning rule removes aggregate keys older than 90 UTC days before each accepted write.
- Client-side consent/persistence/opt-out was accepted manually in `P09-TELEMETRY-CODE-021`.

## Human confirmation — PASS, 2026-10-14

The tester confirmed in the local WordPress QA dashboard that only aggregate
date/tenant/client/event/count data are displayed and the counter does not
increase after opt-out.

## QA PWA launch repair — 2026-10-14

- The generated PWA now resolves bootstrap, manifest, worker and bundle assets relative
  to its installation directory, so the QA copy can run at `/carmilla-pwa-qa/`.
- The WordPress QA endpoint configuration was preserved during deployment.
- Service-worker cache names were advanced to `v5`; the app bundle URL is versioned to
  avoid execution of the previous cached bundle.
- A browser DOM check confirmed the Settings UI and its anonymous-telemetry control
  render. The dashboard/opt-out confirmation above completes the manual step.
