# WPCS integration/manifest/bootstrap triage — 2026-11-03

Pinned report `.artifacts/p02-ci-010c2/wpcs-report.json`, SHA-256 `5F3C5C4E430C39C7704F9B3ADB34348F5A6E394312421830CA0FD6285D7C3A64` (WPCS 3.4.1 / PHPCS 3.13.6). Filter: `WordPress.Security.*` or `WordPress.DB.*` in integration, manifest, message queue, plugin bootstrap files. Exactly 28 findings; duplicate sniffs at one line have distinct report columns. Line numbers are from the pinned report.

| File | Line:col | Sniff | Disposition | Source-backed reason / follow-up |
| --- | --- | --- | --- | --- |
| `class-cb-integrations-controller.php` | 18 | `WordPress.Security.EscapeOutput.OutputNotEscaped` | CONTEXT JUSTIFIED | `wp_die` gets a fixed translated permission message after capability denial, not request data. |
| same | 23 | `WordPress.Security.ValidatedSanitizedInput.MissingUnslash` | FIX REQUIRED | Test-message type is sanitized but not unslashed; B5A. |
| same | 24 | `WordPress.Security.ValidatedSanitizedInput.MissingUnslash` | FIX REQUIRED | Recipient is sanitized but not unslashed; B5A. |
| same | 25 | `WordPress.Security.ValidatedSanitizedInput.MissingUnslash` | FIX REQUIRED | Body is sanitized but not unslashed; B5A. |
| same | 28 | `WordPress.Security.EscapeOutput.OutputNotEscaped` | CONTEXT JUSTIFIED | `wp_die` gets only a fixed translated required-fields message. |
| same | 48 | `WordPress.Security.SafeRedirect.wp_redirect_wp_redirect` | FIX REQUIRED | Target is currently static `admin_url('admin.php')` plus fixed args, but use host-validated redirect; B5A. |
| same | 52 | `WordPress.Security.EscapeOutput.OutputNotEscaped` | CONTEXT JUSTIFIED | Exception detail is passed through `esc_html`; prefix is fixed translated text. Logging the raw exception is a separate privacy concern in B5B. |
| same | 78:30 | `WordPress.Security.NonceVerification.Recommended` | CONTEXT JUSTIFIED | `tab` selects a read-only admin page view; no write or state transition. |
| same | 78:68 | `WordPress.Security.NonceVerification.Recommended` | CONTEXT JUSTIFIED | Same read-only `tab` selector, second reported expression. |
| same | 78:68 | `WordPress.Security.ValidatedSanitizedInput.MissingUnslash` | FIX REQUIRED | Read-only tab value is sanitized but not unslashed; B5A. |
| `class-cb-manifest-controller.php` | 30:33 | `WordPress.Security.ValidatedSanitizedInput.MissingUnslash` | CONTEXT JUSTIFIED | Nonce and `manage_options` precede save; submitted array is only projected onto known feature IDs and cast to booleans, so slashes have no semantic effect. |
| same | 30:33 | `WordPress.Security.ValidatedSanitizedInput.InputNotSanitized` | CONTEXT JUSTIFIED | Same known-key boolean projection; raw keys/values are not echoed or persisted. |
| same | 62 | `WordPress.Security.NonceVerification.Recommended` | CONTEXT JUSTIFIED | Redirect query string only displays an admin notice; it does not mutate manifest state. |
| same | 63 | `WordPress.Security.NonceVerification.Recommended` | CONTEXT JUSTIFIED | `cb_manifest_updated` only selects fixed success copy; no mutation. |
| same | 63 | `WordPress.Security.ValidatedSanitizedInput.InputNotSanitized` | CONTEXT JUSTIFIED | Only `isset` is used; raw value never reaches output. |
| same | 64 | `WordPress.Security.NonceVerification.Recommended` | CONTEXT JUSTIFIED | Same fixed notice path. `cb_manifest_error` array shape can still break formatting; B5C. |
| `class-cb-message-queue.php` | 56 | `WordPress.Security.EscapeOutput.ExceptionNotEscaped` | FIX REQUIRED | Adapter error text is concatenated into a thrown exception and may reach Action Scheduler logs; B5B uses a safe failure code and keeps diagnostics redacted. |
| `class-cb-plugin.php` | 101 | `WordPress.Security.ValidatedSanitizedInput.MissingUnslash` | CONTEXT JUSTIFIED | Server `REQUEST_METHOD` is uppercased and compared only to literal `OPTIONS`; it is not a slashed form field. |
| same | 101 | `WordPress.Security.ValidatedSanitizedInput.InputNotSanitized` | CONTEXT JUSTIFIED | Same fixed-method comparison; no raw value emitted. |
| same | 115 | `WordPress.Security.ValidatedSanitizedInput.MissingUnslash` | CONTEXT JUSTIFIED | Server `REQUEST_URI` is parsed to a path and constrained to a fixed root-alias prefix list; it is not a slashed form field. Logging full path and dispatch behavior are reviewed in B5D. |
| same | 115 | `WordPress.Security.ValidatedSanitizedInput.InputNotSanitized` | CONTEXT JUSTIFIED | Same fixed-prefix path match before any alias dispatch; no arbitrary route is selected. |
| same | 134 | `WordPress.Security.ValidatedSanitizedInput.MissingUnslash` | FIX REQUIRED | Alias copies raw query values into a synthetic REST request; compare to native REST sanitation and reject malformed shapes. B5D. |
| same | 134 | `WordPress.Security.ValidatedSanitizedInput.InputNotSanitized` | FIX REQUIRED | Same raw query value path; B5D. |
| same | 136 | `WordPress.Security.NonceVerification.Recommended` | FIX REQUIRED | Alias supports write HTTP verbs; verify cookie nonce/bearer authentication at the alias boundary with HTTP tests. B5D. |
| same | 221 | `WordPress.Security.ValidatedSanitizedInput.MissingUnslash` | CONTEXT JUSTIFIED | Server Authorization header is a credential, not a slashed form field; decoder verifies JWT before assigning user. |
| same | 221 | `WordPress.Security.ValidatedSanitizedInput.InputNotSanitized` | CONTEXT JUSTIFIED | Credential must remain byte-for-byte for signature verification; `CB_JWT::decode` and browser token verifier gate it. B5D checks malformed header shape. |
| same | 223 | `WordPress.Security.ValidatedSanitizedInput.MissingUnslash` | CONTEXT JUSTIFIED | Alternate server Authorization header has the same credential semantics. |
| same | 223 | `WordPress.Security.ValidatedSanitizedInput.InputNotSanitized` | CONTEXT JUSTIFIED | Same signature-verified credential path, not rendered or stored. |

Count: 28 rows; 19 context justified, 9 fix-required findings grouped into B5A integration form/redirect, B5B message queue error privacy and B5D root alias auth/query handling. B5C separately hardens the manifest notice input shape observed during source review. All four are S/M product tasks. This is source-backed triage, not a claim that the fixes or CI gate have passed. No product source changed in this review.
