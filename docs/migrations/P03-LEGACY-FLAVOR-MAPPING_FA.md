# نگاشت مهاجرت فاز ۳

این سند فقط inventory و قرارداد نگاشت است؛ هیچ package،کلید امضا یا توکن واقعی
در آن ثبت نشده است.

## package و flavor فعلی

| شناسه‌ی legacy | applicationId فعلی | وضعیت پس از migration |
|---|---|---|
| `carmila` | `com.kazemieh.shop` | حفظ کامل برای upgrade |
| `atris` | `com.kazemieh.shop.atris` | حفظ کامل برای upgrade |
| `chronos` | `com.kazemieh.shop.chronos` | حفظ کامل برای upgrade |
| `academy` | `com.kazemieh.shop.academy` | حفظ کامل برای upgrade |
| `psych` | `com.kazemieh.shop.psych` | حفظ کامل برای upgrade |
| `wp` | `com.kazemieh.shop.wp` | حفظ کامل برای upgrade |

مقادیر فعلی `versionName=1.0` و `versionCode=1` هستند و تا inventory امضای
فروشگاه/انتشار انسانی تغییر نمی‌کنند. کلید signing در repository نیست و باید
در release gate جداگانه تأیید شود.

## backend و tenant جدید

از این فاز به بعد backend فقط `WORDPRESS` یا `SPRING` است. شناسه‌ی مشتری/سایت
در `TenantConfig` قرار می‌گیرد و نباید با brand یا package قاطی شود. featureهای
shop/academy/clinic/psych از manifest می‌آیند، نه از flavor.

## token و اجرای یک‌باره

کلیدهای legacy کلاینت `access_token` و `refresh_token` هستند. namespace جدید
آن‌ها را با `backend + tenant + origin` جدا می‌کند؛ بنابراین runner افزونه
عمداً توکن کلاینت را نمی‌خواند یا کپی نمی‌کند. در تغییر tenant/backend، کلاینت
باید logout و purge namespace قبلی را انجام دهد؛ این مورد در QA/migration دستی
تأیید می‌شود.

`CB_Legacy_Migration` فقط Theme Modهای legacy را در اولین اجرا به
`cb_manifest_features` نگاشت می‌کند، marker نسخه‌ی ۱ می‌نویسد و اجرای دوم
هیچ تغییری نمی‌دهد. اگر manifest canonical وجود داشته باشد دست‌نخورده می‌ماند.
