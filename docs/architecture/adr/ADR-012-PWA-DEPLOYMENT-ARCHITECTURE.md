# ADR-012: Web/PWA Independent Deployment Architecture

## Status
Accepted

## Context
Carmilla Web/PWA clients are built with Compose Multiplatform (Kotlin/JS or Kotlin/Wasm). We have two distinct backends (WORDPRESS and SPRING).
We need to define how the PWA is deployed, taking into account CORS, Cookies, Authentication, Cache, and SEO boundaries.
Additionally, WordPress has three host modes:
1. PWA as a Theme (integrated)
2. PWA as a Plugin injected at /app/
3. PWA hosted on an independent Origin (e.g. Vercel, S3) calling WP REST API.

Spring Boot backend will strictly operate in a decoupled (independent Origin) or API Gateway mode.

## Decision
We establish the following deployment and security boundaries:

### 1. CORS & Origin Boundary
- **Integrated WP (Theme/Plugin):** No CORS issues. The PWA shares the origin with WP.
- **Independent Origin (WP/Spring):** The backend MUST whitelist the trusted PWA origins. The PWA will use etch with credentials: 'include' for cookies or Bearer tokens.

### 2. Authentication & Cookie Boundary
- **WordPress Backend:** 
  - If Integrated: Uses native WP Auth Cookies.
  - If Independent: Uses WP Application Passwords (JWT) or OAuth2 tokens. Cross-site cookies are restricted by SameSite=Lax or None (requiring HTTPS).
- **Spring Backend:** Uses strictly stateless JWT via Authorization: Bearer header.

### 3. Cache Boundary (Service Worker)
- The PWA Service Worker will cache static assets (JS, Wasm, CSS).
- API calls to /wp-json/* or /api/v1/* will bypass the cache or use a Network First strategy.

### 4. SEO Boundary
- **Integrated WP:** WordPress handles SSR for SEO (OpenGraph, Meta tags) via PHP before handing over the DOM to the Compose JS bundle.
- **Independent Origin:** A lightweight Edge/Node layer (or static pre-rendering) is required for deep linking and SEO metadata before loading the Compose canvas.

### 5. Production Readiness
- **WordPress Mode:** Ready for integration testing.
- **Spring Mode:** **NOT** production-ready until Gate P15 (Backend integration phase) is cleared.

## Consequences
- We must configure our Webpack/Gradle builds to emit a standalone index.html (for independent origin) AND a bundled artifact that can be enqueued via wp_enqueue_script for the Theme/Plugin modes.
- Network clients in Kotlin (e.g., Ktor) must support dynamic Base URLs and token injection based on the deployment mode.
