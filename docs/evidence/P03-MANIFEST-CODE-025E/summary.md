# Evidence Summary — P03-MANIFEST-CODE-025E

## عنوان تسک
اعمال guard عملیاتی تست روان‌شناسی (با ترنسپورت شمارنده واقعی در DI)

## مشخصات اجرا
- **Task ID:** `P03-MANIFEST-CODE-025E`
- **Branch:** `develop`
- **Date:** 2026-11-22
- **Executed By:** Codex / Carmilla

## شرح تغییرات مهندسی
1. **اعمال گارد در ریپازیتوری‌های کلاینت تست روان‌شناسی:**
   - [PsychTestRepositoryImpl.kt](file:///d:/Android/AndroidStudioProjects/kmp-shop/core/data/src/commonMain/kotlin/com/kazemieh/data/psychtest/repository/PsychTestRepositoryImpl.kt):
     - تزریق اختیاری `guard: FeatureUseCaseGuard? = null` در سازنده جهت حفظ backward compatibility.
     - پوشش تمام متدهای کلاینت (`getTests`, `getTest`, `getMyTests`, `getUserTestQuestions`, `submit`) با `guard.guardOrError("psych.tests")`.
2. **اعمال گارد در ریپازیتوری ادمین تست روان‌شناسی:**
   - [AdminPsychTestRepositoryImpl.kt](file:///d:/Android/AndroidStudioProjects/kmp-shop/core/data/src/commonMain/kotlin/com/kazemieh/data/psychtest/repository/AdminPsychTestRepositoryImpl.kt):
     - تزریق اختیاری `guard: FeatureUseCaseGuard? = null` در سازنده.
     - پوشش تمام متدهای ادمین (`list`, `detail`, `create`, `update`, `delete`, `pendingInterpretations`, `interpret`) با `guard.guardOrError("psych.tests")`.
3. **به‌روزرسانی پلاگین شبکه در لایه ترنسپورت:**
   - [HttpClientFactory.kt](file:///d:/Android/AndroidStudioProjects/kmp-shop/core/network/src/commonMain/kotlin/com/kazemieh/network/common/HttpClientFactory.kt):
     - ارتقای تطبیق مسیر برای پوشش مسیرهای `/api/psych*` و `/api/my-psych-tests` ذیل قابلیت `"psych.tests"`.
4. **پیکربندی Dependency Injection (Koin):**
   - [dataModule.kt](file:///d:/Android/AndroidStudioProjects/kmp-shop/core/data/src/commonMain/kotlin/com/kazemieh/data/di/dataModule.kt):
     - تزریق `getOrNull<FeatureUseCaseGuard>()` به `PsychTestRepositoryImpl` و `AdminPsychTestRepositoryImpl`.
5. **طراحی و پیاده‌سازی آزمون ترنسپورت شمارنده واقعی:**
   - [PsychTestOperationalGuardTest.kt](file:///d:/Android/AndroidStudioProjects/kmp-shop/core/data/src/jvmTest/kotlin/com/kazemieh/data/psychtest/PsychTestOperationalGuardTest.kt):
     - راه‌اندازی سرور HTTP محلی با `AtomicInteger` و اعتبارسنجی صفر ریکوئست در وضعیت خاموش (fail-closed با ۴۰۳)، فوروارد درخواست‌های معتبر در وضعیت روشن، فوروارد توکن احراز هویت و ممانعت از دور زدن اعتبارسنجی مالکیت سرور، و پویایی زمان اجرا (Dynamic runtime transition).

## دستورات اجرا شده و نتایج
| Command | CWD | Result | Exit Code |
|---|---|---|---|
| `.\gradlew.bat :core:data:jvmTest --tests "com.kazemieh.data.psychtest.PsychTestOperationalGuardTest"` | `d:\Android\AndroidStudioProjects\kmp-shop` | 4 passed | 0 |
| `.\gradlew.bat :core:config:capabilities:jvmTest :core:navigation:jvmTest :core:network:jvmTest` | `d:\Android\AndroidStudioProjects\kmp-shop` | All passed | 0 |
| `.\gradlew.bat :composeApp:compileKotlinJs :composeApp:compileKotlinJvm` | `d:\Android\AndroidStudioProjects\kmp-shop` | Build successful | 0 |
| `.\gradlew.bat :androidApp:assembleDebug` | `d:\Android\AndroidStudioProjects\kmp-shop` | Build successful | 0 |

## نتیجه نهایی
معیار اختصاصی کارت ("با transport شمارنده واقعی در DI، feature خاموش صفر request ایجاد کند؛ روشن مجاز request درست؛ auth/ownership با toggle دور زده نشود") با شواهد عینی، قطعی و آزمون‌های رفتاری تأیید شد.
وضعیت تسک: **DONE**
تسک بعدی در صف فاز ۰۳ (`P03-QA-AUTO-026`) اکنون آماده اجرا (READY) است.
