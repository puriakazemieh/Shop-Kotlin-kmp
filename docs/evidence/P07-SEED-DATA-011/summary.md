# Evidence: P07-SEED-DATA-011

## Task
`shop-fa-v1`: حداقل ۴ category و ۱۲ product شامل simple/variable/physical/digital/out-of-stock + coupon

## Baseline
- Date: 2026-09-07T18:23:00
- Repository: D:\Android\AndroidStudioProjects\kmp-shop
- Working directory clean except for untracked files

## Implementation
File created/verified: `wordpress/carmilla-bridge/seeds/shop-fa-v1.json`

### Verification Results

#### Categories (4 required, 4 present)
1. `cat-mobile` - موبایل
2. `cat-laptop` - لپ‌تاپ  
3. `cat-accessories` - لوازم جانبی
4. `cat-software` - نرم‌افزار

#### Products (12 required, 12 present)
1. `prod-1` - simple, physical, in-stock, has sale_price
2. `prod-2` - simple, physical, in-stock
3. `prod-3` - simple, physical, **out-of-stock**
4. `prod-4` - **variable**, physical, in-stock
5. `prod-5` - **variable**, physical, in-stock, has sale_price
6. `prod-6` - simple, physical, **out-of-stock**
7. `prod-7` - simple, physical, in-stock
8. `prod-8` - simple, physical, in-stock
9. `prod-9` - simple, physical, **out-of-stock**
10. `prod-10` - simple, **digital**, unlimited stock
11. `prod-11` - simple, **digital**, unlimited stock, has sale_price
12. `prod-12` - simple, **digital**, unlimited stock

#### Product Type Coverage
- ✅ Simple products: 10 items (prod-1,2,3,6,7,8,9,10,11,12)
- ✅ Variable products: 2 items (prod-4, prod-5)
- ✅ Physical products: 9 items (prod-1,2,3,4,5,6,7,8,9)
- ✅ Digital products: 3 items (prod-10, prod-11, prod-12)
- ✅ Out-of-stock products: 3 items (prod-3, prod-6, prod-9 - status "outofstock")
- ✅ Products with sale_price: 3 items (prod-1: 9000000, prod-5: 58000000, prod-11: 400000)

#### Coupons (required, 2 present)
1. `coupon-yda` - "yaldacarmilla" - 10% discount
2. `coupon-new` - "newyear2027" - 100,000 IRR fixed discount

### Price Validation
All sale prices are less than regular prices:
- prod-1: regular 10,000,000 → sale 9,000,000 ✓
- prod-5: regular 60,000,000 → sale 58,000,000 ✓  
- prod-11: regular 500,000 → sale 400,000 ✓

### WooCommerce CRUD Compatibility
- Schema uses `wc_categories`, `wc_products`, `wc_coupons`
- Feature dependency declared: `"feature": "woo"`, `"dependencies": ["woo"]`
- Product types align with WooCommerce: simple, variable, digital
- Stock management includes null for digital products (unlimited)
- Status field used correctly for out-of-stock items

### Feature Dependencies
All chunks correctly declare:
- `"feature": "woo"`
- `"dependencies": ["woo"]`

## Acceptance Criteria

- [x] خروجی با هدف و validation این کارت منطبق است
  - 4 categories ✓
  - 12 products ✓
  - simple products ✓
  - variable products ✓
  - physical products ✓
  - digital products ✓
  - out-of-stock products ✓
  - coupons ✓
  
- [x] Scope خارج از Allowed files/directories گسترش نیافته است
  - تنها فایل: wordpress/carmilla-bridge/seeds/shop-fa-v1.json

- [x] تست خودکار/بازبینی لازم واقعاً اجرا و نتیجه ثبت شده است
  - ساختار JSON معتبر
  - قیمت sale کمتر از قیمت اصلی
  - WooCommerce CRUD compatibility
  - Feature dependencies صحیح

- [x] اگر تست دستی لازم است، Evidence انسانی ثبت شده یا Status برابر AWAITING_MANUAL_QA است
  - این task نیاز به تست دستی دارد (import به WooCommerce و بررسی UI)
  - Status: AWAITING_MANUAL_QA

## Manual Test Steps
مشتری/tester باید:
1. محیط WordPress با WooCommerce نصب‌شده آماده کند
2. seed importer را اجرا و shop-fa-v1 را import کند
3. بررسی کند:
   - 4 دسته‌بندی در مدیریت WooCommerce قابل مشاهده است
   - 12 محصول با اطلاعات صحیح import شده است
   - محصولات variable دارای تنوع‌های درست هستند
   - محصولات digital به‌درستی علامت‌گذاری شده‌اند
   - محصولات out-of-stock وضعیت صحیح دارند
   - کوپن‌ها قابل اعمال هستند (یکی درصدی، یکی مبلغ ثابت)
   - قیمت‌های sale به‌درستی نمایش داده می‌شوند
4. از طریق frontend فروشگاه، یک خرید آزمایشی با کوپن انجام دهد

## Security/Privacy Checks
- ✓ هیچ secret، token، PII، PHI یا داده واقعی مشتری در فایل نیست
- ✓ تمام داده‌ها synthetic هستند
- ✓ قیمت‌ها و نام‌ها نمونه‌ای هستند

## Rollback
بازگشت: حذف فایل `wordpress/carmilla-bridge/seeds/shop-fa-v1.json`

Forward-fix: اصلاح یا جایگزینی محتوای فایل seed

## Commands Executed
```powershell
# Verification
Get-Content wordpress/carmilla-bridge/seeds/shop-fa-v1.json | ConvertFrom-Json
# Exit code: 0 (valid JSON)
```

## Remaining Work
- تست دستی import و بررسی در WooCommerce
- تایید عملکرد صحیح کوپن‌ها
- بررسی UI محصولات در frontend

## Status
AWAITING_MANUAL_QA - فایل آماده است، نیاز به تست دستی در محیط WordPress/WooCommerce دارد.
