# گزارش شواهد و راستی‌آزمایی فنی P03-MANIFEST-CODE-025C

**شناسه کارت:** `P03-MANIFEST-CODE-025C`  
**عنوان:** اعمال guard عملیاتی آموزش (با ترنسپورت شمارنده واقعی در DI)  
**تاریخ اجرا:** ۱۵ نوامبر ۲۰۲۶  
**مخزن:** `D:/Android/AndroidStudioProjects/kmp-shop` روی شاخه `develop`  
**وضعیت:** `DONE`

---

## ۱. خلاصه هدف و انطباق با قرارداد

مطابق با نیازمندی کارت `P03-MANIFEST-CODE-025C` و سند معماری `docs/INDEPENDENT_PRODUCTS_SPEC_FA.md`:
- ریپازیتوری‌های حوزه آموزش شامل `AcademyRepositoryImpl`، `AdminAcademyRepositoryImpl` و `CourseRequestRepositoryImpl` به گارد عملیاتی `FeatureUseCaseGuard` متصل شدند.
- کلیه متدهای لایه داده آموزش قبل از فراخوانی API و ارسال هرگونه درخواست شبکه، وضعیت فیچرهای مربوطه را ارزیابی می‌کنند:
  - قابلیت‌های پایه آموزش با شناسه `"academy.core"` محافظت شدند (`getCourses`, `getCourse`, `getMyCourses`, `enroll`, `updateLessonProgress`, `joinWaitlist`, `submitProject`, `getLessonQuestions`, `createLessonQuestion`, `markCourseUpdateSeen`, `getPeerSubmissions`, `getPeerComments`, `requestRefund`, و متدهای ادمین دوره/سازمان/استرداد و متدهای `CourseRequestRepository`).
  - آزمون‌های دوره و درس با شناسه `"academy.quiz"` محافظت شدند (`getQuiz`, `submitQuiz`, `getLessonQuiz`, `submitLessonQuiz`, `getPlacementQuiz`, `submitPlacementQuiz`, `upsertQuiz`, `upsertLessonQuiz`).
  - گواهی‌ها با شناسه `"academy.certificate"` محافظت شدند (`getCertificates`, `verifyCertificate`).
- ناوبری صفحه با به‌روزرسانی `FeatureRouteGuard.kt` مسیرهای `LessonQuiz` و `ProjectSubmission` را نیز ذیل `academy.core` گارد می‌کند.
- تزریق وابستگی Koin در `dataModule.kt` با تزریق صریح `getOrNull<FeatureUseCaseGuard>()` به‌روزرسانی شد.
- با ترنسپورت واقعی HTTP بر بستر `com.sun.net.httpserver.HttpServer` و پورت پویا روی `127.0.0.1`، رفتار شمارنده واقعی و عدم دور زدن auth/ownership در شرایط مختلف آزمایش و تأیید گردید.

---

## ۲. سناریوهای آزمون و نتایج ترنسپورت واقعی

کلاس آزمون `core/data/src/jvmTest/kotlin/com/kazemieh/data/academy/AcademyOperationalGuardTest.kt` با اجرای ۵ آزمون رفتاری ترنسپورت را راستی‌آزمایی نمود:

1. **حالت خاموش بودن قابلیت (Fail-Closed با دقیقاً ۰ درخواست شبکه):**
   - فیچرهای `academy.core`، `academy.quiz` و `academy.certificate` غیرفعال شدند.
   - فراخوانی تک‌تک متدهای `academyRepo.getCourses()`، `academyRepo.getCourse("...")`، `academyRepo.getMyCourses()`، `academyRepo.getQuiz(1L)`، `academyRepo.getCertificates()`، `courseRequestRepo.list()` و `adminAcademyRepo.listCourses()` بلافاصله با خطای ۴۰۳ (`AppResult.Error`) متوقف شدند.
   - شمارنده درخواست‌های ترنسپورت روی عدد **۰** باقی ماند (`requestCount.get() == 0`).

2. **حالت روشن بودن قابلیت (هدایت مجاز درخواست‌ها و افزایش شمارنده):**
   - فیچرهای `admin.mobile`، `academy.core`، `academy.quiz` و `academy.certificate` فعال شدند.
   - توکن احراز هویت در `TokenManager` ثبت شد.
   - فراخوانی‌های متوالی انجام و شمارنده ترنسپورت دقیقاً به ازای هر درخواست ۱ واحد افزایش یافت (از ۱ تا ۵) و خروجی `AppResult.Success` دریافت شد.

3. **سلسله‌مراتب زیرقابلیت‌ها (Hierarchy of Sub-features):**
   - در حالت فعال بودن `academy.core` و غیرفعال بودن `academy.quiz` و `academy.certificate`:
     - فراخوانی `getCourses()` موفق شد و شمارنده ترنسپورت ۱ شد.
     - فراخوانی `getQuiz(1L)` با خطای ۴۰۳ مربوط به `academy.quiz` متوقف شد و **۰ درخواست اضافی** ارسال کرد (شمارنده روی ۱ ماند).
     - فراخوانی `getCertificates()` با خطای ۴۰۳ مربوط به `academy.certificate` متوقف شد و **۰ درخواست اضافی** ارسال کرد (شمارنده روی ۱ ماند).

4. **عدم امکان دور زدن Auth و مالکیت سمت سرور (Auth & Ownership Integrity):**
   - ارسال الزامی هدر `Authorization: Bearer <token>` در صورت فعال بودن فیچر بر روی ترنسپورت دریافت و ثبت شد.
   - تلاش برای دسترسی به دوره/منبع ممنوعه (متعلق به کاربر یا مستاجر دیگر) منجر به بازگشت خطای ۴۰۳ سمت سرور شد و اثبات شد که toggle کلاینت نمی‌تواند کنترل مالکیت سرور را دور بزند.

5. **پویایی زمان اجرا (Dynamic Runtime Transition):**
   - سامانه با قابلیت خاموش مقداردهی شد (۰ درخواست و کد ۴۰۳).
   - با به‌روزرسانی دینامیک `store.update(ManifestBootstrapState.Ready(...))` بدون rebuild مجدد DI یا کلاینت، درخواست بعدی مستقیماً به ترنسپورت ارسال شد و شمارنده ۱ شد.

---

## ۳. فرامین اجرا شده و نتایج

| ردیف | دستور | دایرکتوری | کد خروجی | نتیجه |
|---|---|---|---|---|
| ۱ | `cmd /c "cd /d d:\Android\AndroidStudioProjects\kmp-shop && gradlew.bat :core:config:capabilities:jvmTest :core:navigation:jvmTest :core:network:jvmTest :core:data:jvmTest"` | `D:/Android/AndroidStudioProjects/kmp-shop` | `0` | **BUILD SUCCESSFUL** (تمام تست‌های JVM هسته و آزمون ترنسپورت آموزش پاس شدند) |
| ۲ | `cmd /c "cd /d d:\Android\AndroidStudioProjects\kmp-shop && gradlew.bat :composeApp:compileKotlinJs :composeApp:compileKotlinJvm"` | `D:/Android/AndroidStudioProjects/kmp-shop` | `0` | **BUILD SUCCESSFUL** (کامپایل کامل کلاینت‌های JS و JVM) |
| ۳ | `cmd /c "cd /d d:\Android\AndroidStudioProjects\kmp-shop && gradlew.bat :androidApp:assembleDebug"` | `D:/Android/AndroidStudioProjects/kmp-shop` | `0` | **BUILD SUCCESSFUL** (بیلد کامل تمام فلاورهای Android debug) |

---

## ۴. فایل‌های تغییریافته

- `core/data/src/commonMain/kotlin/com/kazemieh/data/academy/repository/AcademyRepositoryImpl.kt`
- `core/data/src/commonMain/kotlin/com/kazemieh/data/academy/repository/AdminAcademyRepositoryImpl.kt`
- `core/data/src/commonMain/kotlin/com/kazemieh/data/courserequest/repository/CourseRequestRepositoryImpl.kt`
- `core/data/src/commonMain/kotlin/com/kazemieh/data/di/dataModule.kt`
- `core/navigation/src/commonMain/kotlin/com/kazemieh/navigation/FeatureRouteGuard.kt`
- `core/data/src/jvmTest/kotlin/com/kazemieh/data/academy/AcademyOperationalGuardTest.kt`
- `docs/evidence/P03-MANIFEST-CODE-025C/summary.md`
- `docs/tasks/P03-MANIFEST-CODE-025C.md`
- `docs/tasks/P03-MANIFEST-CODE-025D.md`
- `docs/tasks.md`
- `docs/MASTER_IMPLEMENTATION_CHECKLIST_FA.md`

---

## ۵. ارزیابی خطر و نتیجه نهایی

- تغییرات کاملاً در محدوده تعریف‌شده کارت (`core/data`, `core/navigation`, `core/config`) هستند.
- سازگاری به عقب سازنده‌ها با مقدار پیش‌فرض `guard: FeatureUseCaseGuard? = null` حفظ شده است.
- تمام ۵ معیار تست اختصاصی با خروجی قطعی ترنسپورت محقق شدند.
- وضعیت کارت: **`DONE`**.
