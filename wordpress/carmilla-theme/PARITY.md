# تطابق کامل اپ ↔ پوسته وردپرس (Parity Tracker)

هدف: **هیچ تفاوتی** بین سایت کاتلینی و سایت وردپرسی نباشد — نه در دیزاین/ریسپانسیو، نه در فیچرها.
این سند وضعیت تک‌تک اسکرین‌های اپ و معادل قالب وردپرس را ردیابی می‌کند.

## راهنمای وضعیت
- ✅ **کامل** — قالب ساخته و با دیزاین هم‌تراز است.
- 🟡 **چیدمان آماده** — قالب هست؛ داده‌ی واقعی پس از ساخت CPT/endpointها در پلاگین (فاز B‑۱) وصل می‌شود.
- ⬜ **مانده** — هنوز ساخته نشده.
- 🔌 **wp-admin/پلاگین** — مدیریت از پنل وردپرس/WooCommerce یا REST پلاگین انجام می‌شود (نه قالب فرانت).

## پایه‌ی مشترک (همه‌ی صفحات از آن ارث می‌برند) — ✅
توکن‌های رنگ (روشن/تاریک)، **فونت Vazirmatn self-hosted** (۵ وزن)، RTL، سه عرض محتوا ۶۴۰/۸۴۰/۱۲۰۰، گرید تطبیقی ۲/۳/۴، **سیستم آیکون SVG** (۲۸ آیکون خطی هم‌سبک drawableهای اپ)، قیمت فارسی (`carmilla_price`).

### ریسپانسیو — عیناً مطابقِ `WindowSize.kt` ✅ (v0.6.1)
ناوبری و اندازه‌ها مو‌به‌مو مثل کامپوز در هر سه بریک‌پوینت:
- **موبایل (`<۶۰۰`, Compact):** نوارِ پایین + گرید ۲ ستونه.
- **تبلت (`۶۰۰–۸۴۰`, Medium):** **ریلِ کناریِ فشرده ۹۲px (فقط آیکن)** در سمتِ start (راست در RTL) + گرید ۳ ستونه؛ نوارِ پایین حذف — دقیقاً مثل `SideNavRail(expanded=false)`.
- **دسکتاپ (`≥۸۴۰`, Expanded):** **ریلِ گسترده‌ی ۲۴۰px (آیکن + برچسب)** + گرید ۴ ستونه — مثل `SideNavRail(expanded=true)`.
- عرضِ محتوا در همه‌جا: readable ۶۴۰ / medium ۸۴۰ / wide ۱۲۰۰ (`responsiveMaxWidth`).

## فروشگاه و کاتالوگ
| اسکرین اپ | قالب وردپرس | وضعیت |
|---|---|---|
| ProductsOverview (خانه) | `front-page.php` | ✅ |
| Categories / CategorySearch | `woocommerce/` archive + `taxonomy-product_cat` | 🟡 |
| Search | `search.php` + `searchform.php` | ✅ |
| DetailsScreen (محصول) | `single-product` (WC) + `inc/product.php` | ✅ |
| Bundle List/Detail | محصول گروهی + `[carmilla_bundles]` (`inc/bundle.php`) | ✅ |
| ShoppingAssistant | `[carmilla_assistant]` + `assistant.js` (REST) | ✅ |
| StoryDetail | نمایشگر تمام‌صفحه (`story-viewer` + `stories.js`) | ✅ |
| Comparison | `[carmilla_compare]` + `compare.js` (جدول تمام‌عرض، REST) | ✅ |

## سبد، سفارش، پرداخت
| اسکرین | قالب | وضعیت |
|---|---|---|
| Cart / Checkout / PaymentCompleted | WooCommerce (استایل کارمیلا) | 🟡 |
| OrderList / OrderDetail (+فاکتور) | `myaccount` (WC) + view سفارش | 🟡 |
| OrderTracking (تایم‌لاین وضعیت) | `woocommerce_order_details_after_order_table` + `inc/orders-extra.php` | ✅ |
| RecurringOrders / ReturnRequest | تب‌های `myaccount` (`orders-extra.js`+REST؛ CPT مرجوعی) | ✅ |

## پروفایل و باشگاه
| اسکرین | قالب | وضعیت |
|---|---|---|
| Profile / Wallet | `myaccount` تب‌ها (WC) | 🟡 |
| Favorites (علاقه‌مندی) | قلبِ کارت‌ها + تب `myaccount` (`wishlist.js`+REST/user-meta) | ✅ |
| CustomerClub / Membership / Referral | تب‌های `myaccount`: باشگاه (سطح+امتیاز از مجموع خرید) + معرفی + کیف پول | ✅ |

## بلاگ
| اسکرین | قالب | وضعیت |
|---|---|---|
| BlogList | `index.php` / `archive.php` | ✅ |
| BlogDetail | `single.php` (+کامنت=پرسش) | ✅ |

## احراز هویت و پشتیبانی
| اسکرین | قالب | وضعیت |
|---|---|---|
| Login / Register / Forgot / Reset | `myaccount` (WC) استایل‌شده | 🟡 |
| Settings | تب `myaccount`: پوسته (روشن/تاریک/سیستم) + زبان (`orders-extra.js`) | ✅ |
| Support / ContactUs (چت) | `[carmilla_support]` (چت زنده با پاسخ ادمین) | ✅ |

## آکادمی (دوره)
| اسکرین | قالب | وضعیت |
|---|---|---|
| CourseList | `archive-cb_course.php` | 🟡 |
| CourseDetail | `single-cb_course.php` | 🟡 |
| CourseLearn (پخش‌کننده) | `single-cb_course.php` + `course-learn.js` (پلیر + پیشرفت) | ✅ |
| CourseQuiz / LessonQuiz | آزمونِ پایانِ دوره در تک‌دوره (`cb_quiz` meta + `academy.js`+REST، نمره‌دهی سمت سرور) | ✅ |
| Certificates / CertificateVerify | تب `myaccount` + `[carmilla_verify]` عمومی (صدور خودکار با قبولی) | ✅ |
| PlacementQuiz | `[carmilla_placement]` (تعیین سطح → لینک دوره‌های همان سطح) | ✅ |
| ProjectSubmission / PeerReview | تک‌دوره: CPT `cb_submission` + نظرِ همتایان (`academy.js`+REST) | ✅ |

## کلینیک (مشاوره/نوبت)
| اسکرین | قالب | وضعیت |
|---|---|---|
| TherapistList | `archive-cb_therapist.php` | 🟡 |
| TherapistDetail (رزرو) | `single-cb_therapist.php` + تقویم اسلات (JS) | 🟡 |
| TherapistMatch | پرسشنامه‌ی تطبیق در آرشیو مشاوران (`therapist-match.js`+REST) | ✅ |
| MoodCheckIn / Journal / Homework | تب «پرونده‌ی مشاوره» در `myaccount` (`clinic.js`+REST) | ✅ |
| MyAppointments / Messaging / SessionReceipt / Emergency | تب «نوبت‌های من» (`appointments.js`+REST): لیست/لغو، ترد پیام، رسید، بنر اورژانس | ✅ |

## تست روان‌شناسی
| اسکرین | قالب | وضعیت |
|---|---|---|
| PsychTestList | `archive-cb_psychtest.php` | ✅ |
| PsychTest detail | `single-cb_psychtest.php` | ✅ |
| TakeTest | `single-cb_psychtest.php` + `psychtest.js` (نمره‌دهی سمت سرور) | ✅ |

## درخواست دوره
| اسکرین | قالب | وضعیت |
|---|---|---|
| CourseRequest (لیست + ثبت + لایک) | `archive-cb_course_request.php` + `course-requests.js` | ✅ |

## پنل ادمین (۱۶+ اسکرین)
مدیریت محصول/سفارش/دوره/درمانگر/تست/مالی/بلاگ/استوری/تخفیف/باندل/... — 🔌 از طریق **wp-admin + WooCommerce + REST پلاگین** (معادل عملیاتی پنل ادمین اپ). در صورت نیاز به پنل فرانت‌اندِ سفارشی، فاز جداگانه.

---

### جمع‌بندی مسیر رسیدن به تطابق کامل
1. **پایه + فروشگاه + بلاگ** ✅ (این فاز).
2. **پلاگین (B‑۱):** CPT/endpointهای آکادمی/کلینیک/تست/درخواست‌دوره + WooCommerce commerce → صفحات 🟡 «داده‌دار» می‌شوند.
3. **صفحات تعاملی (JS+REST):** پخش دوره، آزمون، رزرو تقویمی، چت، تطبیق، تست‌دادن.
4. **حساب کاربری کامل** (باشگاه/عضویت/رفرال/مرجوعی) روی `myaccount`.
5. صیقل نهایی و QA بصری صفحه‌به‌صفحه در ۳ بریک‌پوینت.

---

## به‌روزرسانی نهایی — قالبِ خودکفا (بدون پلاگین) ✅
تصمیم نهایی: همه‌چیز theme-only با مکانیزم‌های بومی وردپرس. انجام‌شده:
- پایه + فروشگاه + بلاگ ✅
- مدیریت قالب (Customizer): رنگ/لوگو/عنوان/هیرو + فعال‌غیرفعال بخش‌ها ✅
- CPTها + متاباکس ادمین (دوره/مشاور/تست/درخواست/استوری/بنر/نوبت) ✅
- داشبورد ادمین در پیشخوان + درون‌ریزی محتوای نمونه (یک‌کلیک) ✅
- فیچرهای تعاملیِ داده‌محور (theme-REST + JS): درخواست‌دوره، تست‌دادن، رزرو نوبت تقویمی، پخش دوره + پیشرفت، چت پشتیبانی ✅
- WooCommerce: استایل کامل (دکمه/قیمت/کارت/سبد/تسویه/حساب) + تب‌های «کیف پول»/«معرفی» ✅

### صفحه‌ی محصول (DetailsScreen) — کامل شد ✅
`inc/product.php` بخش‌های زیر را طبق دیزاین اپ به تک‌محصولِ WooCommerce می‌افزاید (بدون تب، تک‌اسکرول):
- «معرفی محصول» با بولت‌های تیک‌دار.
- کارت «مشخصات» از ویژگی‌های محصول + برند.
- «خلاصه‌ی امتیاز»: میانگین + نمودار میله‌ای توزیع ۵→۱ ستاره.
- **نظرات دارای تصویر** (آپلود در فرم دیدگاه + نمایش زیر متن نظر).
- «پرسش و پاسخ» جدا از نظرات (theme-REST `products/{id}/questions` + `product-qna.js`؛ پاسخ کارشناس از پیشخوان).

### اسکرین‌های فرعی — کامل شد ✅ (v0.5.0)
- **نمایشگر استوری** (overlay تمام‌صفحه با نوار پیشرفت و CTA).
- **مقایسه‌ی محصولات** (لیست localStorage + دکمه‌ها + جدول تمام‌عرض).
- **باندل** (محصول گروهی + خلاصه‌ی مجموع + شبکه).
- **دستیار خرید** (جریان دسته/بودجه/مرتب‌سازی).
- **تطبیق درمانگر** (پرسشنامه‌ی نگرانی در آرشیو مشاوران).
- **پرونده‌ی مشاوره**: حال امروز (مود)، ژورنال، تمرین‌ها.
- **باشگاه مشتریان** (سطح + امتیاز از مجموع خرید + نوار پیشرفت).

### کلینیک — تکمیل شد ✅ (v0.5.1)
تب «نوبت‌های من» در حساب کاربری: فهرست نوبت‌ها + لغو، **رسید جلسه**، **پیام‌رسانی هر جلسه** (پاسخ مشاور از دیدگاه‌های نوبت در پیشخوان)، و **بنر اورژانس** با شماره‌ی قابل‌تنظیم در Customizer.

## پوششِ کاملِ مسیرها (۷۰ route از `Screen.kt`) — قطعی ✅
هر مسیرِ ناوبریِ اپ ↔ معادلِ وردپرس. **هیچ موردی بدون معادل نمانده.**

| # | Route (اپ) | معادلِ وردپرس |
|---|---|---|
| 1 | Login/Register/ForgotPassword/ResetPassword/AuthGraph | WooCommerce `myaccount` (فرم‌های استایل‌شده) |
| 2 | HomeGraph/ProductsOverview | `front-page.php` |
| 3 | BlogList / BlogDetail | `index/archive.php` / `single.php` |
| 4 | Cart / Checkout / PaymentCompleted | WooCommerce (سبد/تسویه/`order-received`) |
| 5 | Categories | `[carmilla_categories]` (`inc/catalog-extra.php`) |
| 6 | CategorySearch | آرشیوِ دسته‌ی WooCommerce (`taxonomy-product_cat`) |
| 7 | Search | `search.php` + `searchform.php` |
| 8 | Profile | داشبوردِ `myaccount` |
| 9 | MyOrders / OrderDetail | سفارش‌های `myaccount` + `view-order` |
| 10 | OrderTracking | تایم‌لاین زیرِ `view-order` (`inc/orders-extra.php`) |
| 11 | ReturnRequest | تب `returns` (CPT `cb_return` + REST) |
| 12 | RecurringOrders | تب `recurring` (user-meta + REST) |
| 13 | Settings | تب `settings` (روشن/تاریک/سیستم + زبان) |
| 14 | Wallet | تب `wallet` |
| 15 | Favorites | قلبِ کارت‌ها + تب `favorites` (`inc/wishlist.php`) |
| 16 | ContactUs | `[carmilla_support]` (چت با پاسخ ادمین) |
| 17 | ProductDetail | تک‌محصولِ WooCommerce + `inc/product.php` |
| 18 | CustomerClub | تب `club` (سطح/امتیاز) |
| 19 | Referral | تب `referral` |
| 20 | Membership | تب `membership` (عضویت ویژه از کیف پول + تخفیف خودکار) |
| 21 | ShoppingAssistant | `[carmilla_assistant]` |
| 22 | Comparison | `[carmilla_compare]` |
| 23 | BundleList / BundleDetail | `[carmilla_bundles]` + محصولِ گروهی |
| 24 | CourseCatalog | `archive-cb_course.php` |
| 25 | MyCourses | تب `my-courses` (+نوار پیشرفت) |
| 26 | FreeCourses | آرشیوِ دوره با `?free=1` |
| 27 | CoursesByLevel | آرشیوِ دوره با `?level=` (لینکِ آزمونِ تعیینِ سطح) |
| 28 | InstructorCourses | آرشیوِ دوره با `?instructor=` (نامِ مدرس کلیک‌پذیر) |
| 29 | CourseDetail / CourseLearn | `single-cb_course.php` (+پلیر/پیشرفت) |
| 30 | CourseQuiz / LessonQuiz | موتورِ آزمون `cb_quiz` (نمره‌دهیِ سمت سرور + صدور گواهی) |
| 31 | Certificates / CertificateVerify | تب `certificates` + `[carmilla_verify]` |
| 32 | PlacementQuiz | `[carmilla_placement]` |
| 33 | ProjectSubmission / PeerReview | تک‌دوره: CPT `cb_submission` + نظرِ همتایان |
| 34 | CourseRequests | `archive-cb_course_request.php` |
| 35 | TherapistCatalog / TherapistDetail | آرشیو/تک‌مشاور + تقویمِ رزرو |
| 36 | TherapistMatch | پرسشنامه‌ی تطبیق در آرشیوِ مشاوران |
| 37 | MyAppointments / SessionReceipt / MessagingThread | تب `appointments` (لغو/رسید/پیام) |
| 38 | MoodCheckIn / Journal / Homework | تب `clinic` (سه زیرتب) |
| 39 | EmergencyResources | بنرِ اورژانس (قابل‌تنظیم در Customizer) |
| 40 | PsychTestCatalog / TakeTest | آرشیو/تکِ تست + نمره‌دهیِ سمت سرور |
| 41 | AdminPanel/ManageProduct/ManageOrders/ManageOptions/ManageDiscounts/ManageWallets/ManageWithdrawals/ManageStories/AdminBlogList/ManageBlog | 🔌 wp-admin + WooCommerce (پنلِ مدیریتِ بومیِ وردپرس) |

> **LessonQuiz** و **CourseQuiz** یک نوع اسکرین‌اند (فرمِ آزمون)؛ هر دو با موتورِ آزمونِ مشترک سرو می‌شوند. **MainGraph/More** ناوبری‌اند (هدر + نوار پایینِ موبایل)، نه صفحه‌ی محتوایی.

### تکمیل صفحات باقی‌مانده — کامل شد ✅ (v0.6.0)
مرورِ کاملِ ۷۵ اسکرینِ اپ و ساختِ تک‌تکِ موارد نداشته:
- **رهگیری سفارش**: تایم‌لاینِ وضعیت (ثبت→پردازش→ارسال→تحویل) زیرِ جدولِ سفارش + کد رهگیری پستی.
- **مرجوعی/تعویض**: تب حساب کاربری با فرمِ ثبت + فهرستِ درخواست‌ها؛ مدیریت از wp-admin (CPT `cb_return`).
- **خریدهای تکراری**: تب حساب کاربری (فهرست + لغو) با user-meta.
- **تنظیمات**: تب حساب کاربری — سوییچِ پوسته (روشن/تاریک/سیستم با `data-theme` و ذخیره در مرورگر) + زبان.
- **علاقه‌مندی‌ها (Favorites)**: دکمه‌ی قلب روی کارت‌ها و تک‌محصول + تب حساب کاربری (user-meta؛ مهمان‌ها localStorage).
- **آزمونِ پایانِ دوره**: پارس سؤال‌ها از `cb_quiz`، نمره‌دهیِ امنِ سمت سرور، و **صدور خودکارِ گواهی** با قبولی.
- **گواهی‌های من + تأییدِ گواهی**: تب حساب کاربری + صفحه‌ی عمومیِ `[carmilla_verify]`.
- **آزمونِ تعیینِ سطح**: `[carmilla_placement]` → سطح + لینکِ دوره‌های همان سطح.
- **پروژه‌ی پایانی + نقدِ همتایان**: ثبتِ پروژه (CPT `cb_submission`، بازخوردِ مدرس) + نظرِ هم‌دوره‌ای‌ها.
- **دسته‌بندی‌ها**: شبکه‌ی دسته‌های WooCommerce (`[carmilla_categories]`) → آرشیوِ دسته.
- **دوره‌های من**: تب حساب کاربری با نوارِ پیشرفتِ هر دوره.
- **فیلترِ دوره‌ها**: رایگان/سطح/مدرس روی آرشیوِ دوره (چیپ‌های فیلتر + `?free=/?level=/?instructor=`).
- **عضویت ویژه**: تب حساب کاربری — فعال‌سازی/تمدید از کیف پول + **تخفیفِ خودکارِ ۵٪** در تسویه.

### باقی‌مانده
- **QA بصری** صفحه‌به‌صفحه روی نصب واقعی وردپرس در ۳ بریک‌پوینت (نیازمند محیط زنده).
- ویدیوکالِ زنده‌ی جلسه (تصمیم زیرساختی جدا؛ مثل خودِ اپ فعلاً لینک بیرونی).
