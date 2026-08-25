# P16-IOS-SEC-023 — بازبینی Keychain پیش از انتشار iOS

- Status: TODO
- Owner: HUMAN
- Depends on: P16-IOS-OPS-021
- Blocks: P16-IOS-GATE-022

## Goal

پیش از ایجاد خروجی انتشار iOS، نگهداری token در iOS Keychain با Xcode/macOS مستقل از پیاده‌سازی KMP بازبینی شود.

## Required verification

1. در نسخهٔ Release روی دستگاه واقعی یا Simulator، login انجام دهید و برنامه را کامل ببندید و باز کنید؛ session فقط طبق policy مورد انتظار باقی بماند.
2. logout کنید؛ سپس برنامه را باز کنید و مطمئن شوید token/session قبلی قابل استفاده نیست.
3. با Xcode یا ابزار مناسب بررسی کنید token در `NSUserDefaults`، log یا فایل‌های قابل خواندن برنامه ثبت نشده باشد و فقط Keychain استفاده شود.
4. نام سرویس/اکانت Keychain و سطح دسترسی آن با شناسهٔ release app سازگار باشد؛ هیچ secret یا token واقعی را در Evidence ذخیره نکنید.

## Success criteria

- token فقط در Keychain نگهداری می‌شود.
- logout و refresh/expiry token قبلی را بی‌اعتبار می‌کند.
- Evidence قرمز‌شده، build fingerprint و نتیجهٔ تست ثبت شده است.
