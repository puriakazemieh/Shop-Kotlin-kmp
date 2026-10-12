# P06 — Theme-only messaging fallback

Date: 2026-10-12

## Change

- Added the Core `MessageSender` service. It configures the site-owned SMTP
  settings for `wp_mail()` and sends a generic SMS through the existing
  HTTPS-only, redirect-free `wp_safe_remote_request` contract.
- Added the Theme-only `Carmilla → Integrations` fallback. It uses the same
  `carmilla_message_settings` option, never renders stored secrets, and offers
  SMS, Email, Templates, Health and Test views.
- The fallback is not registered when Bridge is active, so co-install has one
  integrations page. Theme dashboard registration now runs after the Core menu.

## Automated verification

| Check | Result |
| --- | --- |
| PHP 8.2 syntax lint: Kernel, MessageSender, Theme messaging and admin files | pass |
| `wordpress/packages/carmilla-core/tests/smoke-message-sender.php` | pass — HTTPS SMS, non-HTTPS rejection, `wp_mail` fallback |
| Theme ZIP contains fallback UI and MessageSender | pass |
| Browser Playwright check of Theme-only admin page | pass — active theme `carmilla-theme`, Bridge inactive, Integration page and all five tabs render |

## Preserved local test environment

The Theme ZIP was installed into the existing preserved destination installation
at `D:\Carmilla-QA\p07-wordpress-destination`; no WordPress copy was removed.
For this Theme-only check, `carmilla-bridge` is inactive and `carmilla-theme` is
the active theme. The administrator page is:

`http://localhost/carmilla-p07-destination/wp-admin/admin.php?page=carmilla-integrations`

The browser verification confirmed that the sole Carmilla top-level link points
to `admin.php?page=carmilla-settings`, and its Integrations submenu points to
`admin.php?page=carmilla-integrations`.

## Human QA still required

Manual QA confirmation received from the product owner on 2026-10-12: the
Theme-only configuration and test-send flow completed successfully in the
preserved destination WordPress installation. No recipient, secret, or provider
credential is recorded in this repository evidence.

This confirmation closes only the P06 critical stop for the messaging settings,
capability, operational panel and Theme-only send path. The separate P06 work
for AEAD/key rotation, queue privacy/idempotency and provider mapping remains
open and is not represented as complete here.

## Rollback

Deactivate `carmilla-theme` in the preserved destination installation and
activate `twentytwentyfive`; then reactivate `carmilla-bridge` if the prior
Bridge-only configuration is wanted. Site-owned message settings remain intact.
