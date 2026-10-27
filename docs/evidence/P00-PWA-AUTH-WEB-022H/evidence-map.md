# P00-PWA-AUTH-WEB-022H — evidence map

## Automated verification — 2026-10-27

- `:core:data:compileKotlinJs` and `:core:data:compileKotlinJvm` passed.
- `:core:data:jsBrowserTest --tests com.kazemieh.data.auth.repository.BrowserSessionLifecycleTest`
  passed in ChromeHeadless.
- `:core:network:jsBrowserTest --tests com.kazemieh.network.common.BrowserLogoutRequestTest`
  passed in ChromeHeadless. The test covers successful logout and rejected
  `401`/`503` logout responses, and proves no bearer header, request body, or
  refresh request is sent.
- The test runner's known generated Skiko asset omission was fixed only under
  ignored `build/` output; no generated artifact was added to source control.

## Implementation boundaries

- Browser tabs exchange only the static sign-out signal through
  `BroadcastChannel`; no token, email, profile data, or cookie is published.
- The receiving tab clears its own transient token and profile namespace.
- Native/JVM implementations are explicit no-ops, so persistent native token
  and refresh behavior is unchanged.
- Local state is not cleared when server logout cannot be confirmed.

## Manual proof pending

The cross-tab browser behavior requires an owner check in two real tabs at
`http://localhost:8091/pwa/#/login`. Exact steps and success criteria are in
the task card. This evidence does not claim that manual proof has occurred.
