# P02-CI-CODE-010C3C4B — shipped order-controller line endings

Date: 2026-11-10. No PHP logic or repository blob content changed. The Git blob of `wordpress/carmilla-bridge/includes/class-cb-order-controller.php` was already 320 LF lines, but the Windows working tree under `core.autocrlf=true` held 319 CRLF lines and one LF line. `wordpress/build-bridge-zip.sh` copies working-tree bytes, so its ZIP acquired `Internal.LineEndings.Mixed` at `includes/class-cb-order-controller.php:1:1`.

GitNexus impact on the exact `CB_Order_Controller` class UID found one direct caller, `CB_Plugin.register_routes`, with LOW risk. The fix adds a path-specific `text eol=lf` rule in `.gitattributes` and normalizes the local working-tree bytes. `git check-attr` confirms `text: set, eol: lf`. The extracted new ZIP file is 320 LF / 0 CRLF, SHA-256 `3bb8e9b4905a1e7dd3cbf9cf31116d9f644912cb330f3bcfc0542025f65774ff`, exactly matching the pre-existing Git blob SHA-256.

Verification: the official `wordpress/build-bridge-zip.sh` built ZIP SHA-256 `24764d495bdee4ee3340fc5321314aec4ab156197280c572fd92b6a2d5277525`; PHP lint on the installed file passed. The ZIP was installed over the previous version in disposable WordPress 6.4.3/PHP 8.1/MariaDB 10.6 with Plugin Check 2.1.0. The same scan had 312 findings before this change and 311 after; the sole mixed-line-ending fingerprint disappeared. Complete after-report: [plugin-check-after.txt.gz](plugin-check-after.txt.gz), uncompressed SHA-256 `2cd321a12a2225e807888f659507ed5a17bffdbf430102305baf2be37adf5c39`.

The three read-only GET warnings and wallet direct-query finding remain for C3C4C–D; this card does not claim the parent gate passed. No user test is needed for line-ending normalization.
