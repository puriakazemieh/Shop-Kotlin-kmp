# Evidence Summary — P03-MANIFEST-CODE-025D

## عنوان تسک
اعمال guard عملیاتی کلینیک/نوبت (با ترنسپورت شمارنده واقعی در DI)

## مشخصات اجرا
- **Task ID:** `P03-MANIFEST-CODE-025D`
- **Branch:** `develop`
- **Date:** 2026-11-22
- **Executed By:** Codex / Carmilla

## شرح تغییرات مهندسی
1. **اعمال گارد در ریپازیتوری‌های کلاینت کلینیک:**
   - [ClinicRepositoryImpl.kt](file:///d:/Android/AndroidStudioProjects/kmp-shop/core/data/src/commonMain/kotlin/com/kazemieh/data/clinic/repository/ClinicRepositoryImpl.kt):
     - تزریق اختیاری `guard: FeatureUseCaseGuard? = null` در سازنده جهت حفظ backward compatibility.
     - پوشش متدهای پیام‌رسانی (`listMessages`, `sendMessage`, `messagingStatus`) با `guard.guardOrError("clinic.messaging")`.
     - پوشش تمام متدهای رزرواسیون، تراپیست، نوبت‌ها، مود، ژورنال و تطبیق با `guard.guardOrError("clinic.booking")`.
2. **اعمال گارد در ریپازیتوری ادمین کلینیک:**
   - [AdminClinicRepositoryImpl.kt](file:///d:/Android/AndroidStudioProjects/kmp-shop/core/data/src/commonMain/kotlin/com/kazemieh/data/clinic/repository/AdminClinicRepositoryImpl.kt):
     - تزریق اختیاری `guard: FeatureUseCaseGuard? = null` در سازنده.
     - پوشش متدهای پیام‌رسانی ادمین (`listMessagesWithPatient`, `sendMessageToPatient`) با `guard.guardOrError("clinic.messaging")`.
     - پوشش تمام متدهای مدیریتی تراپیست‌ها، اسلات‌ها، نوبت‌ها، یادداشت‌ها، پرونده و نشست‌ها با `guard.guardOrError("clinic.booking")`.
3. **پیکربندی Dependency Injection (Koin):**
   - [dataModule.kt](file:///d:/Android/AndroidStudioProjects/kmp-shop/core/data/src/commonMain/kotlin/com/kazemieh/data/di/dataModule.kt):
     - تزریق `getOrNull<FeatureUseCaseGuard>()` به `ClinicRepositoryImpl` و `AdminClinicRepositoryImpl`.
4. **طراحی و پیاده‌سازی آزمون ترنسپورت شمارنده واقعی:**
   - [ClinicOperationalGuardTest.kt](file:///d:/Android/AndroidStudioProjects/kmp-shop/core/data/src/jvmTest/kotlin/com/kazemieh/data/clinic/ClinicOperationalGuardTest.kt):
     - راه‌اندازی سرور HTTP محلی با `AtomicInteger` و اعتبارسنجی صفر ریکوئست در وضعیت خاموش، فوروارد درخواست‌های معتبر در وضعیت روشن، تفکیک سلسله‌مراتبی booking و messaging، فوروارد توکن احراز هویت و عدم امکان دور زدن اعتبارسنجی مالکیت سرور، و پویایی زمان اجرا (Dynamic runtime transition).

## دستورات اجرا شده و نتایج
| Command | CWD | Result | Exit Code |
|---|---|---|---|
| `.\gradlew.bat :core:data:jvmTest --tests "com.kazemieh.data.clinic.ClinicOperationalGuardTest"` | `d:\Android\AndroidStudioProjects\kmp-shop` | 5 passed | 0 |
| `.\gradlew.bat :core:config:capabilities:jvmTest :core:navigation:jvmTest :core:network:jvmTest` | `d:\Android\AndroidStudioProjects\kmp-shop` | All passed | 0 |
| `.\gradlew.bat :composeApp:compileKotlinJs :composeApp:compileKotlinJvm` | `d:\Android\AndroidStudioProjects\kmp-shop` | Build successful | 0 |
| `.\gradlew.bat :androidApp:assembleDebug` | `d:\Android\AndroidStudioProjects\kmp-shop` | Build successful | 0 |

## نتیجه نهایی
معیار اختصاصی کارت ("با transport شمارنده واقعی در DI، feature خاموش صفر request ایجاد کند؛ روشن مجاز request درست؛ auth/ownership با toggle دور زده نشود") با شواهد عینی، قطعی و آزمون‌های رفتاری تأیید شد.
وضعیت تسک: **DONE**
تسک مسدودشده بعدی (`P03-MANIFEST-CODE-025E`) اکنون آماده اجرا (READY) است.
