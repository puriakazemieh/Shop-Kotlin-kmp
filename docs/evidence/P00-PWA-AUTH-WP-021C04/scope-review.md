# P00-PWA-AUTH-WP-021C04 — scope and OTP review

Date: 2026-10-17. Static review, no live network change.

`021C04` combines three independently stateful flows: WP-password authentication filters and browser-session creation, registration including uniqueness/partial creation and recovery, and OTP verification/login including first-use account creation. Each must check the browser guard before mutation, keep refresh credentials out of public responses and logs, handle session/token/storage failures, preserve mobile behavior, and be tested against real WordPress and a real session database. As a single security-critical card this exceeds M.

The existing `CB_Auth_Controller::verify_otp()` is private and reads, checks, then deletes a transient separately. The current code does not establish atomic single-use under concurrent requests. Simply copying it, bypassing WP authentication filters, or testing a mock OTP callback alone would not establish safe integration. Existing mobile behavior cannot be silently replaced or claimed to be tested end-to-end. Registration's existing `cb_mobile` metadata lookup also has no database uniqueness constraint: cross-account ambiguity and concurrent creation need explicit handling before enabling OTP.

Split the parent into the sequential, independently testable children:

- `021C04A`: guarded password login and a common, non-serializable credential handoff; actual WP authentication filters and real DB synthetic tests.
- `021C04B`: guarded registration with explicit duplicate/partial-creation behavior and synthetic real-WP tests.
- `021C04C`: OTP verification, identity mapping and guarded login, including concurrency/replay and compatibility analysis; fail closed where exactly-once verification or identity ownership cannot be proven. If still larger than M, split again before implementation.
- `021C04D`: reconcile all children and parent evidence before making `021C05` READY.

No public route/hook/cookie emission, existing QA-site modification, user deletion or production deployment is authorized by these child cards. Migration and network owner QA remain `021C06` and `021C07`; browser/PWA end-to-end confirmation remains `P00-PWA-AUTH-QA-023`.
