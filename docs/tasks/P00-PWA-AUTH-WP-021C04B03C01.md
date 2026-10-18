# P00-PWA-AUTH-WP-021C04B03C01 — synthetic shared identity reference model

- Status: DONE; Size: M; Owner: AI; Risk: HIGH
- Depends on: `P00-PWA-AUTH-WP-021C04B03B`; scope-review evidence: `docs/evidence/P00-PWA-AUTH-WP-021C04B03C/mobile-baseline.md`.
- Develop a **test-only**, disposable MariaDB/real-WordPress model of one exact-binary synthetic identity claim owned by one user with independently tracked references from user login, nonempty email and multiple `cb_mobile` rows. Do not infer that a reference is unused merely because one source was removed.
- Test sequential INSERT/UPDATE/DELETE with both real WordPress metadata APIs and direct SQL, duplicate same-owner rows, cross-namespace same-owner aliases, removing one versus the last reference, and keeping `billing_phone` outside identity claims. Explicitly reject or document unsupported fixture values; no production phone normalization or identity merging.
- Preserve existing user-row experiments and sites; no route, migration, live schema or OTP changes. Record redacted evidence in `docs/evidence/P00-PWA-AUTH-WP-021C04B03C01/`. A passing sequential fixture does not establish safe concurrency, reassignment or production behavior; those belong to C02 and the composite gate.

## Completion record — 2026-10-18

- مدل ترتیبی صرفاً آزمایشی با claim واحد و مراجع مستقل برای login، email و ردیف‌های متعدد `cb_mobile` ساخته شد؛ `billing_phone` خارج از هویت ماند.
- آزمایش WordPress واقعی و SQL مستقیم، شامل مراجع تکراری، aliases، حذف تدریجی، rollback و رد ناسازگاری، روی سه نسخه وردپرس/PHP و MariaDB موقت پاس شد؛ محدودیت‌ها و جزئیات در [Evidence](../evidence/P00-PWA-AUTH-WP-021C04B03C01/shared-reference-experiment.md) ثبت شده‌اند.
- هیچ سایت نگه‌داری‌شده یا کد اجرایی محصول تغییر نکرد. هم‌زمانی، تغییر مالک و رفتار شکست و side effectها به C02 واگذار شده‌اند؛ این کارت به معنی رفع ورود یا ثبت‌نام نیست.
