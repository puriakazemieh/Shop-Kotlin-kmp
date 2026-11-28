# برنامه یکپارچگی Elementor

تاریخ: ۲۰۲۶-۱۱-۲۸. این طرح آینده‌نگر است و نشانه آماده بودن یا پوشش فعلی نیست. قرارداد محصولات مستقل و صف اجرای پروژه مقدم‌اند. کارت موجود `P04-WPTHEME-CODE-025` برای layout هنوز در انتظار QA است؛ کارت‌های تازه جایگزین شواهد آن نمی‌شوند.

## هدف و حدود

ساخت همه بخش‌های سایت با Elementor در ترکیب‌های واقعاً پشتیبانی‌شده، استفاده از ویجت‌های رسمی و ارائه قابلیت‌های Carmilla به صورت ویجت با استفاده از همان خدمات اصلی. قالب به‌تنهایی و افزونه روی قالب دیگر مستقل‌اند؛ نصب هم‌زمان نباید ثبت تکراری بسازد. Elementor اختیاری است و حذف آن اطلاعات را پاک نمی‌کند. کد Pro توزیع نمی‌شود و قابلیت Pro نیازمند نصب و مجوز معتبر است.

پشتیبانی از «همه ویجت‌ها» به معنی تحمیل فهرست ثابت از همه افزونه‌های شخص ثالث یا بازنویسی موتور Elementor نیست. کارت کشف، فهرست نسخه‌دار از ویجت‌های رسمی Free/Pro، ویجت‌های Carmilla و افزونه‌های ثالثِ هدف تهیه می‌کند؛ سازگاری هر مورد تنها بعد از آزمون همان ترکیب اعلام می‌شود. نسخه‌های آینده و اجزای ناشناخته بدون آزمون ضمانت ندارند. ویژگی‌های آزمایشی، افزونه ثالث و Pro پیش‌نیاز و وضعیت جدا دارند.

## معماری و sync

وردپرس و Elementor مرجع ویرایش و انتشار طراحی وب هستند؛ خدمات و entitlementهای موجود Carmilla مرجع داده و عملیات دامنه‌اند. جزئیات مالکیت و مسیر همگام‌سازی در ADR تعیین و آزمون می‌شود: تغییرات منتشرشده، شناسه پایدار، revision، مجوز، cache و invalidation، تعارض و rollback. تنظیمات Elementor حاوی secret یا داده خصوصی نباشد. برای وب از رندر و APIهای پشتیبانی‌شده Elementor استفاده شود؛ ساختار JSON نسخه‌دار است و به تنهایی قرارداد تضمین‌شده رندر همه ویجت‌ها روی Android/iOS/Desktop نیست. پیش از تعهد به برابری native باید راهبرد عملی، پوشش دقیق، وابستگی‌ها، امنیت و رفتار ویجت ناشناخته تصویب و آزمون شود؛ هر فاصله صریح گزارش می‌شود.

برای هدر/فوتر، Theme locations و fallback رسمی با display conditions و layout بررسی شوند. Canvas عمداً هدر/فوتر را پنهان می‌کند. منوی وردپرس و Menu/Mega Menu المنتور دو مدل جدا هستند؛ همگام‌سازی و UX هر کدام، موبایل و RTL و دسترسی صفحه‌کلید در کارت‌های جدا آزمون می‌شوند. افزونه نباید قالب ثالث را بدون قرارداد در اختیار بگیرد.

## ترتیب

۱۲ کارت TODO پس از فاز ۱۰ صف قرار دارند؛ شناسه‌های P04 برای حفظ ارجاع‌ها ثابت مانده‌اند. شروع کشف به تکمیل واقعی گیت انتشار، کار انتشار و بررسی امنیتی P10 و کارت سازگاری پایه فاز ۴ وابسته است؛ DONE تاریخی به‌تنهایی اثبات گیت باز نیست. انتشار اولیه شامل ادعای پشتیبانی کامل Elementor نیست. ترتیب کارت‌ها: کشف، ADR، زیرساخت، هدر/فوتر، منو، مگامنو، ویجت‌های پایه، فروشگاه، آموزش، رزرو، sync و QA. ترتیب به معنی آماده بودن وابستگی‌های قابلیت‌های دامنه نیست؛ هر کارت تا فراهم شدن سرویس و entitlement واقعی مربوطه متوقف می‌ماند. محدوده‌های تازه یا بیش از M پیش از اجرا به کارت‌های کوچک‌تر شکسته می‌شوند. این مجموعه Gateهای قدیمی را تغییر نمی‌دهد و کارهای پروژه در هر نوبت طبق صف اجرا می‌شوند.

## مستندات رسمی بررسی‌شده

- [مستندات توسعه Elementor](https://developers.elementor.com/docs/)؛ [ثبت ویجت](https://developers.elementor.com/docs/managers/registering-widgets.html)، [وابستگی assets](https://developers.elementor.com/docs/widgets/widget-dependencies) و [بررسی سازگاری](https://developers.elementor.com/docs/addons/compatibility/).
- [ثبت مکان‌های قالب](https://developers.elementor.com/docs/themes/registering-locations/) و [نمایش مکان‌ها و fallback](https://developers.elementor.com/theme-locations-api/displaying-locations/).
- [ساختار داده](https://developers.elementor.com/docs/data-structure/)، [ساختار عناصر](https://developers.elementor.com/docs/data-structure/general-elements/) و [ویجت‌های atomic](https://developers.elementor.com/docs/data-structure/atomic-widgets/).
- [مستند منوی WordPress](https://elementor.com/help/create-menu/)، [ویجت Menu و شرایط آزمایشی آن](https://elementor.com/help/the-menu-widget/)، [شرایط نمایش قالب](https://elementor.com/help/conditions/) و [رفتار Canvas](https://elementor.com/help/how-to-fix-a-missing-header-or-footer-in-elementor/).
- [Dynamic tags](https://developers.elementor.com/docs/dynamic-tags/) و [cache خروجی ویجت](https://developers.elementor.com/docs/widgets/widget-output-caching/).

این مطالعه بخش‌های مرتبط را پوشش می‌دهد، نه ادعای مطالعه تمامی صفحات مستندات؛ کارت کشف باید مستندات و فهرست جامع نسخه مورد هدف را بازبینی و تکمیل کند.
