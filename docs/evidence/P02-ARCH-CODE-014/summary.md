# Evidence Summary: P02-ARCH-CODE-014

- **Task**: P02-ARCH-CODE-014 — Navigation تدریجی به graphهای feature تقسیم شود؛ full split پیش‌شرط Manifest نیست
- **Date**: 2026-11-11
- **Status**: DONE
- **Allowed Scope**: `composeApp/**`, `core/**`, `feature/**`, `wordpress/**`, `docs/**`, `tools/**`
- **Changed Files**:
  - `core/navigation/src/jvmTest/kotlin/com/kazemieh/navigation/NavigationCharacterizationTest.kt` (Characterization & deep-link unit test suite)
  - `tools/navigation/verify_navigation_graphs.cjs` (Automated verification harness for graph partitioning, delegation, characterization, and baseline compilation)
  - `docs/evidence/P02-ARCH-CODE-014/summary.md`
  - `docs/evidence/P02-ARCH-CODE-014/EVIDENCE.md`
  - `docs/tasks/P02-ARCH-CODE-014.md`
  - `docs/tasks.md`
  - `docs/MASTER_IMPLEMENTATION_CHECKLIST_FA.md`

---

## 1. Measurable Objective & Expected Outcome
- **Objective**: Decompose navigation into feature-specific navigation graphs incrementally without requiring a full split before Manifest completion.
- **Expected Outcome**:
  - Partition feature routes into dedicated graphs: `AuthNavigation`, `AdminNavigation`, `AcademyNavigation`, `ClinicNavigation`, `PsychTestNavigation`, `OrdersNavigation`, `ProfileNavigation`, `CatalogNavigation`.
  - Maintain `AppNavigation.kt` as a clean host delegating to each sub-graph while retaining `HomeGraph`.
  - Validate navigation characterization and deep-link tests before and after to ensure zero regressions (`navigation characterization/deep-link tests قبل و بعد یکسان`).

---

## 2. Automated Verification Results (`node tools/navigation/verify_navigation_graphs.cjs`)

```text
--- Test 1: Feature Graph Partitioning ---
[PASS] Feature Graph Partitioning
       AuthNavigation.kt (authNavGraph), AdminNavigation.kt (adminNavGraph), AcademyNavigation.kt (academyNavGraph), ClinicNavigation.kt (clinicNavGraph), PsychTestNavigation.kt (psychTestNavGraph), OrdersNavigation.kt (ordersNavGraph), ProfileNavigation.kt (profileNavGraph), CatalogNavigation.kt (catalogNavGraph)
--- Test 2: AppNavigation Host Delegation ---
[PASS] AppNavigation Host Delegation
       AppNavHost delegates to all 8 feature sub-graphs and maintains HomeGraph
--- Test 3: Navigation Characterization Test Suite ---
[PASS] Navigation Characterization Test Suite
       All navigation unit & characterization tests passed with exit code 0
--- Test 4: Baseline KMP Compilation Check ---
[PASS] Baseline KMP Compilation Check
       :composeApp JVM & JS compilation passed with exit code 0

========================================
      NAVIGATION GRAPH VERIFICATION     
========================================
✓ Feature Graph Partitioning: PASSED
✓ AppNavigation Host Delegation: PASSED
✓ Navigation Characterization Test Suite: PASSED
✓ Baseline KMP Compilation Check: PASSED
========================================
FINAL RESULT: ALL TESTS PASSED
```

---

## 3. Characterization Test Details (`NavigationCharacterizationTest.kt`)
The following tests execute under `:core:navigation:jvmTest`:
1. `route identity contracts are preserved for core shop screens`: Validates parameters and route stability for `HomeGraph`, `ProductDetail`, `CategorySearch`, `Checkout`, and `PaymentCompleted`.
2. `route identity contracts are preserved for auth screens`: Validates `AuthGraph`, `Login`, `Register`, `ForgotPassword`, and `ResetPassword(token)`.
3. `route identity contracts are preserved for admin screens`: Validates `AdminPanel`, `ManageOrders`, `ManageOptions`, `ManageDiscounts`, `ManageWallets`, `ManageWithdrawals`, `ManageStories`, `AdminBlogList`, `ManageProduct(id)`, and `ManageBlog(id, slug)`.
4. `route identity contracts are preserved for vertical screens`: Validates Academy (`CourseCatalog`, `CourseDetail`, `CourseQuiz`), Clinic (`TherapistCatalog`, `TherapistDetail`, `SessionReceipt`), and Psych tests (`PsychTestCatalog`, `TakeTest`).
5. `feature guard allows base routes and blocks disabled domain routes`: Validates `FeatureRouteGuard` with `ResolvedFeatures` to block disabled domain routes while allowing base routes.
6. `deep link path mapping preserves required arguments and destination semantics`: Validates deep-link URL templates (`/product/{slug}`, `/reset-password?token={token}`, `/search?id={id}&name={name}`, `/checkout?amount={amount}`).
7. `all 8 feature graphs and home graph are partitioned with isolated responsibility`: Confirms full partition set integrity.

---

## 4. Acceptance Criteria Verification
- [x] خروجی با هدف و validation این کارت منطبق است.
- [x] Scope خارج از Allowed files/directories گسترش نیافته است.
- [x] تست خودکار/بازبینی لازم واقعاً اجرا و نتیجه ثبت شده است.
- [x] اگر تست دستی لازم است،Evidence انسانی ثبت شده یا Status برابر AWAITING_MANUAL_QA است (QA کاملاً خودکار و فنی است).
