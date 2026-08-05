# ADR-002: Hybrid Core Architecture, Backend Profiles, and Manifest-Driven Tenancy

- **Status**: PROPOSED
- **Date**: 2026-08-05
- **Deciders**: Product Owner, Tech Lead (AI)
- **References**: Sections 21-29 of `PROJECT_AUDIT_AND_PUBLICATION_PLAN_FA.md`

## Context
The current project structure has multiple Android flavors for different verticals, which leads to configuration sprawl and build-time explosion. Furthermore, the WordPress theme contains significant business logic, creating a "plugin territory" violation and data silos between the theme and the bridge plugin.

## Decisions

### 1. Two Backend Profiles
We will collapse all build variants into exactly two backend profiles:
- **`WORDPRESS`**: Targets the Carmilla Bridge plugin on a WordPress/WooCommerce site.
- **`SPRING`**: Targets the standalone Spring Boot Kotlin backend.
Verticals (Shop, Academy, Clinic, etc.) are **not** build flavors; they are runtime capabilities.

### 2. Feature Manifest (Hybrid Model)
Tenant capabilities will be controlled by a versioned JSON Manifest.
- **Compiled Ceiling**: The binary includes a set of available modules.
- **Manifest Overlay**: A remote or bundled JSON defines which features are enabled for the specific tenant.
- **Fail-Closed**: If the manifest is missing, invalid, or specifies a feature above the compiled ceiling, the app must default to a safe, disabled state.

### 3. WordPress Theme/Core Boundary
- **Core Plugin**: Sole owner of Domain models, Custom Tables (CPTs), REST Controllers, Auth, and Business Logic.
- **Theme**: Presentation-only. It must consume the Core Plugin's API/Services. Switching themes must not destroy or hide business data.

### 4. Customer Overlay Strategy
Instead of permanent source forks or git branches per customer, we will use a **Customer Overlay** system:
- **Base Product**: The core source code shared by all.
- **Overlay**: Versioned configuration, resources (icons, colors), and branding manifest specific to a customer.
- **Building**: The Build Service (managed) applies the overlay onto the base template during the artifact generation phase.

### 5. Unified Identity & Package Policy
- Each application must have a unique `applicationId` / `bundleId` per customer.
- Signing keys are owned by the customer (or managed for them) and never stored in the base repository.

## Rationale
- **Scalability**: Decoupling flavors from verticals prevents build variant explosion.
- **Security**: Server-side enforcement of the manifest ensures features cannot be unlocked simply by client-side hacking.
- **Portability**: Standardizing the WordPress boundary allows the plugin to be listed on WordPress.org and work with 3rd party themes.
- **Maintainability**: The overlay strategy allows core updates to be rolled out without manually merging customer-specific branches.

## Consequences
- **Positive**: Simplified build logic, clear ownership of data, and easier onboarding for new white-label customers.
- **Negative**: Requires a migration effort for existing legacy flavors and moving logic out of the current theme.
- **Technical**: Requires implementing a `FeatureGuard` in the client (UI/Navigation) and Backend (API) that respects the manifest.
