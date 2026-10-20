# P00-PWA-AUTH-WP-021C04B03C — synthetic mobile-meta feasibility

- Status: DONE; Size: exceeds M after scope review; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C04B03C02`; child chain starts after `P00-PWA-AUTH-WP-021C04B03B`.
- Extend the synthetic proof to `cb_mobile` INSERT/UPDATE/DELETE, duplicate meta rows, same-owner multi-reference behavior, reassignment, unsupported values, collision and concurrency/failure handling. Do not treat `billing_phone` as an identity, and do not claim OTP replay has been solved.
- Test against real synthetic WordPress and MariaDB, with no real-site writes, production normalization or route. Evidence: `docs/evidence/P00-PWA-AUTH-WP-021C04B03C/`. Split or stop if needed.

## Scope review — 2026-10-18

The user-row-only fixture releases an identity while two same-owner mobile references still exist, and real WordPress then accepts that mobile for a second synthetic owner. Fixing this safely requires a shared cross-table reference model and a separate reassignment/concurrency/failure matrix; that exceeds this card's M scope. Children `021C04B03C01` and `021C04B03C02` are queued sequentially. No mobile protection or production readiness is claimed. Evidence: [mobile-baseline.md](../evidence/P00-PWA-AUTH-WP-021C04B03C/mobile-baseline.md).

## Completion — 2026-10-20

The two child matrices now cover the stated mobile metadata lifecycle and bounded failure case. The previous native-delete false-success is no longer used for managed Carmilla accounts; the controlled path and owner QA are recorded in `021C04B03C02`. Migration remains the next separate task.
