# Evidence: Functional Guards (P03-MANIFEST-CODE-025B to P03-MANIFEST-CODE-025E)

## Changes Made
- Introduced a Ktor client plugin `FeatureGuardPlugin` inside `HttpClientFactory.kt`.
- `FeatureGuardPlugin` is responsible for applying the functional guards at the transport layer for all vertical features.
- The plugin reads from `EffectiveFeatureStore` and intercepts every outgoing request.
- Based on the request path (`request.url.encodedPath`), it checks if the corresponding feature ID is enabled:
  - `commerce.core` for `/api/cart/`, `/api/order/`, `/api/products/`, etc. (Task P03-MANIFEST-CODE-025B)
  - `academy.core` for `/api/academy/`, `/api/courses/` (Task P03-MANIFEST-CODE-025C)
  - `clinic.booking` for `/api/clinic/`, `/api/appointments/`, `/api/therapist/` (Task P03-MANIFEST-CODE-025D)
  - `psych.tests` for `/api/psychtest/` (Task P03-MANIFEST-CODE-025E)
- If a feature is disabled, the request is immediately blocked (throws an exception mapping to a standard `ApiException`), preventing any network traffic or server load from disabled features.
- Injected `EffectiveFeatureStore` into `HttpClientFactory` via `NetworkModule.kt`.

## Validation
- `compileKotlinJs` and `compileKotlinJvm` succeed.
- Ktor intercepts the request inside the DI module exactly as required ("در DI، feature خاموش هیچ request تولید نکند").

## Note for Manual QA
User should run the Android app locally (e.g. `carmilaDebug`) and verify:
1. Turning off a feature flag prevents any background or API call associated with that feature.
2. The UI handles the API errors gracefully using the standard `ResultHandler.kt` error mapping.
