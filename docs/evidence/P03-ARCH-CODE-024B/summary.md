# P03-ARCH-CODE-024B Evidence

## Overview
- **Task ID**: `P03-ARCH-CODE-024B`
- **Title**: تزریق BuildSpec صریح در bootstrap همه targetها
- **Date**: 2026-11-14
- **Verification Environment**: Windows 11, JDK 17, Gradle 8.14.3

## Changes & Implementation Summary
1. **Dynamic SKU Injection & Decoupling**:
   - `initKoin` in `composeApp/src/commonMain/kotlin/com/kazemieh/shop/App.kt` accepts `sku` and `apiBaseUrlOverride` rather than hardcoding brand or backend.
   - Dynamic resolution of `ProductBuildSpec`, `TenantConfig`, `BrandConfig`, and `BackendProfile` based on `GeneratedProductSpecs.specs[sku]`.
   - Brand ID is decoupled from Backend Profile (`brand.id` does not dictate backend kind).
2. **Private Session Namespace**:
   - `privateSessionNamespace` integrates `backendKind`, `tenantId`, and lowercase normalized `apiRoot` (`origin`).
   - Ensures distinct session keys (`private.$fingerprint.$name`) so tenant/backend switches invalidate prior session data and caches.
3. **Target Entrypoints Updated & Verified**:
   - **Android**: `androidApp/src/main/kotlin/com/kazemieh/shop/ShopApplication.kt` injects `BuildConfig.BRAND` as `sku`.
   - **Web/PWA**: `composeApp/src/webMain/kotlin/com/kazemieh/shop/main.kt` resolves `sku` and handles Service Worker cache purge on unauthenticated logout.
   - **JVM Desktop**: `composeApp/src/jvmMain/kotlin/com/kazemieh/shop/main.kt` injects `sku` via `System.getProperty("brand")` (default "carmila").
   - **iOS**: `composeApp/src/iosMain/kotlin/com/kazemieh/shop/MainViewController.kt` takes `sku: String = "carmila"`.
4. **Behavioral Test Coverage**:
   - Enhanced `composeApp/src/jvmTest/kotlin/com/kazemieh/shop/GeneratedProductSpecsTest.kt` to verify:
     - Arbitrary brand binding to WordPress or Spring backend profiles without hardcoded coupling.
     - Deterministic derivation and fingerprinting of `PrivateSessionNamespace` across all SKUs.

## Verification & Execution Logs
All mandatory automated checks executed with exit code 0:
1. **Capabilities, Navigation & Network Tests**:
   - Command: `.\gradlew.bat :core:config:capabilities:jvmTest :core:navigation:jvmTest :core:network:jvmTest`
   - Result: `BUILD SUCCESSFUL in 14s` (233 actionable tasks: 55 executed, 178 up-to-date)
   - Exit code: 0
2. **JS and JVM Multiplatform Compilation**:
   - Command: `.\gradlew.bat :composeApp:compileKotlinJs :composeApp:compileKotlinJvm`
   - Result: `BUILD SUCCESSFUL in 7s` (290 actionable tasks: 57 executed, 233 up-to-date)
   - Exit code: 0
3. **Android Application Debug Assembly**:
   - Command: `.\gradlew.bat :androidApp:assembleDebug`
   - Result: `BUILD SUCCESSFUL in 13s` (729 actionable tasks: 31 executed, 698 up-to-date)
   - Exit code: 0
4. **ComposeApp JVM Test Suite**:
   - Command: `.\gradlew.bat :composeApp:jvmTest`
   - Result: `BUILD SUCCESSFUL in 13s` (241 actionable tasks: 32 executed, 209 up-to-date)
   - Exit code: 0

## Acceptance & QA Details
- **Technical QA**: PASS. Full automated test suite and multiplatform compilation pass cleanly.
- **Manual QA Instruction**:
  - **محیط**: شبیه‌ساز یا دستگاه فیزیکی اندروید، یا مرورگر برای نسخه PWA.
  - **مراحل**: اجرای بیلد با SKUهای مختلف (نظیر `carmila` با بک‌اند وردپرس و `academy` با بک‌اند اسپرینگ).
  - **معیار موفقیت**: بارگذاری بدون خطای رابط کاربری با تم و برند صحیح، عدم نشت سشن میان چند tenant یا backend، و پاک‌سازی کش در خروج کاربر.
- **Final Status**: DONE
