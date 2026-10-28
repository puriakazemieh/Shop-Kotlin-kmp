# P01-PAYMENT-CODE-007 — انتقال کنترل نتیجهٔ پرداخت

- تاریخ: ۲۰۲۶-۱۰-۲۸
- تصمیم‌گیر: مالک پروژه
- تصمیم: کنترل deep link پرداخت به کارت‌های پلتفرم و backend منتقل شد.

| محدوده | کارت مقصد | کنترل لازم |
|---|---|---|
| Android | `P11-ANDROID-CODE-006` | App Link opaque و query server قبل از تغییر UI/cart. |
| iOS | `P16-IOS-CODE-007` | Universal Link opaque و query server قبل از تغییر UI/cart. |
| Desktop | `P17-DESKTOP-CODE-006` | callback opaque و query server قبل از تغییر UI/cart. |
| Web/PWA | `P08-PWA-SEC-020` | URL موفق جعلی بی‌اثر؛ browser lifecycle. |
| Payment backend | `P05-PAYMENT-CODE-006/007` | result session opaque و server-to-server verify. |

این انتقال PASS پرداخت یا مجوز تولید نیست؛ تا Evidence هر کارت، URL/deep link منبع trusted نتیجه محسوب نمی‌شود.
