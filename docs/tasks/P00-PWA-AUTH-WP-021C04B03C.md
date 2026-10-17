# P00-PWA-AUTH-WP-021C04B03C — synthetic mobile-meta feasibility

- Status: TODO; Size: M; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C04B03B`.
- Extend the synthetic proof to `cb_mobile` INSERT/UPDATE/DELETE, duplicate meta rows, same-owner multi-reference behavior, reassignment, unsupported values, collision and concurrency/failure handling. Do not treat `billing_phone` as an identity, and do not claim OTP replay has been solved.
- Test against real synthetic WordPress and MariaDB, with no real-site writes, production normalization or route. Evidence: `docs/evidence/P00-PWA-AUTH-WP-021C04B03C/`. Split or stop if needed.
