# ADR-003: Observability and Analytics Strategy

- **Status**: PROPOSED
- **Date**: 2026-08-05
- **Deciders**: Tech Lead (AI), Product Owner

## Context
As Carmilla moves from prototype to a production white-label platform, it requires a structured way to monitor system health (Operational Logs), ensure compliance (Audit Logs), and measure user success (Product Analytics). Currently, logging is scattered using `Kermit` without a centralized taxonomy.

## Decisions

### 1. Log Categorization
We will enforce a strict separation between three types of telemetry data:

| Type | Purpose | Audience | Retention |
|---|---|---|---|
| **Audit Logs** | Security & Compliance (e.g., login, payment verify, record delete) | Security / Legal | 5+ Years |
| **Operational Logs**| System Health (Errors, Latency, Resource Usage) | Engineering / SRE | 30-90 Days |
| **Product Analytics**| User Behavior (Funnel completion, feature usage) | Product / Growth | Indefinite (Aggregated) |

### 2. Client-Side Abstraction
We will introduce an `AnalyticsTracker` interface in `core:common` or a dedicated `core:telemetry` module.
- Features will depend on the interface, not specific providers (Firebase, Mixpanel, etc.).
- Provider implementation will be injected via Koin based on the `BackendProfile` and tenant config.

### 3. Redaction and Privacy
- **Strict Prohibition**: No PII (Personal Identifiable Information), PHI (Protected Health Information), or Secrets (Tokens, Keys) are allowed in log messages or analytics parameters.
- **Auto-Redaction**: Interceptors must be used in the Network layer to mask `Authorization` headers and sensitive body fields (e.g., `password`, `otpCode`).

### 4. User Consent
- Analytics collection must be **Opt-In** for production builds.
- A "Privacy & Data" section in Settings will allow users to toggle telemetry and export/delete their tracked data.

### 5. Backend Parity
Both `SPRING` and `WORDPRESS` backends must emit events using the same taxonomy (IDs and Parameter names) to allow unified dashboards.

## Rationale
- **Compliance**: Separation of Audit logs is required for many regulatory standards.
- **Performance**: High-volume operational logs should not be mixed with long-term analytics.
- **Portability**: The abstraction layer allows white-label tenants to plug in their own tracking IDs without source code changes.

## Consequences
- **Positive**: Better debugging, clear user insights, and improved security posture.
- **Negative**: Adds a small development overhead to every new feature to ensure instrumentation.
- **Technical**: Requires implementing a standardized `EventBus` for telemetry handoff.
