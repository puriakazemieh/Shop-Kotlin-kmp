# ADR-004: FeatureManifest v1 contract

- **Status:** PROPOSED
- **Date:** 2026-08-26
- **Deciders:** Product Owner, Tech Lead (AI)
- **Task:** `P03-MANIFEST-ADR-001`
- **Supersedes:** The manifest details in ADR-002; ADR-002 remains the decision for hybrid tenancy.

## Context

`BrandConfig` currently mixes branding, backend origin, and vertical flags. The product must instead ship only the `WORDPRESS` and `SPRING` backend profiles, while a validated feature manifest controls tenant capabilities within the compiled feature ceiling. This document freezes the first contract consumed by KMP, WordPress, and the later Spring implementation.

## Decision

### Trust boundary

- `BackendProfile` is trusted bootstrap/build configuration. It owns `apiRoot`, `assetRoot`, `allowedAuthHosts`, contract version, and the manifest URL.
- A remote manifest **cannot** change backend origin, asset origin, allowed authentication hosts, package identity, signing identity, or compiled feature ceiling.
- Android includes a bundled default manifest. A remote manifest may only reduce its capability set.
- PWA receives a manifest only from a trusted same-origin deployment or the profile's configured signed endpoint; query-string configuration is forbidden.

### Envelope and validation

- `schemaVersion` is the integer `1`.
- `backendProfile` is exactly `WORDPRESS` or `SPRING` and must equal the immutable profile compiled into the artifact.
- `tenantId`, `manifestVersion`, `issuedAt`, `minimumAppVersion`, `features`, and `integrity` are mandatory.
- `integrity.algorithm` is `Ed25519`; `keyId` selects a pinned public key; `signature` covers the canonical JSON payload excluding `integrity` itself.
- Validation order is: JSON parsing → schema version → profile equality → signature → expiry/version policy → known feature IDs → dependency resolution → compiled ceiling/platform policy.
- Any failure produces the safe bootstrap state: no sensitive feature, route, DI module, use case, or backend endpoint becomes available. Clinic, psych, admin, and payment are always fail-closed.
- An unknown feature key, unknown integrity algorithm, invalid signature, incomplete dependency, or unsupported schema version rejects the whole remote manifest. Diagnostics may record only the error class and revision; they must not log tokens, signatures, or tenant secrets.

### Feature dependencies

| Feature | Requires |
|---|---|
| `commerce.physical` | `commerce.core` |
| `commerce.digital` | `commerce.core` |
| `academy.core` | `content.blog` |
| `academy.quiz` | `academy.core` |
| `academy.certificate` | `academy.core` |
| `clinic.booking` | `content.blog` |
| `clinic.messaging` | `clinic.booking` |
| `psych.tests` | `content.blog` |
| `wallet` | `commerce.core` |
| `admin.mobile` | an explicitly enabled domain feature plus backend authorization |

Feature enablement never grants authorization. The backend must enforce the same manifest entitlement and actor ownership for every write/read path.

### Backward-compatibility policy

- Clients implementing v1 accept only `schemaVersion: 1`.
- New optional fields require a new ADR and a compatible v1 default; new required fields or changed semantics require `schemaVersion: 2`.
- A client below `minimumAppVersion` uses its bundled safe manifest and displays an upgrade-required state; it does not fall back to legacy vertical flags.
- The last-known-good remote manifest may be cached only until its explicit `expiresAt`; it is namespaced by `backendProfile + tenantId + normalizedOrigin` and is purged when any of those values changes.

## JSON Schema (draft 2020-12)

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "https://carmilla.example/contracts/feature-manifest-v1.schema.json",
  "title": "Carmilla FeatureManifest v1",
  "type": "object",
  "additionalProperties": false,
  "required": ["schemaVersion", "manifestVersion", "backendProfile", "tenantId", "minimumAppVersion", "issuedAt", "expiresAt", "features", "integrity"],
  "properties": {
    "schemaVersion": { "const": 1 },
    "manifestVersion": { "type": "string", "pattern": "^[0-9]{4}\\.[0-9]{2}\\.[0-9]+$" },
    "backendProfile": { "enum": ["WORDPRESS", "SPRING"] },
    "tenantId": { "type": "string", "pattern": "^[a-z0-9][a-z0-9-]{1,62}$" },
    "minimumAppVersion": { "type": "string", "pattern": "^[0-9]+\\.[0-9]+\\.[0-9]+$" },
    "issuedAt": { "type": "string", "format": "date-time" },
    "expiresAt": { "type": "string", "format": "date-time" },
    "seedPack": { "type": "string", "maxLength": 80 },
    "features": {
      "type": "object",
      "additionalProperties": false,
      "properties": {
        "content.blog": { "type": "boolean" },
        "commerce.core": { "type": "boolean" },
        "commerce.physical": { "type": "boolean" },
        "commerce.digital": { "type": "boolean" },
        "academy.core": { "type": "boolean" },
        "academy.quiz": { "type": "boolean" },
        "academy.certificate": { "type": "boolean" },
        "clinic.booking": { "type": "boolean" },
        "clinic.messaging": { "type": "boolean" },
        "psych.tests": { "type": "boolean" },
        "wallet": { "type": "boolean" },
        "admin.mobile": { "type": "boolean" }
      }
    },
    "integrity": {
      "type": "object",
      "additionalProperties": false,
      "required": ["algorithm", "keyId", "signature"],
      "properties": {
        "algorithm": { "const": "Ed25519" },
        "keyId": { "type": "string", "pattern": "^[A-Za-z0-9._-]{1,64}$" },
        "signature": { "type": "string", "minLength": 64, "maxLength": 512 }
      }
    }
  }
}
```

## Reference presets F0–F4

All examples use the same envelope fields and a placeholder signature (`<signed-by-pinned-key>`). They are synthetic and must not be used as production tenant data.

| Preset | Enabled features | Purpose |
|---|---|---|
| F0 | `content.blog` | content-only safe baseline |
| F1 | F0 + `commerce.core`, `commerce.physical` | shop-only first release |
| F2 | F0 + `commerce.core`, `commerce.digital`, `academy.core`, `academy.quiz`, `academy.certificate` | academy tenant |
| F3 | F0 + `clinic.booking`, `clinic.messaging` | clinic tenant; backend relationship checks required |
| F4 | F0 + `psych.tests` | psych tenant; consent/retention policy required |

```json
{
  "schemaVersion": 1,
  "manifestVersion": "2026.08.1",
  "backendProfile": "WORDPRESS",
  "tenantId": "fixture-f1-shop",
  "minimumAppVersion": "1.0.0",
  "issuedAt": "2026-08-26T00:00:00Z",
  "expiresAt": "2026-09-02T00:00:00Z",
  "seedPack": "shop-fa-v1",
  "features": {
    "content.blog": true,
    "commerce.core": true,
    "commerce.physical": true,
    "commerce.digital": false,
    "academy.core": false,
    "academy.quiz": false,
    "academy.certificate": false,
    "clinic.booking": false,
    "clinic.messaging": false,
    "psych.tests": false,
    "wallet": false,
    "admin.mobile": false
  },
  "integrity": {
    "algorithm": "Ed25519",
    "keyId": "fixture-key-v1",
    "signature": "<signed-by-pinned-key>"
  }
}
```

F0, F2, F3, and F4 use this identical envelope with the enabled-feature matrix above. Their complete generated fixtures and signature verification tests are implementation work for `P03-MANIFEST-CODE-007` through `P03-MANIFEST-CODE-008`.

## Consequences

- `P03-ARCH-CODE-003` separates manifest, profile, branding, and build identity from `BrandConfig`.
- `P03-ARCH-CODE-004` implements the immutable profile trust boundary.
- `P03-MANIFEST-CODE-007` and `P03-MANIFEST-CODE-008` implement the catalog, dependency resolver, compiled ceiling, schema parsing, and preset fixtures.
- WordPress/Spring endpoints must emit this envelope and never send secrets in it.

## Approval requested

Approve or request changes to: the strict rejection of unknown feature keys, Ed25519 as the v1 signature algorithm, the five F0–F4 preset definitions, and the rule that v1 accepts no future schema version.
