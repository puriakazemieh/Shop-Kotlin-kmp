# P01-SECURITY-CODE-005 — انتقال کنترل‌های پلتفرمی

- تاریخ: ۲۰۲۶-۱۰-۲۸
- تصمیم‌گیر: مالک پروژه
- تصمیم: کنترل‌های token/session بر پایهٔ پلتفرم به Gateهای مقصد منتقل و کارت والد P01 بسته شد.

| محدوده | کارت مقصد | وضعیت واقعی |
|---|---|---|
| Android | `P11-ANDROID-SEC-007` | پیاده‌سازی Keystore موجود است؛ migration،backup و آزمون دستگاه هنوز Gate مقصد هستند. |
| iOS | `P16-IOS-SEC-006` | Keychain و آزمون دستگاه هنوز باز هستند. |
| Desktop | `P17-DESKTOP-SEC-005` | vault بومی و persistence امن هنوز باز هستند؛ fallback حافظه‌ای فعلی عمداً plaintext نیست. |
| Web/PWA و host binding | `P10-RELEASE-SEC-021` | آزمون HTTPS واقعی،cookie lifecycle و redirect/foreign-host هنوز باز هستند. |

این مدرک PASS امنیتی یا مجوز انتشار نیست و هیچ token یا دادهٔ واقعی ندارد.
