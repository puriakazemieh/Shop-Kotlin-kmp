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

## فاز ۲ — تجارتِ کامل (نسخه ۰.۲.۰)

سبد، سفارش، پرداخت (ZarinPal)، کیف‌پول، پروفایل/آدرس و نظر/پرسش افزوده شد — همه با همان قرارداد DTOِ اپ.

### سبد (Bearer) — سبدِ سمتِ سرور در user-meta (مستقل از سشنِ WooCommerce)
| متد | مسیر | بدنه |
|---|---|---|
| GET | `api/cart` | → CartResponse |
| DELETE | `api/cart` | خالی‌کردن |
| POST | `api/cart/items` | `{productId?,variantId?,qty}` |
| PATCH | `api/cart/items/{itemId}` | `{qty}` |
| DELETE | `api/cart/items/{itemId}` | حذف خط |
| PUT | `api/cart/items/{variantId}` | `{qty}` |
| PATCH | `api/cart/items/{variantId}/adjust` | `{delta}` |
| POST | `api/cart/items/{itemId}/save-for-later` | |
| POST | `api/cart/items/{itemId}/move-to-cart` | |
| POST/DELETE | `api/cart/discount` | `{code}` (اعتبارسنجیِ کوپنِ WooCommerce) |

### سفارش (Bearer) — سفارش‌های WooCommerce
| متد | مسیر |
|---|---|
| GET | `api/orders` → List<OrderResponse> |
| GET | `api/orders/{id}` → OrderDetailResponse |
| POST | `api/orders` (`{items?,addressId?,useWallet?,isGift?,giftMessage?}`) — از سبد یا اقلامِ صریح؛ کسر از کیف‌پول |
| POST | `api/orders/{id}/cancel` — بازگشتِ وجهِ کیف‌پول |
| GET | `api/orders/{id}/track` → OrderTrackingResponse |
| POST | `api/orders/{id}/reorder` → ReorderResponse |

### پرداخت — درگاه ZarinPal
| POST `api/payment/request` (`{orderId}`) → `{paymentUrl}` | GET `api/payment/verify` → ۳۰۲ به دیپ‌لینکِ اپ |

تنظیمات (option در wp-admin یا ثابت در wp-config): `cb_zarinpal_merchant`، `cb_zarinpal_sandbox="1"`، `cb_app_return_url` (پیش‌فرض `carmilla://payment/result`). مبلغ پیش‌فرض تومان×۱۰ (ریال)، با فیلترِ `cb_zarinpal_amount` قابل‌تغییر.

### کیف‌پول (Bearer) — user-meta `cb_wallet_balance` + `cb_wallet_txns`
`GET api/wallet/balance` · `GET api/wallet/transactions?page&size` · `POST api/wallet/top-up {amount}` (شارژ با ZarinPal) · `POST api/wallet/withdraw {amount,iban}`

### پروفایل و آدرس (Bearer)
`GET/PATCH api/users/me` · `GET api/addresses[/default|/{id}]` · `POST/PATCH/DELETE api/addresses[/{id}]` · `POST api/addresses/{id}/default`

> نکته‌ی مسیر: اپ برای پروفایل/آدرس از مسیرِ ریشه (`/api/users/me`) استفاده می‌کند که خارج از `/wp-json/carmilla/v1/` می‌افتد؛ پلاگین این‌ها را با یک aliasِ سطحِ ریشه (`parse_request` → `rest_do_request`) به همان route هدایت می‌کند تا **اپ نیازی به تغییر نداشته باشد**.

### نظر و پرسش (کامنت‌محور)
`GET api/reviews/product/{id}` · `POST/PUT/DELETE api/reviews[/{id}]` · `POST api/reviews/{id}/helpful` · `GET api/questions/product/{id}` · `POST/PUT/DELETE api/questions[/{id}]` (کامنتِ نوعِ `cb_qna`، مخفی از فیدِ عادی).

### آزمونِ فاز ۲
```
php tests/smoke-phase2.php   # نگاشتِ وضعیتِ سفارش، ریاضیِ کوپن، شکل‌دهیِ آدرس
```

### اتصالِ اپِ اندروید به وردپرس (خروجی)
- برندِ `wp` در `core/designSystem/.../brand/Brand.kt` (`WpBrand.apiBaseUrl` = آدرسِ سایتِ شما).
- فلیورِ `wp` در `composeApp/build.gradle.kts` → `assembleWpDebug` / `assembleWpRelease`.
- بیلدِ `carmila` (سرورِ فعلی) بدونِ تغییر — اثباتِ غیرمخرب‌بودن.

## فاز ۳ — آکادمی (نسخه ۰.۳.۰)

عمودیِ آموزش کامل شد؛ CPTِ `cb_course` با مدلِ خطیِ درس (متایِ `cb_lessons`) — هم‌تراز با قالبِ کارمیلا تا سایتِ قالب+پلاگین دیتای مشترک داشته باشد. شناسه‌ی درس ساختگی و پایدار: `courseId*100000+(index+1)`.

### دوره‌ها و درس‌ها
| متد | مسیر | خروجی |
|---|---|---|
| GET | `api/courses` | List<CourseSummaryResponse> |
| GET | `api/courses/{slug}` | CourseDetailResponse (بخش/درس، ویدیوِ درس‌های رایگان یا خریداری‌شده) |
| GET | `api/academy/my-courses` | دوره‌های ثبت‌نام‌شده |
| POST | `api/academy/courses/{id}/enroll` | ثبت‌نامِ دوره‌ی رایگان |
| POST | `api/academy/courses/{id}/waitlist` | WaitlistResponse |
| POST | `api/academy/lessons/{lessonId}/progress` | ثبتِ پیشرفت per-کاربر (`cb_course_prog_{id}`) |
| POST | `api/academy/courses/{id}/mark-update-seen` | |

### آزمون و گواهی (امتیازدهیِ سمتِ سرور)
| GET | `api/academy/courses/{id}/quiz` — گزینه‌ها بدونِ لوِ پاسخ |
| POST | `api/academy/courses/{id}/quiz/submit` → نمره/قبولی؛ **صدورِ خودکارِ گواهی** روی قبولی |
| GET | `api/academy/certificates` — گواهی‌های من |
| GET | `api/courses/certificates/verify/{certNumber}` — تأییدِ عمومی |

شماره‌ی گواهی هم‌فرمولِ قالب: `CB-` + ۱۰ رقمِ md5(course-user-salt) — قطعی و یکتا per کاربر/دوره.

### آزمونِ تعیینِ سطح
`GET api/academy/placement-quiz` + `POST …/submit` → سطح (مبتدی/متوسط/پیشرفته) بر پایه‌ی مجموعِ امتیاز.

### پروژه‌ی پایانی و نقدِ همتایان
`POST api/academy/courses/{id}/project` (فایل) یا `…/project/link` (لینک) · `GET …/project` (MyProject) · `GET …/project/peers` (پروژه‌های تأییدشده‌ی دیگران) · `GET/POST api/academy/project/{submissionId}/comments`.

### پرسش‌وپاسخِ درس و بازگشتِ وجه
`GET/POST api/academy/lessons/{lessonId}/questions` (کامنتِ `cb_lesson_q` با متایِ ایندکسِ درس) · `POST api/academy/courses/{id}/refund-request` + `GET api/academy/refund-requests/mine`.

### آزمونِ فاز ۳
```
php tests/smoke-phase3.php   # پارسِ کوییز (نشانِ پاسخ)، تعیینِ سطح، کدکِ شناسه‌ی درس، شماره‌ی گواهی
```

## فاز ۴ — کلینیک + تستِ روان‌شناسی (نسخه ۰.۴.۰)

عمودیِ مشاوره و تست کامل شد. CPTهای `cb_therapist`/`cb_psychtest`/`cb_appointment` (هم‌تراز با قالب) + جدولِ `wp_cb_bookings` با **کلیدِ یکتا روی (therapist_id, slot_time)** برای **قفلِ اتمیکِ رزرو** (روی activation ساخته می‌شود).

### کلینیک (درمانگر/نوبت)
| متد | مسیر | نکته |
|---|---|---|
| GET | `api/therapists` / `api/therapists/{slug}` | بازه‌های خالی (`slotId = therapistId*100000+index`) |
| GET | `api/clinic/my-appointments` | نوبت‌های من |
| POST | `api/clinic/appointments` (`{slotId,notes?}`) | **رزروِ اتمیک**: INSERT با کلیدِ یکتا → رزروِ هم‌زمان ۴۰۹؛ مصرفِ **اعتبار جلسه** برای درمانگرِ پولی |
| POST | `api/clinic/appointments/{id}/cancel` | آزادسازیِ بازه + بازگشتِ اعتبار |
| GET | `api/clinic/appointments/{id}/receipt` | رسیدِ جلسه |
| GET/POST | `api/clinic/mood-checkins` | ثبتِ خلق‌وخو (۱..۵) |
| GET/POST/DELETE | `api/clinic/journal[/{id}]` | ژورنالِ شخصی |
| GET | `api/clinic/homework` + `…/{id}/complete` | تمرین‌های درمانگر |
| GET/POST | `api/clinic/therapists/{id}/messages` + `messaging-status` | پیام (کامنتِ `cb_msg`، PATIENT/THERAPIST، ۳ پیامِ رایگان) |
| POST | `api/clinic/switch-requests` + `…/mine` | درخواستِ تعویضِ درمانگر |
| GET/POST | `api/clinic/therapist-match/questions` + `submit` | تطبیقِ درمانگر بر پایه‌ی تگ/تخصص |

**اعتبارِ جلسه** در user-meta `cb_ther_credits_{therapistId}` (هم‌کلیدِ قالب) — با خریدِ بسته اعطا، با رزرو مصرف، با لغو بازگردانده می‌شود.

### تستِ روان‌شناسی
| متد | مسیر |
|---|---|
| GET | `api/psych-tests` / `api/psych-tests/{slug}` (گزینه‌ها **بدونِ امتیاز**) |
| GET | `api/my-psych-tests` (attemptها؛ خریدِ محصول = مالکیت) |
| GET | `api/my-psych-tests/{userTestId}/questions` |
| POST | `api/my-psych-tests/{userTestId}/submit` → **امتیازدهیِ سمتِ سرور** + تفسیرِ بازه‌ای (AUTO) یا انتظارِ مشاور (COUNSELOR) |

فرمتِ متا (هم‌ترازِ قالب): سؤال `text | label=score , …`؛ بازه `min | max | تفسیر`. **امتیازِ گزینه‌ها هرگز به کلاینت نمی‌رود.**

### آزمونِ فاز ۴
```
php tests/smoke-phase4.php   # کدکِ شناسه‌ی بازه، امتیازِ تطبیق، پارس/امتیاز/تفسیرِ تست
```

## فاز ۵ — افزوده‌های فروشگاهی (نسخه ۰.۵.۰)

| گروه | مسیرها | منبع |
|---|---|---|
| **عضویت/باشگاه** | `api/memberships/mine` · `api/memberships/subscribe` | user-meta `cb_membership_expires` + کسر از کیف‌پول |
| **معرفی** | `api/referrals/mine` | کدِ قطعیِ per کاربر + شمارنده‌ها |
| **علاقه‌مندی** | `GET/POST/DELETE api/favorites[/{id}]` | user-meta `cb_wishlist` (هم‌کلیدِ قالب) |
| **اخیراً‌دیده** | `GET api/recently-viewed` · `POST …/{id}` | user-meta (۴۰ مورد آخر) |
| **مرجوعی** | `POST api/return-requests` · `GET …/mine` | user-meta `cb_returns` |
| **سفارشِ تکراری** | `POST api/recurring-orders` · `GET …/mine` · `POST …/{id}/cancel` | user-meta `cb_recurring` |
| **اطلاع‌رسانی** | `POST api/stock-notifications` · `POST api/price-alerts` | user-meta |
| **پشتیبانی** | `GET/POST api/support/tickets` · `GET …/{id}` · `POST …/{id}/messages` | user-meta `cb_tickets` (تِرِدِ پیام) |
| **باندل** | `GET api/bundles` · `GET api/bundles/{slug}` | محصولِ **grouped**ِ WooCommerce |
| **استوری** | `GET api/stories` | CPTِ `cb_story` (فعال/غیرمنقضی) |
| **پیشنهادِ مکمل** | `GET api/products/{id}/frequently-bought-together` | محصولاتِ هم‌دسته |

علاقه‌مندی/اخیراً‌دیده از مسیرِ ریشه (`/api/favorites`) استفاده می‌کنند؛ با همان aliasِ سطحِ ریشه هدایت می‌شوند (بدونِ تغییرِ اپ).

### آزمونِ فاز ۵
```
php tests/smoke-phase5.php   # پنجره‌ی فعالِ عضویت، کدِ قطعیِ معرفی
```

## فاز ۶ — پنلِ ادمین (نسخه ۰.۶.۰)

اپ به یک **پنلِ مدیریتِ کاملِ سایت** تبدیل شد. همه با نقشِ ادمین/مدیرِ فروشگاه.

### داشبورد و فروشگاه
| متد | مسیر |
|---|---|
| GET | `api/admin/stats` — درآمد/سفارش/محصول/مشتری، فروشِ ۷روز، شمارشِ عمودی‌ها، فروشِ امروز، کم‌موجودی |
| GET | `api/admin/orders` (فیلترِ وضعیت/کاربر، صفحه‌بندی) · `…/{id}` · PATCH `…/{id}/status` |
| GET/POST | `api/admin/categories` · PATCH/DELETE `…/{id}` (دسته‌های محصول) |
| GET/POST | `api/admin/discounts` · PATCH/DELETE `…/{id}` (کوپنِ WooCommerce) |
| GET | `api/admin/wallet/users/search` · POST `api/admin/wallet/adjust` |
| GET | `api/admin/wallet/withdrawals` · POST `…/{id}/process` (تأیید/رد + بازگشتِ وجه) |
| GET/DELETE | `api/admin/course-requests[/{id}]` |
| GET/PATCH | `api/admin/return-requests[/{id}]` |

> رهگیریِ وضعیتِ سفارش: `SHIPPED` (که ووکامرس ندارد) در متایِ `_cb_app_status` نگه‌داری و در خواندن اولویت داده می‌شود.

### مدیریتِ محتوایِ عمودی‌ها
| بخش | مسیرها |
|---|---|
| **دوره** | CRUD `api/admin/courses[/{id}]` · `…/{id}/quiz` (upsert) · `…/{id}/lessons` (افزودن) · `…/{id}/projects` · `…/projects/{sid}/review` |
| **درمانگر** | CRUD `api/admin/therapists[/{id}]` · `…/{id}/generate-slots` · `…/appointments` · `…/appointments/{id}/confirm|complete` |
| **تست** | CRUD `api/admin/psych-tests[/{id}]` · `…/pending-interpretations` · `…/user-tests/{id}/interpret` |
| **استوری** | CRUD `api/admin/stories[/{id}]` |
| **بازبینی** | `api/admin/academy/refund-requests[/{id}/review]` · `api/admin/clinic/switch-requests[/{id}/review]` |

**نکته‌ی هم‌ترازی:** محتوایِ ساخته‌شده از اپ همان متایِ خطی را می‌نویسد که سمتِ خواندن پارس می‌کند — پس در اپ و در قالبِ کارمیلا **یکسان** دیده می‌شود (تستِ رفت‌وبرگشتِ سازنده↔پارسر در `smoke-phase6.php`).

### آزمونِ فاز ۶
```
php tests/smoke-phase6.php   # رفت‌وبرگشتِ سازنده‌ی کوییز/تست/بازه + نگاشتِ وضعیتِ سفارش
```

### موارد به‌تعویق‌افتاده (نیازمندِ کارِ سنگین‌ترِ WooCommerce)
مدیریتِ عمیقِ تنوع/آپشن/تصویر/ویدیو/موجودیِ محصول (`api/admin/products/{id}/variants|images|videos`, `api/admin/options`, `api/admin/variants/*/inventory`)، صندلیِ سازمانی/B2B (`api/admin/organizations/*`)، و CRMِ کاملِ مراجع (`api/admin/therapists/{id}/patients/*`) و مدیریتِ باندل (`api/admin/bundles`) در تکرارِ بعدی افزوده می‌شوند؛ ساختِ/ویرایشِ محصولِ ساده از فاز ۱ موجود است.
