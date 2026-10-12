# P06 — Shared settings registration and integrations capability

Date: 2026-10-12

## Fixed

- `Carmilla_Kernel::init_core()` now initializes `MessageSettings`, so the site-owned `carmilla_message_settings` schema is registered in either Theme or Bridge host mode.
- `CapabilityRegistry` now includes `manage_carmilla_integrations` and grants it to the WordPress `administrator` role during canonical capability registration.
- The Core and messaging smoke tests no longer silently exit outside WordPress before their assertions execute.

## Automated verification

| Check | Result |
| --- | --- |
| PHP 8.2 lint for all changed PHP files | pass |
| `wordpress/packages/carmilla-core/tests/smoke-carmilla-core.php` | pass — including settings-capability assertions |
| `wordpress/carmilla-bridge/tests/smoke-messaging.php` | pass — HTTPS rejection, encrypted-key masking, template sanitization |
| Docker PHP 8.1 lint for changed files | pass |

## Real local WordPress verification

The Bridge copy in the preserved local WordPress test installation was first confirmed to match the previous repository revision, then only the two changed Shared Core files were synchronized for the test.

With WordPress loaded from `D:\Carmilla-QA\p07-wordpress\wp-load.php`, the actual `admin_init` hook registered `carmilla_message_settings`, and the installed `administrator` role had `manage_carmilla_integrations`:

```text
registered=1 capability=1
```

## Remaining P06 gate work

This does not close the P06 critical gate. The SMS, Email, Templates, and Health tabs still need real forms/health behavior, and Theme-only administration must be implemented and tested. The P06 guide item remains unchecked until those changes and their human QA are complete.
