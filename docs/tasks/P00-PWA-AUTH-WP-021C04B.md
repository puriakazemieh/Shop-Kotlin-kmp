# P00-PWA-AUTH-WP-021C04B — isolated guarded registration

- Status: DONE; Size: exceeds M after investigation; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C04B06` (child chain begins after `021C04A`).
- Implement/test guarded registration against real WordPress users and the common token handoff. Respect server registration policy and WP validation/filter outcomes; prevent duplicate/ambiguous identity and unsafe account association. Preserve mobile API behavior. Account creation followed by session failure must report the partial outcome accurately without deleting the user.
- Test invalid input, existing identities, concurrent/ambiguous cases, WP failure, guard rejection, account creation and storage failure/redaction using synthetic WordPress and MariaDB only. If identity uniqueness cannot be safely established with current schema, fail closed and document the precise limitation; do not declare the feature production-ready.
- No hooks, routes, cookie emission, migration or edits to preserved sites. Record redacted evidence in `docs/evidence/P00-PWA-AUTH-WP-021C04B/`; owner network QA remains `021C07`.

## Investigation and blocker — 2026-10-17

- A deterministic two-process test against actual WordPress 6.4.3, 6.6.2 and 7.1.3 and a disposable MariaDB reproduced two accounts with the same login and email. The core user-login/email indexes and `cb_mobile` usermeta are not unique; a check before `wp_insert_user()` is therefore not a safe uniqueness guarantee. Other WP/plugin entry points do not necessarily honor a private lock in the new browser service.
- No registration endpoint or cookie emission was enabled, and no actual WordPress installation was changed. The existing mobile registration endpoint was not modified and is not claimed secure by this investigation. Password login was separately hardened to reject already-ambiguous identities.
- A safe end-to-end implementation needs an identity-ownership/concurrency design covering existing duplicate data, cross-namespace collisions and writers outside this service, a reversible migration/compatibility strategy and owner migration QA, followed by guarded registration and real concurrency/partial-failure tests. That exceeds this M card and its explicit no-migration scope. Do not treat a process-local or advisory lock used only by the new route as a global guarantee.
- Proposed sub-tasks: (1) design the shared identity invariants and audit existing writers/schema without exposing account data; (2) design and test a reversible, collision-aware migration on a synthetic copy with an owner review gate; (3) implement guarded registration and recovery, then test simultaneous creation and session-storage failure. These are a proposal, not completed work or newly READY cards.

## Completion — 2026-10-20

The split chain is complete for supported Carmilla writers. Registration is an
inactive guarded service and does not activate an endpoint or alter retained
sites. It relies on the owner-confirmed synthetic migration gate, rejects
ambiguous identity and retains a recovery marker rather than deleting a partially
created account. Third-party/raw writers remain out of scope and are not claimed
safe.
- Evidence and reproduction: `docs/evidence/P00-PWA-AUTH-WP-021C04B/identity-race.md`. Until resolved, this card and dependent `021C04C`–`021C08` are not complete; no owner browser QA is ready.

## Split — 2026-10-17

After the owner requested continuation, the proposed work was split into `021C04B01`–`021C04B06` in dependency order. The parent remains blocked; the split does not assert a chosen storage architecture or change the no-live-migration restriction. If a child cannot establish its prerequisites safely or exceeds M, stop and revise that child rather than assuming success.
