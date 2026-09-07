# ADR 008: Shared Messaging Infrastructure

## Status
Accepted

## Context
As part of the shift to independent products (Theme, Plugin, Clients) outlined in ADR-006, the messaging infrastructure (SMS, Email, Generic HTTP) needs to be available in the core of each server-side deployment (ZIP) without forcing a dependency on the Bridge when using only the Theme. 
Messaging must be secure, preventing any exposure of secrets, API keys, or recipient details in client payloads, HTML templates, or logs.

## Decision
1. **Shared Core Inclusion**: We will include the messaging abstraction and core provider implementations (SMS, Email, HTTP) inside wordpress/packages/carmilla-core so that any ZIP (Theme or Plugin) has access to them natively.
2. **Secure Configuration**: Provider settings (e.g., API keys for SMS providers) will be stored securely in the site's database (WordPress wp_options or Spring environment variables) and will never be serialized into client states, REST API responses, or HTML templates.
3. **Queue and Audit Contract**:
   - All outgoing messages must go through an internal queue interface to allow async processing.
   - An audit log must record message delivery statuses.
   - Logs must redact PII (like phone numbers or email addresses) and completely exclude secrets/tokens.
4. **Three Host States**:
   - **Theme Only**: The messaging core runs independently. Site admin configures the gateway directly via the Theme settings page.
   - **Plugin Only**: The messaging core runs within the Plugin. Settings are managed via the Plugin settings page.
   - **Theme + Bridge (or Spring)**: The Bridge can intercept or route messages through external services if needed, but the foundational capability remains intact in the core.

## Consequences
- **Positive**: Complete independence for the Theme. Better security posture as secrets never leave the server backend. Unified API for messaging across the entire Carmilla ecosystem.
- **Negative**: Redundant code packaging if both Theme and Plugin are installed (though handled by package autoloading). Requires strict adherence to secure logging practices.
