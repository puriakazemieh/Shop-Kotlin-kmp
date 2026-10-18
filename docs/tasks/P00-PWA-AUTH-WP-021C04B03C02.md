# P00-PWA-AUTH-WP-021C04B03C02 — synthetic mobile identity concurrency and failure

- Status: BLOCKED; Size: M; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C04B03C01`, `P00-PWA-AUTH-WP-021C04B03C02A`, `P00-PWA-AUTH-WP-021C04B03C02B`, `P00-PWA-AUTH-WP-021C04B03C02C`, `P00-PWA-AUTH-WP-021C04B03C02D`, `P00-PWA-AUTH-WP-021C04B03C02E`, `P00-PWA-AUTH-WP-021C04B03C02F`.
- Exercise the shared-reference prototype with real synthetic WordPress and independent SQL: conflicting owners across login/email/mobile, metadata value and user reassignment, duplicate meta rows, missing or invalid users, unsupported values, rollback, simultaneous add/update/delete and safe user deletion. Verify that a claim is not released while any source remains, transferred to the wrong owner or left inconsistent after failure. Capture both successful and rejected/deadlocked outcomes and resulting ownership.
- Account explicitly for WP return values, callbacks and side effects without claiming database rollback reverses external actions. Test only synthetic exact-binary values; do not treat `billing_phone` as a login, claim OTP replay protection, activate a route or migrate a real site.
- Evidence: `docs/evidence/P00-PWA-AUTH-WP-021C04B03C02/`. If the model cannot meet the original C acceptance or its coverage exceeds M, record the blocker rather than asserting C is complete.

## Blocker record — 2026-10-18

- ماتریس محدود WordPress واقعی و SQL مستقل، شامل تعارض‌ها، انتقال metadata، شکست، timeout و deadlock اجرا شد؛ [شاهد و محدودیت‌ها](../evidence/P00-PWA-AUTH-WP-021C04B03C02/concurrency-and-failure.md).
- با failure injection محدود روی حذف metadata، `wp_delete_user` در سه نسخه `true` برگرداند و callbackهای حذف را اجرا کرد، درحالی‌که کاربر و موبایلش باقی ماندند و بخشی از metadata قبلاً حذف شده بود. claim در این سناریو حفظ شد، ولی حذف امن و گزارش نتیجه تأیید نشد.
- پیش از بازآزمایی این کارت، طراحی جداگانه lifecycle/خطا و بررسی اثر callbackها لازم است؛ نه والد C و نه ورود/ثبت‌نام واقعی تکمیل محسوب نمی‌شوند. تغییری در سایت یا schema واقعی داده نشد.

## Reassessment queued — 2026-10-18

- `021C04B03C02A` has completed a [strictly synthetic controlled-path experiment](../evidence/P00-PWA-AUTH-WP-021C04B03C02A/managed-deletion.md). Native deletion still reports the injected failure incorrectly. This card is READY **only for a fresh assessment and its full acceptance rerun**, not DONE; the controlled path is not a production fix or a replacement for WordPress deletion.

## Reassessment result — 2026-10-18

- The original full three-version matrix was rerun after C02A. The same native partial-deletion/false-success behavior persisted on every version. See the [reassessment evidence](../evidence/P00-PWA-AUTH-WP-021C04B03C02/concurrency-and-failure.md). The synthetic controlled path bypasses normal hooks and cannot satisfy native compatibility, so this card remains BLOCKED.
- First assess an alternative supported-writer and fail-closed identity design in `021C04B03C02B`; do not advance to migration or represent this as a fix.

## Owner decision — 2026-10-18

- The owner selected normal WordPress deletion and plugin compatibility, with detection, warning and recovery for database failures, while acknowledging WordPress itself may still display a false success. Do not infer that irreversible effects can be undone or that duplicate identity safety was relaxed.
- `021C04B03C02C` starts by testing bounded detection in synthetic WordPress; recovery, complete lifecycle and product integration remain separate work. This decision alone does not unblock this card, authorize site changes or make browser registration ready.
- `021C04B03C02C` completed only bounded detection ([evidence](../evidence/P00-PWA-AUTH-WP-021C04B03C02C/native-deletion-observer.md)); `021C04B03C02D` is the next synthetic recovery experiment. C02 remains blocked.

## Synthetic recovery result — 2026-10-18

- `021C04B03C02D` demonstrated a narrowly bounded, conflict-aware recovery in disposable WordPress only; see [evidence](../evidence/P00-PWA-AUTH-WP-021C04B03C02D/synthetic-recovery.md). It does not resolve WP's false success, external side effects, durable snapshots or production authorization. `021C04B03C02E` is queued to examine redacted durable incident records; this parent remains BLOCKED.

## Synthetic incident follow-up — 2026-10-18

- `021C04B03C02E` demonstrated process-level readback of a redacted synthetic record but no account correlation, durable fallback or administrator warning; see [evidence](../evidence/P00-PWA-AUTH-WP-021C04B03C02E/synthetic-incident.md). `021C04B03C02F` is READY to test narrowly scoped synthetic account correlation and authorized access. A real alert, recovery, privacy/retention policy and the full parent acceptance remain outstanding.

## Synthetic correlation result — 2026-10-18

- The subsequently executed `021C04B03C02F` is now DONE for its limited synthetic account correlation and guarded access experiment; see [evidence](../evidence/P00-PWA-AUTH-WP-021C04B03C02F/incident-access.md). The earlier READY note above is historical. No production warning, durable fallback, privacy/retention policy or real recovery was implemented; this parent remains BLOCKED.

## Inactive incident storage — 2026-10-18

- `021C04B03C02G` implemented and tested a separate inactive storage primitive ([evidence](../evidence/P00-PWA-AUTH-WP-021C04B03C02G/verification.md)). It has no observer, route, migration, alert or recovery and does not guarantee persistence during a database outage. Privacy/retention, production authorization and all original acceptance remain open; this parent stays BLOCKED.
- `021C04B03C02H` added an inactive observer, tested only by attaching fixture hooks ([evidence](../evidence/P00-PWA-AUTH-WP-021C04B03C02H/verification.md)). Native false success, incomplete detection, durable fallback, warning, recovery and the full acceptance remain unresolved.
