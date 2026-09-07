# ADR-007: Payment Infrastructure & Unified Checkout

**Status:** Accepted
**Date:** 2026-09-07

## Context
Phase 05 introduces native payment gateway capabilities (ZarinPal), Buy-Now-Pay-Later (BNPL) flows, and virtual wallet systems directly into the `carmilla-core` and KMP clients. We need to unify how Theme, Plugin, and Headless modes resolve payment contexts and how deep-links redirect users back to the app after a web-based payment flow.

## Decision
1. **Shared Controller:** The existing `class-cb-payment-controller.php` will be integrated into the core architecture. It will expose `api/payment/request` and `api/payment/verify`.
2. **Settings Authority:** Payment keys (`cb_zarinpal_merchant`, `cb_zarinpal_sandbox`) will be managed via the new `SettingsService` built in Phase 04, ensuring strict capability checks.
3. **Deep Linking:** The return URL to the KMP app will be read from `cb_app_return_url`. The WordPress backend will return a 302 redirect directly back to the app schema (e.g., `carmilla://payment/result?status=success&order=123`).
4. **Order Status Mutation:** ZarinPal verifications will securely mutate WooCommerce HPOS order statuses (from `pending` to `processing` or `failed`) without direct SQL queries, strictly utilizing the `WooCommerceAdapter`.

## Consequences
- Requires strict nonces/JWT for the `request` endpoint.
- Webhooks or synchronous returns must properly validate the transaction signature before completing the WooCommerce order.
- BNPL or Wallet endpoints will be built alongside this as new payment methods within the existing checkout flow.
