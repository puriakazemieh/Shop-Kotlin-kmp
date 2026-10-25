# Guarded login services reconciliation

2026-10-25. This reconciliation was refreshed after the bounded managed-deletion
implementation and its owner QA. It accepts the supported Carmilla writer
boundary only; it does not claim safety for native WordPress or third-party
writers outside that boundary.

| Area | Evidence | Result |
| --- | --- | --- |
| Database adapter, origin/cookie policy, bearer binding | `021C01`–`021C03` evidence | Isolated automated evidence exists; no route deployment. |
| Password registration and first-OTP account creation | `021C04B03D` and `021C04B03D01` | Supported writer and owner QA complete; post-callback conflict fails closed with recovery required. |
| Managed identity lifecycle and deletion | `021C04B03C02` | Bounded managed deletion and owner QA complete; dependent accounts are refused. Native WP and third-party writers remain outside the accepted boundary. |
| OTP consumption | `021C04C` and `021C04C03` | Database-backed OTP consumption and owner QA complete; persistent object-cache mode fails closed. |

## Decision

`021C04` is accepted for its isolated Carmilla-supported handler boundary and
`021C05` is READY. No route, retained site, live network behavior or PWA
configuration was changed by this reconciliation. Public browser-session
activation remains deferred to `021C07`, where the remaining native/third-party
writer and recovery limitations must stay fail-closed.
