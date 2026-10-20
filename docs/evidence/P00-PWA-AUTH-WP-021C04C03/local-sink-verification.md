# Local OTP sink verification

Date: 2026-10-20

The disposable localhost WordPress fixture was configured with the explicitly
enabled local QA sink. It has no external SMS send, production host support or
account listing.

`smoke-local-otp-sink-real-wp.php` passed three checks:

1. a synthetic OTP was captured locally without SMS;
2. the captured code authenticated once through the active route; and
3. the same code was rejected on reuse.

Human PWA UI testing, the three-invalid-attempt case and persistent-object-cache
rejection remain owner QA.

## Owner QA progress — 2026-10-20

The owner confirmed the PWA's successful first use and rejected reuse of a
local-only synthetic OTP. This preserves the card's `AWAITING_MANUAL_QA`
status: the separate three-invalid-attempt lockout scenario has not yet been
explicitly confirmed. Persistent-object-cache rejection is not applicable to
this disposable site because it has no persistent object cache.
