# Manual QA Evidence Handoff / تحویل شواهد تست دستی: P09-QA-MANUAL-003

- **Task ID / شناسه تسک:** P09-QA-MANUAL-003
- **Date / تاریخ:** 2026-09-06
- **Title / عنوان:** نصب تمیز، ارتقا و بازگشت (Rollback) روی ماتریس WP/PHP/Woo

## Test Instructions for User / دستورالعمل تست برای کاربر
### کجا نگاه کنید
- پیشخوان وردپرس بخش افزونه‌ها (`/wp-admin/plugins.php`) یا پوسته‌ها (`/wp-admin/themes.php`)
- تنظیمات ووکامرس بخش Advanced -> Features (وضعیت HPOS)
- لاگ‌های وردپرس (`wp-content/debug.log`)

### مراحل تست
1. **ماتریس نصب تمیز:** فایل ZIP افزونه/پوسته را روی وردپرس خام (نسخه‌های ۶.۴ تا ۶.۶) با PHP 8.1 تا 8.3 نصب و فعال کنید.
2. **تست ارتقا:** نسخه قبلی را نصب کرده، چند سفارش بسازید، سپس نسخه جدید را آپلود کرده و صحت اجرای migrationها را تایید کنید.
3. **تست بازگشت:** نسخه را دانگرید کنید و عدم تخریب پایگاه داده را تایید نمایید.
4. **سازگاری HPOS و بلوک‌ها:** حالت HPOS و بلوک‌های تسویه‌حساب گوتنبرگ را روشن کرده و تست کنید.

## Status / وضعیت
- **Final Status / وضعیت نهایی:** AWAITING_MANUAL_QA (در انتظار تست دستی)

## اجرای قابل‌بازگشت در محیط QA — 2026-10-14

### محیط

- نصب مقصد پایدار: `D:\Carmilla-QA\p07-wordpress-destination`
- WordPress `7.1.3`، PHP `8.2.12`
- حالت اولیه و نهایی: قالب Carmilla فعال و افزونهٔ Carmilla Bridge غیرفعال

### نتیجهٔ اجراشده

- ZIPهای `carmilla-bridge.zip` و `carmilla-theme.zip` با شمارش 109 و 141 فایل
  به‌ترتیب، خوانده شدند و ساختار ZIP معتبر بود.
- lint فایل bootstrap افزونه و `functions.php` قالب در نصب مقصد موفق بود.
- افزونه از وضعیت غیرفعال فعال شد؛ در یک boot تازه، namespace `carmilla/v1`
  ثبت و 118 route آن قابل مشاهده بود.
- smoke واقعی migration فاز 07 با دادهٔ synthetic پاس شد: درج، mapping، restore
  parent، rewrite URL، حذف metadata حساس و idempotent re-run همگی PASS بودند.
- در پایان افزونه دوباره غیرفعال شد؛ دادهٔ واقعی یا پرداخت ایجاد نشد.

### محدودیت باقی‌مانده

این فقط یک اجرای بازگشت‌پذیر روی یک ترکیب است و معیار `minimum/current/mid +
HPOS/Blocks` را پوشش نمی‌دهد. در نصب اصلی QA، WooCommerce `11.2.1` با HPOS
روشن و بستهٔ Blocks در دسترس است؛ ولی اجرای clean-install/upgrade/rollback روی
چند نسخهٔ پشتیبانی‌شدهٔ WordPress/PHP/WooCommerce همچنان تأیید انسانی/محیط جدا
می‌خواهد. وضعیت کارت عمداً `AWAITING_MANUAL_QA` باقی می‌ماند.

## حداقل PHP با Docker — 2026-10-14

- Docker Engine `29.2.1` در دسترس بود.
- `docker compose -f tools/test-env/docker-compose.yml run --rm php-test php -v`
  روی PHP `8.1.34` با exit code `0` اجرا شد.
- تمام فایل‌های PHP در `wordpress/carmilla-bridge` و
  `wordpress/carmilla-theme` با همان image و `php -l` بررسی شدند:
  `PHP_LINT_PASS`، exit code `0`.

این نتیجه فقط baseline سازگاریِ syntax در minimum PHP است و جایگزین clean
install/upgrade/rollback واقعی WordPress/WooCommerce یا تأیید انسانی نیست.

## ماتریس clean-install پایدار Docker — 2026-10-14

محیط‌های جداگانه و persistent خارج از سورس در
`D:\Carmilla-QA\p09-wordpress-matrix` ایجاد شدند؛ volumeهای Docker و
containerها برای بازآزمایی‌های بعدی نگه داشته می‌شوند.

| ردیف | URL | WordPress | PHP | نصب ZIP Theme/Bridge | نتیجه |
|---|---|---:|---:|---|---|
| minimum | `http://localhost:8081/` | 6.4.3 | 8.1.27 | هر دو فعال | PASS |
| mid | `http://localhost:8082/` | 6.6.2 | 8.2.25 | هر دو فعال | PASS |
| current | `http://localhost:8083/` | 7.1.3 | 8.3.35 | هر دو فعال | PASS |

همهٔ ردیف‌ها clean install هستند و `GET /wp-json/` پاسخ `200` داده است. دادهٔ
مدیر هر محیط synthetic و فقط محلی است.

### current: WooCommerce / HPOS / Blocks

- WooCommerce `11.2.1` نصب و فعال شد.
- گزینه‌های `woocommerce_custom_orders_table_enabled` و
  `woocommerce_custom_orders_table_data_sync_enabled` به `yes` تنظیم شدند.
- Store API بلوک‌ها (`GET /wp-json/wc/store/v1/cart`) پاسخ `200` داد.
- route واقعی Bridge با `wp eval` در REST server حاضر بود
  (`CARMILLA_ROUTE_PRESENT`).

### موارد باز

این evidence، clean-install سه‌ردیفی و HPOS/Blocks current را می‌بندد، اما
برای تکمیل کامل کارت هنوز migration از **artifact نسخهٔ قبلی** به artifact
فعلی، rollback نسخه‌ای و تأیید انسانی UI/admin لازم است. تا آن زمان وضعیت
`AWAITING_MANUAL_QA` بدون تغییر می‌ماند.

## upgrade / rollback Bridge و compatibility Woo — 2026-10-14

- در ردیف mid، artifact قبلی موجود (`carmilla-bridge-wordpress.zip`، نسخهٔ
  `0.7.3`) نصب شد، سپس به artifact فعلی (`carmilla-bridge.zip`، نسخهٔ `1.0.0`)
  ارتقا یافت، به `0.7.3` rollback شد و در پایان با forward-fix دوباره به
  `1.0.0` بازگشت. هر چهار نصب/activation از طریق WP-CLI موفق بودند.
- WooCommerce `11.2.1` در current با WP `7.1.3` فعال و HPOS/Blocks آن آزموده
  شد، اما این نسخه روی WP `6.4.3` و `6.6.2` فعال نمی‌شود زیرا حداقل WordPress
  موردنیاز آن `7.0` است. این یک compatibility blocker برای ادعای «یک نسخهٔ Woo
  روی همهٔ ردیف‌ها» است، نه failure کد Bridge/Theme.
- تلاش برای دریافت WooCommerce `8.3.1` (نسخهٔ سازگار برای ردیف‌های قدیمی‌تر)
  در Docker timeout شد؛ فرآیند بدون حذف volume/container متوقف شد. ردیف‌های
  minimum/mid همچنان Theme/Bridge `1.0.0` فعال دارند؛ Woo `11.2.1` فقط نصب و
  غیرفعال است.

### تصمیم لازم پیش از PASS کارت

برای بستن کامل ماتریس، باید artifact تأییدشدهٔ WooCommerce سازگار با WP 6.4 و
6.6 در دسترس باشد یا حداقل پشتیبانی رسمی Bridge از این دو ترکیب بازتعریف شود.
پس از تأمین artifact، فعال‌سازی Woo، Store API/Blocks و HPOS همان دو ردیف باید
دوباره اجرا شود. این blocker، Gate P09 را باز نگه می‌دارد.

## تصحیح compatibility Woo و اجرای واقعی REST — 2026-10-14

بخش «artifact سازگار در دسترس نیست» در رکورد قبلی با اجرای زیر جایگزین شده
است؛ متن قبلی برای تاریخچه نگه داشته شده است.

- artifact محلی و پایدار WooCommerce `8.3.1` برای ردیف‌های minimum و mid نصب و
  فعال شد. در هر دو ردیف HPOS (`woocommerce_custom_orders_table_enabled`) برابر
  `yes` است.
- هر سه ردیف اکنون Theme `0.8.0`، Bridge `1.0.0` و یک WooCommerce سازگار و فعال
  دارند: minimum (`WP 6.4.3 / PHP 8.1.27 / Woo 8.3.1`)، mid (`6.6.2 / 8.2.25 /
  8.3.1`) و current (`7.1.3 / 8.3.35 / 11.2.1`).
- با REST fallback استاندارد WordPress (`?rest_route=`)، هر سه endpoint
  `carmilla/v1/client-manifest`، `carmilla/v1/api/products` و
  `wc/store/v1/cart` در هر سه ردیف پاسخ `200` با `Content-Type: application/json`
  دادند. بنابراین هم Bridge و هم Blocks Store API واقعاً در ماتریس پاسخ‌گو هستند.
- مسیر prettyِ `/wp-json/...` در image پیش‌فرض Docker برای این نصب‌های تازه به
  قالب HTML redirect می‌شود، در حالی‌که fallback استاندارد WordPress JSON درست
  می‌دهد. این تنظیم محیط Docker است، نه failure routeهای Bridge؛ محیط QA XAMPP که
  PWA علیه آن آزموده شد از مسیر pretty استفاده می‌کند. این نکته برای بازتولید
  ماتریس ثبت می‌شود و مانع acceptance کارت نیست.

### وضعیت باقی‌مانده

upgrade → rollback → forward-fix Bridge و matrix خودکار اکنون PASS هستند. کارت
همچنان `AWAITING_MANUAL_QA` می‌ماند، چون تنها موارد بازِ این کارت تأیید انسانی
UI/admin (فعال‌سازی و مشاهدهٔ HPOS/Blocks از پیشخوان) و کنترل عدم خطای قابل‌مشاهده
در آن سه محیط است؛ هیچ blocker خودکار باقی نمانده است.

## اصلاح template صفحهٔ Cart در ماتریس — 2026-10-14

- بازبینی UI روی ردیف minimum نشان داد Cart badge مقدار `1` را دارد، ولی صفحهٔ
  Cart فقط عنوان صفحه را به شکل card بلاگ نشان می‌دهد. علت: قالب `page.php` نداشت
  و WordPress به `index.php` fallback می‌کرد؛ در نتیجه محتوای Cart Block اجرا
  نمی‌شد.
- `wordpress/carmilla-theme/page.php` اضافه شد تا محتوای صفحه‌های ثابت، از جمله
  Cart/Checkout/My Account، با `the_content()` رندر شود.
- lint فایل با PHP محلی PASS شد؛ `carmilla-theme.zip` دوباره ساخته و با
  `wp theme install --force --activate` روی هر سه ردیف matrix نصب شد.
- درخواست `?page_id=6` پس از نصب، markup مربوط به `wp-block-woocommerce-cart`
  را در هر سه محیط برگرداند؛ بنابراین fallback بلاگ حذف شده است. نمایش و تعامل
  نهایی Cart Block همچنان یک تأیید انسانی مرورگر می‌خواهد.

## بومی‌سازی Cart/Checkout Blocks در ماتریس — 2026-10-14

- locale اصلی و translation package ووکامرس در همهٔ ردیف‌ها به `fa_IR` تغییر کرد.
  HTML هر سه سایت اکنون `lang="fa-IR"` دارد و بسته‌های ترجمهٔ Cart/Checkout
  Blocks بارگذاری می‌شوند.
- currency و کشور پیش‌فرض محیط synthetic به‌ترتیب `IRR` و `IR` تنظیم شدند؛ محصول
  تست نیز به «محصول آزمایشی کارمیلا» تغییر نام یافت.
- برای جلوگیری از override شدن فونت/جهت توسط React Blocks ووکامرس، قالب به‌صورت
  صریح `Vazirmatn` و RTL را برای Cart/Checkout Blocks اعمال می‌کند. ZIP قالب
  بازسازی و روی هر سه ردیف matrix نصب شد.
- نبود payment method در Checkout یک failure نیست: هیچ درگاه پرداخت واقعی یا
  آزمایشی در محیط QA پیکربندی نشده و این کارت مجاز به پیکربندی پرداخت نیست.

## URLهای خوانا و session-aware Checkout در ماتریس — 2026-10-14

- permalink structure هر سه سایت به `/%postname%/` تنظیم شد؛ URLهای قابل استفاده
  اکنون `/shop/`، `/cart/`، `/checkout/` و `/my-account/` هستند و دیگر برای کاربر
  به `?page_id=` وابسته نیستند.
- در هر سه ردیف، یک session synthetic دارای محصول ساخته و مسیر واقعی
  `/cart/` → `/checkout/` با HTTP `200` آزموده شد.
- اگر Checkout با session خالی باز شود، WooCommerce عمداً آن را به Cart هدایت
  می‌کند؛ این رفتار استاندارد است و failure محسوب نمی‌شود.
