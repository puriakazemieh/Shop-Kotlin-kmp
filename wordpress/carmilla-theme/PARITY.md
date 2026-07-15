# تطابق کامل اپ ↔ پوسته وردپرس (Parity Tracker)

هدف: **هیچ تفاوتی** بین سایت کاتلینی و سایت وردپرسی نباشد — نه در دیزاین/ریسپانسیو، نه در فیچرها.
این سند وضعیت تک‌تک اسکرین‌های اپ و معادل قالب وردپرس را ردیابی می‌کند.

## راهنمای وضعیت
- ✅ **کامل** — قالب ساخته و با دیزاین هم‌تراز است.
- 🟡 **چیدمان آماده** — قالب هست؛ داده‌ی واقعی پس از ساخت CPT/endpointها در پلاگین (فاز B‑۱) وصل می‌شود.
- ⬜ **مانده** — هنوز ساخته نشده.
- 🔌 **wp-admin/پلاگین** — مدیریت از پنل وردپرس/WooCommerce یا REST پلاگین انجام می‌شود (نه قالب فرانت).

## پایه‌ی مشترک (همه‌ی صفحات از آن ارث می‌برند) — ✅
توکن‌های رنگ (روشن/تاریک)، **فونت Vazirmatn self-hosted** (۵ وزن)، RTL، سه عرض محتوا ۶۴۰/۸۴۰/۱۲۰۰، گرید تطبیقی ۲/۳/۴، **سیستم آیکون SVG** (۲۸ آیکون خطی هم‌سبک drawableهای اپ)، هدر چسبان + **نوار پایین موبایل**، قیمت فارسی (`carmilla_price`).

## فروشگاه و کاتالوگ
| اسکرین اپ | قالب وردپرس | وضعیت |
|---|---|---|
| ProductsOverview (خانه) | `front-page.php` | ✅ |
| Categories / CategorySearch | `woocommerce/` archive + `taxonomy-product_cat` | 🟡 |
| Search | `search.php` + `searchform.php` | ✅ |
| DetailsScreen (محصول) | `single-product` (WC) + `inc/product.php` | ✅ |
| Bundle List/Detail | قالب باندل (محصول WooCommerce) | ⬜ |
| ShoppingAssistant | صفحه‌ی دستیار (JS + REST) | ⬜ |
| StoryDetail | نمایشگر استوری (JS overlay) | ⬜ |
| Comparison | `page-compare` (جدول تمام‌عرض + JS) | ⬜ |

## سبد، سفارش، پرداخت
| اسکرین | قالب | وضعیت |
|---|---|---|
| Cart / Checkout / PaymentCompleted | WooCommerce (استایل کارمیلا) | 🟡 |
| OrderList / OrderDetail (+فاکتور) / Tracking | `myaccount` (WC) + view سفارش | 🟡 |
| RecurringOrders / ReturnRequest | افزونه/endpoint پلاگین | ⬜ |

## پروفایل و باشگاه
| اسکرین | قالب | وضعیت |
|---|---|---|
| Profile / Favorites / Wallet | `myaccount` تب‌ها (WC + wishlist) | 🟡 |
| CustomerClub / Membership / Referral | تب‌های سفارشی `myaccount` (REST پلاگین) | ⬜ |

## بلاگ
| اسکرین | قالب | وضعیت |
|---|---|---|
| BlogList | `index.php` / `archive.php` | ✅ |
| BlogDetail | `single.php` (+کامنت=پرسش) | ✅ |

## احراز هویت و پشتیبانی
| اسکرین | قالب | وضعیت |
|---|---|---|
| Login / Register / Forgot / Reset | `myaccount` (WC) استایل‌شده | 🟡 |
| Settings | تنظیمات کاربر (`myaccount`) | ⬜ |
| Support / ContactUs (چت) | صفحه‌ی تماس + تیکت (REST) | ⬜ |

## آکادمی (دوره)
| اسکرین | قالب | وضعیت |
|---|---|---|
| CourseList | `archive-cb_course.php` | 🟡 |
| CourseDetail | `single-cb_course.php` | 🟡 |
| CourseLearn (پخش‌کننده) | `single-cb_course.php` + `course-learn.js` (پلیر + پیشرفت) | ✅ |
| CourseQuiz / LessonQuiz | فرم آزمون (JS+REST) | ⬜ |
| Certificates / CertificateVerify | صفحه‌ی گواهی + تأیید | ⬜ |
| PlacementQuiz / PeerReview / ProjectSubmission | فرم‌های REST | ⬜ |

## کلینیک (مشاوره/نوبت)
| اسکرین | قالب | وضعیت |
|---|---|---|
| TherapistList | `archive-cb_therapist.php` | 🟡 |
| TherapistDetail (رزرو) | `single-cb_therapist.php` + تقویم اسلات (JS) | 🟡 |
| TherapistMatch | پرسشنامه‌ی تطبیق (JS+REST) | ⬜ |
| MyAppointments / Messaging / Journal / Homework / MoodCheckIn / SessionReceipt / Emergency | تب‌های کاربر (REST) | ⬜ |

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

### باقی‌مانده‌ی واقعی (اسکرین‌های فرعی، اولویت پایین)
- **Comparison** (مقایسه‌ی محصولات)، **Bundle**، **ShoppingAssistant**، **StoryDetail** (نمایشگر تمام‌صفحه‌ی استوری).
- کلینیک: **TherapistMatch**، **Journal/Homework/MoodCheckIn** (تب‌های کاربر).
- باشگاه/عضویت (**CustomerClub/Membership**) — فعلاً فقط «معرفی به دوستان» و «کیف پول».
- **QA بصری** صفحه‌به‌صفحه روی نصب واقعی وردپرس در ۳ بریک‌پوینت (نیازمند محیط زنده).
