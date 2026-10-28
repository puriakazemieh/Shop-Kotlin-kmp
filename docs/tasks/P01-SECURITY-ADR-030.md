# P01-SECURITY-ADR-030 — سیاست ماندگاری امن نشست Native

- Status: DONE
- Phase/Area/Type: P01 / SECURITY / ADR
- Priority/Risk/Size: P0/HIGH / S
- Owner: BOTH
- Depends on: P01-SECURITY-CODE-005D

## تصمیم

نشست Native پس از restart عادی باید حفظ شود، اما فقط زمانی که refresh token در vault امن سیستم‌عامل نگهداری شود. storage معمولی مانند `PreferencesSettings`، فایل‌ها یا logها هرگز جایگزین vault نیستند. تا آماده‌شدن vault Desktop، fallback درون‌حافظه‌ای فعلی fail-closed باقی می‌ماند.

## تفکیک اجرا

- Android: `P11-ANDROID-SEC-007` — حفظ نشست با Android Keystore و `EncryptedSharedPreferences`، به‌همراه migration،backup،restart،logout و tenant-switch.
- Desktop: `P17-DESKTOP-SEC-005` — vault بومی Windows/macOS/Linux، migration از fallback حافظه‌ای و آزمون restart/logout/expiry/revoke.
- iOS: `P16-IOS-SEC-006` — Keychain با سیاست accessibility/backup و آزمون lifecycle.

## معیار پذیرش

- restart عادی Native نباید logout اجباری ایجاد کند، مگر vault در دسترس نیست یا token نامعتبر/منقضی/revoked است.
- token در plaintext preferences،فایل،backup نامناسب یا log صفر باشد.
- logout،expiry،revoke و تغییر tenant/backend/origin اطلاعات نشست را پاک کنند.

## Evidence

- `docs/evidence/P01-SECURITY-ADR-030/decision-20261028.md`

## Completion record

- Completed at: 2026-10-28
- Final status: DONE (decision and platform-task routing; native vault implementations remain open in P11/P16/P17)
