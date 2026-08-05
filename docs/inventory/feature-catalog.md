# Feature Catalog and Consumer Inventory

> Generated: 2026-08-05T12:15:00.000000000+03:30
> Task ID: P00-MANIFEST-DISC-005

This document maps all identified features to their UI routes, DI modules, and network APIs.

## 1. Core & Infrastructure
| Area | Feature ID (Prop.) | DI Module | Notes |
|---|---|---|---|
| Main/Dashboard | `main` | `mainModule` | Entry point, Banners, Stories |
| Navigation | `core:navigation` | N/A | AppNavHost (Central Registry) |
| Identity/Auth | `auth` | `authModule` | Login, OTP, Register |
| Settings | `settings` | `settingsModule` | Language, Theme |

## 2. Commerce (Shop)
| Area | Feature ID | UI Routes (Screen) | APIs / Network |
|---|---|---|---|
| Catalog | `commerce.catalog` | `ProductDetail`, `CategorySearch` | `CatalogApi`, `ProductApi` |
| Cart | `commerce.cart` | `HomeGraph(showCart=true)` | `CartApi` |
| Checkout | `commerce.checkout` | `Checkout`, `PaymentCompleted` | `PaymentApi` |
| Orders | `commerce.orders` | `MyOrders`, `OrderDetail`, `OrderTracking` | `OrderApi` |
| Returns | `commerce.returns` | `ReturnRequest` | `OrderApi#requestReturn` |
| Comparison | `commerce.comparison`| `Comparison` | Local Logic / Product API |
| Bundles | `commerce.bundles` | `BundleList`, `BundleDetail` | `BundleApi` |
| Assistant | `commerce.assistant` | `ShoppingAssistant` | N/A (Client-side/LLM?) |

## 3. Verticals (Add-ons)
| Area | Feature ID | UI Routes (Screen) | APIs / Network |
|---|---|---|---|
| **Academy (LMS)** | `academy` | `CourseCatalog`, `MyCourses`, `CourseDetail`, `CourseLearn` | `AcademyApi`, `CourseRequestApi` |
| Academy Quiz | `academy.quiz` | `CourseQuiz`, `LessonQuiz` | `AcademyApi#submitQuiz` |
| Academy Cert. | `academy.cert` | `Certificates`, `CertificateVerify` | `AcademyApi#getCertificates` |
| **Clinic** | `clinic` | `TherapistCatalog`, `TherapistDetail`, `MyAppointments` | `ClinicApi` |
| Clinic File | `clinic.file` | `MessagingThread`, `Homework`, `Journal` | `ClinicApi#getPatientFile` |
| **Psych Tests** | `psych` | `PsychTestCatalog`, `TakeTest` | `PsychTestApi` |

## 4. User Account & Social
| Area | Feature ID | UI Routes (Screen) | APIs / Network |
|---|---|---|---|
| Profile | `profile` | `Profile` | `ProfileApi` |
| Wallet | `wallet` | `Wallet` | `WalletApi` |
| Club | `social.club` | `CustomerClub` | `ProfileApi` |
| Referral | `social.referral` | `Referral` | `ReferralApi` |
| Membership | `social.membership` | `Membership` | `MembershipApi` |
| Blog | `content.blog` | `BlogList`, `BlogDetail` | `BlogApi` |
| Stories | `social.stories` | Part of Main | `StoryApi` |

## 5. Admin Panel
| Area | Feature ID | UI Routes (Screen) | APIs / Network |
|---|---|---|---|
| Admin Core | `admin` | `AdminPanel` | `AdminApi` |
| Admin Shop | `admin.shop` | `ManageProduct`, `ManageOrders`, `ManageDiscounts` | `AdminApi` |
| Admin Vertical | `admin.verticals`| `ManageAcademy`, `ManageClinic`, `ManagePsychTest` | `AdminApi` |
| Admin Wallet | `admin.wallet` | `ManageWallets`, `ManageWithdrawals` | `AdminApi` |

---

## 6. Consumer Mapping (Path/Line)

### Navigation Registry
- **File**: `core/navigation/src/commonMain/kotlin/com/kazemieh/navigation/AppNavigation.kt`
- **Feature Check (Current)**: Most routes are currently unguarded or rely on visibility flags in `BrandConfig`.

### DI Module Handoff
- **File**: `composeApp/src/commonMain/kotlin/com/kazemieh/shop/App.kt`
- **Modules List**: lines 78-100 (Hardcoded list).

### Network API Implementation
- **Base Root**: `core/network/src/commonMain/kotlin/com/kazemieh/network/`
- **Interface Owner**: Each feature module owns its `Repository`, but API implementations are grouped in `core:network`.

---

## 7. Blocker & Unknowns
1. **Ownership**: Many feature modules in `feature/` directy import types from other feature modules. This violates the intended boundary for the Manifest.
2. **Dynamic DI**: Current `initKoin` loads ALL modules. To support the Manifest, this must be changed to load only "enabled" modules or use a lazy loading strategy.
3. **Route Guard**: There is no central mechanism to "disable" a route based on a manifest.
