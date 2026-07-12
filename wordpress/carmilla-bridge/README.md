# Carmilla Bridge (WordPress plugin)

پلی که محتوای **WordPress + WooCommerce** را با همان قرارداد API که اپ Carmilla (KMP) از قبل می‌شناسد در اختیار اپ می‌گذارد. اپ می‌تواند محصول و مقاله را **بخواند و مستقیم روی وردپرس مدیریت کند** (افزودن/ویرایش/حذف)، و احراز هویت با JWT انجام می‌شود.

این فاز ۱ از پلن مهاجرت به وردپرس است (منبع اصلی داده = وردپرس). هدف طراحی: اپ فقط **baseUrl** را عوض کند و بقیه‌ی مسیرها و شکل JSON ثابت بماند.

## نصب
1. پوشه‌ی `carmilla-bridge` را در `wp-content/plugins/` کپی کنید (یا zip کرده و از داشبورد آپلود کنید).
2. افزونه را فعال کنید (روی activation، CPTها ثبت و rewrite ها flush می‌شوند).
3. **WooCommerce** را نصب/فعال کنید (برای بخش فروشگاه لازم است).
4. در `wp-config.php` یک کلید امن تعریف کنید:
   ```php
   define( 'CB_JWT_SECRET', 'یک-رشته-تصادفی-طولانی' );
   ```
   اگر تعریف نشود از `AUTH_KEY` وردپرس استفاده می‌شود.
5. اگر سرور Apache است و هدر Authorization حذف می‌شود، در `.htaccess`:
   ```
   SetEnvIf Authorization "(.*)" HTTP_AUTHORIZATION=$1
   ```

## اتصال اپ
`baseUrl` اپ را به این آدرس تنظیم کنید (فایل `core/network/.../common/PlatformConfig.*.kt`):
```
https://<your-site>/wp-json/carmilla/v1/
```
اپ همچنان مسیرهای نسبی مثل `api/products` و `api/blogs` را صدا می‌زند که به `/wp-json/carmilla/v1/api/...` نگاشت می‌شوند.

## نقش‌ها
- `administrator` و `shop_manager` → نقش `ADMIN` (اجازه‌ی نوشتن).
- بقیه → `CUSTOMER`.
- احراز هویت با هدر `Authorization: Bearer <accessToken>`.

## Endpointها (namespace: `carmilla/v1`)

### Auth
| متد | مسیر | بدنه/خروجی |
|---|---|---|
| POST | `api/auth/login` | `{username,password}` → AuthResponse |
| POST | `api/auth/register` | `{email?,mobile?,password}` → AuthResponse |
| POST | `api/auth/refresh` | `{refreshToken}` → `{accessToken,refreshToken}` |
| GET | `api/users/me` | (Bearer) → UserResponse |

### فروشگاه (WooCommerce)
| متد | مسیر |
|---|---|
| GET | `api/categories` |
| GET | `api/products` (`query,categoryId,minPrice,maxPrice,inStock,sort,page,size`) |
| GET | `api/products/{slug}` |
| GET | `api/campaigns/active` |
| GET | `api/banners` |
| GET/POST | `api/admin/products` (ادمین) |
| GET/PATCH/DELETE | `api/admin/products/{id}` (ادمین) |

### مقاله (پست وردپرس)
| متد | مسیر |
|---|---|
| GET | `api/blogs` (`search,categoryId,page,size`) |
| GET | `api/blogs/featured` |
| GET | `api/blogs/{slug}` |
| GET | `api/blogs/{slug}/related` |
| GET | `api/blogs/categories` |
| GET/POST | `api/admin/blogs` (ادمین) |
| GET | `api/admin/blogs/{slug}` (ادمین) |
| PUT/DELETE | `api/admin/blogs/{id}` (ادمین) |
| POST | `api/admin/blogs/media/upload` (multipart `file`) → `{url}` |
| POST/PUT/DELETE | `api/admin/blogs/categories[/{id}]` (ادمین) |

## نگاشت بدنه‌ی مقاله
بدنه‌ی مقاله بین بلاک‌های **Gutenberg** و آرایه‌ی `BlogBlockDto` اپ نگاشت می‌شود
(`header, paragraph, image, button, list, quote, divider`) — کلاس `CB_Blocks`.

## آزمون
- `php -l` روی همه‌ی فایل‌ها بدون خطا.
- تست دود (round-trip توکن JWT + امضای دستکاری‌شده + نگاشت بلاک→HTML) در `tests/` قابل اجراست:
  ```
  php tests/smoke.php
  ```

## محدودیت‌های این نسخه (۰.۱.۰)
- ساخت/ویرایش محصول از اپ فعلاً **محصول ساده (simple)** را پشتیبانی می‌کند؛ مدیریت variation/option در افزایش بعدی.
- بخش تجارت (سبد/تسویه/سفارش/کیف پول/پرداخت ZarinPal) در این فاز نیست؛ در فاز ۴ با WooCommerce Store API و درگاه ZarinPal افزوده می‌شود.
- استوری/بنر/کمپین به‌صورت CPT ثبت می‌شوند؛ endpoint خواندن بنر و کمپین فعال آماده است.
