# P01-SECURITY-CODE-005B — iOS Keychain token storage

- Status: DONE
- وضعیت ممیزی ۲۰۲۶-۱۰-۲۸: برخلاف Evidence، `TokenSettingsFactory.ios.kt` از `NSUserDefaultsSettings` استفاده می‌کند، نه Keychain؛ بنابراین token persistence امن نیست. مالک در ۲۰۲۶-۱۰-۲۸ تصمیم گرفت این کنترل صرفاً در Gate اختصاصی iOS اجرا و اثبات شود؛ این DONE به‌معنای پیاده‌سازی Keychain نیست.
- Owner: AI
- Depends on: P01-SECURITY-CODE-005A
- Goal: انتقال token از NSUserDefaults به Keychain، حذف token قدیمی و login دوباره.

## Completion record

- Completed at: 2026-10-28
- Disposition: مالک پروژه تأیید کرد که پیگیری و پذیرش این کنترل از P01 به `P16-IOS-SEC-006` منتقل شود. این کارت فقط انتقال برنامه‌ریزی P01 را می‌بندد و هیچ ادعای پیاده‌سازی یا آزمون Keychain ندارد.
- Evidence: `docs/evidence/P01-SECURITY-CODE-005B/deferral-decision-20261028.md`
- Remaining control: `P16-IOS-SEC-006` باید migration از `NSUserDefaultsSettings`، سیاست Keychain accessibility/backup و آزمون‌های logout، expiry، reinstall و device backup را با دستگاه و build iOS واقعی انجام دهد.
- Final status: DONE (routing to P16 iOS Keychain owner card)
