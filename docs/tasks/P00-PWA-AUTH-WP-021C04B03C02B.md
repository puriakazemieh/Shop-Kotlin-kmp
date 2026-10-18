# P00-PWA-AUTH-WP-021C04B03C02B — reassess identity strategy against native lifecycle

- Status: DONE; Size: M; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C04B03C02A`; starting evidence: `docs/evidence/P00-PWA-AUTH-WP-021C04B03C02/` and `docs/evidence/P00-PWA-AUTH-WP-021C04B03C02A/`.
- Inspect actual Carmilla identity resolution, registration and WordPress writers together with the demonstrated native deletion limitation. In a bounded comparison, assess at least the current trigger-backed global-claim model and a fail-closed resolution/controlled-writer alternative, including legacy or uncooperative writers, cross-namespace collisions, concurrent creation, deletion, stale state, result reporting and irreversible callbacks. Do not assume that avoiding triggers alone ensures unique registration or safe association.
- Select a testable candidate only if its guarantees and unsupported operations are explicit and meet the parent requirements; otherwise record the precise blocker and the smallest concrete design decision or owner input needed. Identify a minimal synthetic validation matrix and a reversible compatibility/migration direction, without claiming either has already been implemented. Do not patch WordPress core, activate a route, migrate a real site, or change preserved sites.
- Evidence: `docs/evidence/P00-PWA-AUTH-WP-021C04B03C02B/`. `C02` and its parents remain blocked until their own complete acceptance is satisfied; a design comparison alone is not production readiness.

## Result — 2026-10-18

- Compared the observed browser primitive, legacy routes and supported-writer alternatives in [the evidence](../evidence/P00-PWA-AUTH-WP-021C04B03C02B/identity-lifecycle-comparison.md). No option tested so far meets the entire parent contract. No candidate was presented as a production solution.
- The smallest unresolved product decision is the failure/compatibility contract for native WP deletion versus a restricted controlled-writer path. Until resolved, keep the original contract, keep the new browser registration disabled and leave C02 blocked. No production behavior or preserved site was changed.
