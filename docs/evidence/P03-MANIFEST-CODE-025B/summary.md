# Evidence: Operational Guards for Commerce (P03-MANIFEST-CODE-025B)

## Date & Status
- Date: 2026-11-15
- Task: `P03-MANIFEST-CODE-025B`
- Status: **DONE**
- Branch: `develop`
- Authority: AI Technical Verification (all automated tests & counting transport in DI verified with reproducible test report and exit code 0)

## Objective & Requirements
- Apply operational feature guards across the commerce domain (`cart`, `orders`, `catalog`, `wallet`, `bundle`, `return requests`, `recurring orders`).
- Strict requirement: With a real counting transport in DI:
  - When `commerce.core` is disabled: exactly **0 requests** sent to transport; fails closed returning `AppResult.Error(code = 403)`.
  - When enabled: valid requests reach transport and increment request counter.
  - Sub-features (`wallet`) adhere to feature tree (`wallet` is child of `commerce.core`).
  - Auth and ownership cannot be bypassed via feature toggles: Bearer tokens are properly transmitted and server-side 401/403 ownership checks are enforced.
  - Dynamic runtime transitions (from disabled to enabled) dynamically open transport without rebuild.

## Changes Implemented
1. **Feature Guard Extensions (`core/data/src/commonMain/kotlin/com/kazemieh/data/common/FeatureGuardExtensions.kt`)**:
   - `FeatureUseCaseGuard?.guardOrError(...)`: blocks execution and returns `AppResult.Error(disabledMessage, 403)` when feature is disabled.
   - `FeatureUseCaseGuard?.guardFlow(...)`: intercepts cold flows, emitting `AppResult.Error(disabledMessage, 403)` immediately without executing the underlying network/data source flow.
2. **Repository Guarding**:
   - `CartRepositoryImpl`: all operations (`getCart`, `addItem`, `updateQty`, `remove`, `clear`, `setVariantQty`, `adjustVariantQty`, `moveToSaveForLater`, `moveToCart`, `applyDiscount`, `removeDiscount`) guarded with `"commerce.core"`.
   - `OrderRepositoryImpl`: all operations (`getMyOrders`, `getOrder`, `createOrder`, `cancelOrder`, `trackOrder`, `reorder`) guarded with `"commerce.core"`.
   - `CatalogRepositoryImpl`: catalog queries (`getCategories`, `getProducts`, `getProductDetail`, `getActiveCampaign`, `getBanners`, `requestBackInStock`, `getFrequentlyBoughtTogether`, `subscribeToPriceAlert`) guarded with `"commerce.core"`.
   - `WalletRepositoryImpl`: wallet operations (`getBalance`, `getTransactions`, `topUp`, `withdraw`, `searchWalletUsers`, `adjustWalletBalance`, `listWithdrawals`, `processWithdrawal`) guarded with `"wallet"`.
   - `ReturnRequestRepositoryImpl`: return operations (`create`, `listMine`, `adminList`, `adminUpdateStatus`) guarded with `"commerce.core"`.
   - `RecurringOrderRepositoryImpl`: recurring order operations (`create`, `listMine`, `cancel`) guarded with `"commerce.core"`.
   - `BundleRepositoryImpl` & `AdminBundleRepositoryImpl`: bundle operations guarded with `"commerce.core"`.
   - All constructors provide `guard: FeatureUseCaseGuard? = null` for 100% backward compatibility.
3. **DI Wiring (`core/data/src/commonMain/kotlin/com/kazemieh/data/di/dataModule.kt`)**:
   - Injected `getOrNull<FeatureUseCaseGuard>()` into `WalletRepository`, `CatalogRepository`, `CartRepository`, `OrderRepository`, `ReturnRequestRepository`, `RecurringOrderRepository`, `BundleRepository`, `AdminBundleRepository`.
4. **Network Module Compatibility (`core/network/src/commonMain/kotlin/com/kazemieh/network/di/networkModule.kt`)**:
   - `HttpClientFactory.create(get(), getOrNull())` allowing optional `EffectiveFeatureStore` injection.
5. **Navigation Route Guard Fix (`core/navigation/src/commonMain/kotlin/com/kazemieh/navigation/FeatureRouteGuard.kt`)**:
   - Corrected `featureIdFor` to distinguish `HomeGraph(showCart = false)` (always allowed Home) from `HomeGraph(showCart = true)` (guarded by `commerce.core`).
6. **Automated Counting Transport Tests (`core/data/src/jvmTest/kotlin/com/kazemieh/data/commerce/CommerceOperationalGuardTest.kt`)**:
   - Real JDK `HttpServer` with `AtomicInteger` request counter and header capture.
   - Comprehensive test cases:
     - `when commerce core is disabled in DI then repositories emit 0 requests to transport and fail closed` (PASS)
     - `when commerce core is enabled then valid requests reach transport and increment request counter` (PASS)
     - `when wallet is disabled while commerce core is enabled then wallet produces 0 requests` (PASS)
     - `auth and ownership cannot be bypassed by enabling feature toggle` (PASS)
     - `runtime transition from disabled to enabled dynamically opens transport` (PASS)

## Verification Commands & Results
| Command | Result |
| --- | --- |
| `./gradlew.bat :core:config:capabilities:jvmTest :core:navigation:jvmTest :core:network:jvmTest :core:data:jvmTest` | **BUILD SUCCESSFUL** (exit code 0) |
| `./gradlew.bat :composeApp:compileKotlinJs :composeApp:compileKotlinJvm` | **BUILD SUCCESSFUL** (exit code 0) |
| `./gradlew.bat :androidApp:assembleDebug` | **BUILD SUCCESSFUL** (exit code 0) |

## QA Verification Scenario
- **کجا**: محیط تست یا اپلیکیشن، بخش فروشگاه و کیف‌پول
- **چگونه**: قابلیت `commerce.core` یا `wallet` در مانیفست خاموش شود؛ متدهای سبد خرید، سفارشات و کیف‌پول فراخوانی شوند.
- **نتیجه موفق**: در حالت خاموش، دقیقاً صفر درخواست به ترنسپورت شبکه ارسال شده و خطای ۴۰۳ بلافاصله صادر می‌شود. با فعال‌سازی قابلیت، درخواست معتبر با هدر احراز هویت ارسال شده و کنترل مالکیت توسط سرور محفوظ است.
