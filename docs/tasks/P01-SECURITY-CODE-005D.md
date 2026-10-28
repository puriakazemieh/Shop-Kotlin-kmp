# P01-SECURITY-CODE-005D — JVM token persistence policy

- Status: DONE
- وضعیت ممیزی ۲۰۲۶-۱۰-۲۸: factory JVM اکنون storage درون‌حافظه‌ای برمی‌گرداند؛ token پس از ایجاد instance جدید JVM باقی نمی‌ماند.
- Owner: AI
- Depends on: P01-SECURITY-CODE-005C
- Goal: refresh token persistent روی JVM ذخیره نشود و بسته‌شدن برنامه logout کند.

## Completion record

- Implemented at: 2026-10-28
- Changed files: `core/data/src/jvmMain/kotlin/com/kazemieh/data/local/TokenSettingsFactory.jvm.kt`؛ `core/data/src/jvmTest/kotlin/com/kazemieh/data/local/TokenSettingsSecurityTest.kt`.
- Verification: baseline `.\gradlew.bat :core:data:compileKotlinJvm --console=plain` exit 0؛ characterization پیش از اصلاح `:core:data:jvmTest --tests com.kazemieh.data.local.TokenSettingsSecurityTest` expected-fail (token پایدار بود)؛ پس از اصلاح همان تست همراه `:core:data:compileKotlinJs :core:data:compileKotlinJvm` exit 0.
- Evidence: `docs/evidence/P01-SECURITY-CODE-005D/verification-20261028.md`
- Manual QA: N/A؛ policy عمر نشست JVM با تست واحد سنجیده شد.
- Remaining control: ذخیرهٔ امن Android و iOS و آزمون دستی HTTPS Web/PWA در کارت‌های اختصاصی خود باز هستند.
- Final status: DONE
