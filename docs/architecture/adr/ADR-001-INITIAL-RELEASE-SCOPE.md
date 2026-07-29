# ADR-001: Scope of First Stable Release (v1.0)

- **Status**: PROPOSED
- **Date**: 2026-07-29
- **Deciders**: Product Owner, Lead Developer (AI)

## Context
The Carmilla project is a highly ambitious multi-vertical platform (Shop, LMS, Clinic, Psych Tests) supporting multiple platforms (Android, iOS, Web, Desktop, WordPress). Attempting to release all features across all platforms simultaneously introduces high security risks and delays revenue.

## Decision
We will freeze the scope of the first stable release (v1.0) to a "Minimum Viable Commercial Product" centered around the WordPress ecosystem.

### 1. In-Scope for v1.0
- **Vertical**: Shop-only (Physical & Digital products via WooCommerce).
- **Backend**: WordPress (via Carmilla Bridge plugin).
- **Frontend 1**: Carmilla WordPress Theme (Presentation layer).
- **Frontend 2**: Progressive Web App (PWA) built with Compose Multiplatform (JS).
- **Core Features**: Auth (OTP/Password), Product Catalog, Cart, Checkout, ZarinPal Payment, Order Tracking.

### 2. Out-of-Scope (Deferred to v1.1+)
- **Verticals**: LMS (Academy), Clinic (Consultation), Psych Tests.
- **Backend**: Spring Boot Server.
- **Platforms**: Native Android App, Native iOS App, Desktop App.
- **Complex Features**: Wallet/Ledger, Product Comparison, Advanced Analytics.

## Rationale
- **Revenue**: The WordPress marketplace (Zhaket/Ratlchin) is the fastest path to monetization.
- **Stability**: Focus on one vertical (Shop) allows for thorough security and concurrency testing.
- **Architecture**: Decoupling the Theme from the Core Plugin is a prerequisite that must be solved for v1.0 success.

## Consequences
- **Positive**: Faster time-to-market, lower support burden initially, clear marketing message.
- **Negative**: Users expecting the full multi-platform experience will have to wait for later updates.
- **Technical**: All non-shop features must be safely disabled via Feature Toggles (fail-closed) in the v1.0 codebase.
