# P00-PWA-AUTH-WP-021C04B03C02I — bounded incident discovery

- Status: DONE; Size: S; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C04B03C02H`. The owner authorized independently executable Phase 00 work despite the blocked parent.
- Extend the inactive incident store with a bounded installation-scoped discovery operation so an authorized administrator can find unexpired recorded incidents without already knowing an incident ID. Guard before querying; deny other roles, bound the result size, fail closed on malformed data, clock or database failure, and return only minimal server-side references. Preserve the existing API and no hooks, routes, UI, migration or site changes.
- Exercise real isolated WordPress and MariaDB with synthetic data for administrator/editor/logged-out access, installation isolation, bounds, expiry and storage failure; rerun the prior storage and observer regressions. Include syntax checks and redacted evidence in `docs/evidence/P00-PWA-AUTH-WP-021C04B03C02I/`. If the scope exceeds S/M, split or stop.
- Discovery is not an operational warning, durable outage fallback, recovery, approved privacy/retention policy, product activation or proof that native deletion succeeded. The parent and its dependencies remain blocked.

## Completion — 2026-10-18

Inactive bounded discovery was added and the synthetic real-WordPress storage and observer regressions passed on three combinations; see [verification](../evidence/P00-PWA-AUTH-WP-021C04B03C02I/verification.md). The result is limited to the newest 100 unexpired records and has no pagination or resolution workflow. No manual UI test applies to this internal component. The parent remains BLOCKED.
