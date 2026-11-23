# Evidence: P04-WPPLUGIN-CODE-008 — قرارداد و adapter رسمی WooCommerce در هسته مشترک

## مشخصات اجرا
- **Task ID:** `P04-WPPLUGIN-CODE-008`
- **عنوان:** قرارداد و adapter رسمی WooCommerce در هسته مشترک (WooCommerce Adapter, CRUD Contracts, HPOS Parity)
- **فاز/حوزه/نوع:** `P04 / WPPLUGIN / CODE`
- **تاریخ:** ۲۳ نوامبر ۲۰۲۶ (2026-11-23)
- **محیط تست:** وردپرس واقعی (PHP 8.3.35, MariaDB 10.6.28, Apache) در کانتینر `test-env-wordpress-test-1`
- **وضعیت ووکامرس:** نسخه 9.x فعال؛ پشتیبانی رسمی از HPOS (Custom Order Tables) و Legacy Posts.

---

## ۱. اهداف و معیارهای پذیرش (Acceptance Criteria)
- [x] **Woo پیش‌نیاز مجاز و منبع رسمی commerce باقی مانده است:** تایید شد که ووکامرس منبع اصلی کاتالوگ محصولات، سبد خرید و سفارش‌هاست و هیچ موتور فروشگاه موازی یا غیراستانداردی ایجاد نشده است.
- [x] **adapter و storage cart قرارداد روشن دارند:** کلاس `WooCommerceAdapter` قرارداد کامل عملیات محصولات (`create_product`, `get_product`, `get_products`, `delete_product`)، سفارشات (`create_order`, `get_order`, `get_orders`, `delete_order`) و سبد خرید (`get_cart`, `get_cart_items`, `clear_cart`, `get_cart_storage_driver`) را بدون استفاده از هرگونه SQL مستقیم (`$wpdb`) تعریف و پیاده‌سازی کرد.
- [x] **fixture HPOS روشن/خاموش در هر دو host ثبت شده است:** تست‌های خواندن/نوشتن سفارش تحت دو حالت فعال بودن HPOS (`STORAGE_HPOS`) و غیرفعال بودن HPOS (`STORAGE_LEGACY`) با رفتار کاملاً یکسان روی هر دو میزبان (`carmilla-theme` و `twentytwentyfour`/Plugin) با موفقیت اجرا و اثبات شدند.
- [x] **مهاجرت کامل دامنه و موتور commerce جدید در این کارت انجام نشده است:** تایید شد که این کارت در محدوده خود باقی مانده و مهاجرت کامل به فازهای اختصاصی P05/P06 و کارت‌های 026A/026B واگذار شده است.

---

## ۲. تغییرات اعمال‌شده در کدها
1. **توسعه و تثبیت آداپتر رسمی ووکامرس (`WooCommerceAdapter.php`):**
   - افزودن متدهای رسمی CRUD محصول: `create_product()`, `get_product()`, `get_products()`, `delete_product()` بر پایه کلاس استاندارد `\WC_Product_Simple` و متدهای شیءمحور ووکامرس.
   - افزودن متدهای رسمی CRUD سفارش: `create_order()`, `get_order()`, `get_orders()`, `delete_order()` بر پایه تابع رسمی `wc_create_order()` و `wc_get_order()`.
   - مدیریت و تشخیص پویای مخزن ذخیره‌سازی سفارش: `is_hpos_enabled()`, `get_order_storage_type()`, `set_hpos_enabled()`.
   - مدیریت قرارداد سبد خرید و درایور نشست: `get_cart_items()`, `clear_cart()`, `get_cart_storage_driver()`.
   - محافظت در برابر بازتعریف با شرط `if ( ! class_exists( ... , false ) )`.
2. **تثبیت محافظ سازگاری HPOS (`WooCompat.php`):**
   - افزودن محافظ `if ( ! class_exists( ... , false ) )` جهت جلوگیری از تداخل بارگذاری در محیط‌های دوگانه میزبان.
   - اعلام سازگاری صریح با `custom_order_tables` و `cart_checkout_blocks` از طریق `Automattic\WooCommerce\Utilities\FeaturesUtil`.

---

## ۳. نتایج آزمون‌های واقعی در کانتینر وردپرس

### ۳.۱. اجرای سوئیت اختصاصی آداپتر ووکامرس (`test-woocommerce-adapter-008.php`):
دستور:
```bash
docker exec test-env-wordpress-test-1 php /var/www/html/wp-content/themes/carmilla-theme/packages/carmilla-core/tests/test-woocommerce-adapter-008.php
```
خروجی:
```text
====================================================================
  P04-WPPLUGIN-CODE-008: WooCommerce Adapter & Contract QA          
====================================================================

Group 1: WooCommerce Availability & Compatibility Declarations
  [PASS] WooCommerceAdapter detects WooCommerce is available
  [PASS] WooCommerce core class is loaded
  [PASS] WooCompat class exists in shared core
  [PASS] Automattic\WooCommerce\Utilities\FeaturesUtil exists for HPOS compatibility declaration

Group 2: Official Product CRUD Operations
  [PASS] Product created via official WC CRUD
  [PASS] Created product has valid positive ID (100292)
  [PASS] Product retrieved via WooCommerceAdapter::get_product
  [PASS] Product name matches retrieved
  [PASS] Product regular price matches 380,000
  [PASS] Product found in get_products collection
  [PASS] Product deleted permanently via WooCommerceAdapter::delete_product
  [PASS] Product ID 100292 is null after deletion

Group 3: Order CRUD with HPOS Enabled
  [PASS] HPOS successfully enabled
  [PASS] Storage type reports 'hpos'
  [PASS] Order created under HPOS via official CRUD
  [PASS] HPOS Order has valid positive ID (100293)
  [PASS] HPOS Order retrieved via get_order
  [PASS] HPOS Order status matches 'pending'
  [PASS] HPOS Order status updated to 'processing'
  [PASS] HPOS Order deleted via official CRUD
  [PASS] HPOS Order is null after deletion

Group 4: Order CRUD with HPOS Disabled (Legacy Posts Storage)
  [PASS] HPOS successfully disabled (legacy storage active)
  [PASS] Storage type reports 'legacy_posts'
  [PASS] Order created under legacy storage via official CRUD
  [PASS] Legacy Order has valid positive ID (100294)
  [PASS] Legacy Order retrieved via get_order
  [PASS] Legacy Order status matches 'on-hold'
  [PASS] Legacy Order status updated to 'completed'
  [PASS] Legacy Order deleted via official CRUD
  [PASS] Legacy Order is null after deletion
  [PASS] Restored original HPOS setting (yes)

Group 5: Parity Across Both Hosts
  [PASS] Host 1 active: carmilla-theme
  [PASS] Host 1: Product created successfully
  [PASS] Host 1: Order created successfully
  [PASS] Host 2 active: twentytwentyfour
  [PASS] Host 2: Product created successfully under default theme
  [PASS] Host 2: Order created successfully under default theme
  [PASS] Restored theme to carmilla-theme

Group 6: Cart Contract & Driver
  [PASS] Cart driver is recognized (wc_session)
  [PASS] WooCommerceAdapter::get_cart_items returns array
  [PASS] Cart clear operates safely

Group 7: Scope Boundary Verification
  [PASS] WooCommerce remains the authoritative source of truth for commerce products/orders
  [PASS] Zero raw SQL queries utilized; all order/product operations use official WC CRUD
  [PASS] HPOS enabled/disabled parity verified with identical return types
  [PASS] Full domain migration and new shop engine are strictly excluded from this task

--------------------------------------------------------------------
Results: Total: 45 | Passed: 45 | Failed: 0
--------------------------------------------------------------------
```

### ۳.۲. بررسی رگرسیون و پایداری سایر بخش‌ها:
- `test-plugin-host-standalone-007.php`: **۵۳ از ۵۳ تست پاس شد (۰ خطا).**
- `verify-all-phase04-real-wp.php`: **۷۸ از ۷۸ تست جامع پاس شد (۰ خطا).**
- `test-entitlement-resolver-040.php`: **۶۴ از ۶۴ تست لایسنس و روت پاس شد (۰ خطا).**

---

## ۴. نتیجه‌گیری نهایی
آداپتر رسمی و قرارداد WooCommerce به طور کامل در هسته مشترک پیاده‌سازی و اعتبارسنجی شد.
- **وضعیت:** `DONE`
