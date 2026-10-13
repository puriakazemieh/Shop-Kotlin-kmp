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

## Remaining human confirmation

Open the WordPress dashboard after the PWA test and confirm that only date/tenant/client/event/count are displayed and that the counter does not change after opt-out. This is intentionally still an `AWAITING_MANUAL_QA` task.
