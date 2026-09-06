<div dir="rtl" align="right">

# گزارش P03-MANIFEST-OPS-021: موجودی و Deprecation برای Aliasهای Legacy

با توجه به درخواست مستندسازی و حفظ aliasهای موجود با telemetry و deprecation برای یک چرخه کلاینت، موارد زیر شناسایی و اعمال شد:

## جدول موجودی Aliasها

| Alias | Canonical (مسیر/متد اصلی) | Consumer (مصرف‌کننده) | زمان خروج (Sunset) |
|---|---|---|---|
| `/api/users/me` | `/wp-json/carmilla/v1/api/users/me` | کلاینت‌های قدیمی، بیلد‌های Web/Desktop | چرخه بعدی کلاینت |
| `/api/addresses` | `/wp-json/carmilla/v1/api/addresses` | کلاینت‌های قدیمی | چرخه بعدی کلاینت |
| `/api/favorites` | `/wp-json/carmilla/v1/api/favorites` | کلاینت‌های قدیمی | چرخه بعدی کلاینت |
| `/api/recently-viewed` | `/wp-json/carmilla/v1/api/recently-viewed` | کلاینت‌های قدیمی | چرخه بعدی کلاینت |
| `BootstrapProfiles.fromLegacyApiRoot` | `BootstrapProfiles.spring` / `wordpress` | کدهای قدیمی توسعه/بیلد | چرخه بعدی کلاینت |

## تغییرات اعمال‌شده (Telemetry و Deprecation)

- **PHP (WordPress Bridge):**
  در فایل `class-cb-plugin.php` و درون متد `maybe_root_alias`، کدهای زیر اضافه شد تا استفاده از مسیرهای alias ثبت (log) شود و هدرهای deprecation به کلاینت ارسال گردد. این مسیر کماکان از طریق `rest_do_request` پردازش می‌شود که به این معناست که محدودیت‌ها و guardهای فعلی (از جمله `guard_rest_request`) همچنان اعمال می‌شوند و bypass امکان‌پذیر نیست.
  ```php
  error_log( 'Carmilla Bridge Legacy Alias Usage: ' . $uri );
  header( 'X-Carmilla-Deprecated: root-alias' );
  header( 'Warning: 299 - "Legacy root alias is deprecated and will be removed in the next client cycle"' );
  ```

- **Kotlin:**
  در فایل `UrlResolvers.kt`، متد `fromLegacyApiRoot` از قبل با انوتیشن `@Deprecated` مشخص شده بود و تغییری در آن اعمال نگردید، زیرا قرارداد deprecation را رعایت می‌کند.

## Regression و عدم Bypass
از آنجا که تغییرات صرفا در سطح صدور هدر و لاگ (telemetry) بوده و مسیر جریان درخواست‌ها همچنان از `rest_do_request` عبور می‌کند، هیچ قابلیتی بالاتر از policy فعلی باز نمی‌شود و bypass رخ نمی‌دهد (سازگاری با P03-QA-MANUAL-027).

</div>
