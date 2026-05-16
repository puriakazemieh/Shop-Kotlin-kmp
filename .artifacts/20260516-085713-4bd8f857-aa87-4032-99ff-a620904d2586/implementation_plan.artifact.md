# Admin Panel Implementation Plan

The goal is to implement a fully functional Admin Panel for managing products, categories, variants, and inventory, based on the provided Spring Boot server code. The implementation will follow the MVI architecture and integrate with the existing Clean Architecture pattern (Domain, Data, UI layers).

## User Review Required

> [!IMPORTANT]
> - **ProductCategory Enum**: The existing code references a `ProductCategory` enum which seems to be missing or hardcoded. I will replace it with a dynamic `Category` model fetched from the server.
> - **Image Storage**: The current code uses Firebase Storage for images. The server API only accepts image URLs. I will maintain the Firebase Storage logic for uploading images before sending the URL to the server.
> - **Variants UI**: I will add a section to `ManageProductScreen` to manage variants (Color, Size, Price, SKU, Inventory).

## Proposed Changes

### Core: Network Layer
- **[NEW] [CatalogDtos.kt](file:///D:/Android/AndroidStudioProjects/kmp-shop/core/network/src/commonMain/kotlin/com/kazemieh/network/dto/CatalogDtos.kt)**: Define DTOs for public catalog (Categories, Sizes, Colors).
- **[NEW] [CatalogApi.kt](file:///D:/Android/AndroidStudioProjects/kmp-shop/core/network/src/commonMain/kotlin/com/kazemieh/network/CatalogApi.kt)**: Interface for public catalog endpoints.
- **[NEW] [CatalogApiImpl.kt](file:///D:/Android/AndroidStudioProjects/kmp-shop/core/network/src/commonMain/kotlin/com/kazemieh/network/CatalogApiImpl.kt)**: Implementation using Ktor.
- **[AdminDtos.kt](file:///D:/Android/AndroidStudioProjects/kmp-shop/core/network/src/commonMain/kotlin/com/kazemieh/network/dto/AdminDtos.kt)**: Ensure DTOs match server code (already done).

---

### Core: Domain Layer
- **[NEW] Admin Models**: Define `AdminProduct`, `AdminVariant`, `AdminProductDetail` in `core/domain/model/admin`.
- **[NEW] [AdminRepository.kt](file:///D:/Android/AndroidStudioProjects/kmp-shop/core/domain/src/commonMain/kotlin/com/kazemieh/domain/repository/AdminRepository.kt)**: Interface for admin operations.
- **[NEW] Use Cases**: Create use cases for all admin operations in `core/domain/usecase/admin`.
    - `GetAdminProductsUseCase`
    - `GetAdminProductDetailUseCase`
    - `CreateAdminProductUseCase`
    - `UpdateAdminProductUseCase`
    - `DeleteAdminProductUseCase`
    - `AddProductImageUseCase`
    - `DeleteProductImageUseCase`
    - `CreateProductVariantUseCase`
    - `UpdateProductVariantUseCase`
- **[NEW] [CatalogRepository.kt](file:///D:/Android/AndroidStudioProjects/kmp-shop/core/domain/src/commonMain/kotlin/com/kazemieh/domain/repository/CatalogRepository.kt)**: Interface for public catalog data.
- **[NEW] Catalog Use Cases**:
    - `GetCategoriesUseCase`
    - `GetSizesUseCase`
    - `GetColorsUseCase`

---

### Core: Data Layer
- **[NEW] [AdminRepositoryImpl.kt](file:///D:/Android/AndroidStudioProjects/kmp-shop/core/data/src/commonMain/kotlin/com/kazemieh/data/repository/AdminRepositoryImpl.kt)**: Implementation of `AdminRepository`.
- **[NEW] [CatalogRepositoryImpl.kt](file:///D:/Android/AndroidStudioProjects/kmp-shop/core/data/src/commonMain/kotlin/com/kazemieh/data/repository/CatalogRepositoryImpl.kt)**: Implementation of `CatalogRepository`.
- **[NEW] DataSources**: Remote data sources for Admin and Catalog.
- **[dataModule.kt](file:///D:/Android/AndroidStudioProjects/kmp-shop/core/data/src/commonMain/kotlin/com/kazemieh/data/di/dataModule.kt)**: Register new repositories and data sources.

---

### Feature: Admin Panel
- **[AdminPanelViewModel.kt](file:///D:/Android/AndroidStudioProjects/kmp-shop/feature/admin_panel/src/commonMain/kotlin/com/kazemieh/admin_panel/AdminPanelViewModel.kt)**:
    - Implement MVI with `AdminPanelState`, `AdminPanelIntent`, and `UiEvent`.
    - Use `GetAdminProductsUseCase` for fetching products.
- **[ManageProductViewModel.kt](file:///D:/Android/AndroidStudioProjects/kmp-shop/feature/admin_panel/src/commonMain/kotlin/com/kazemieh/admin_panel/manage_product/ManageProductViewModel.kt)**:
    - Implement MVI with `ManageProductState`, `ManageProductIntent`, and `UiEvent`.
    - Integrate all required use cases (Product, Images, Variants, Categories, etc.).
- **[ManageProductScreen.kt](file:///D:/Android/AndroidStudioProjects/kmp-shop/feature/admin_panel/src/commonMain/kotlin/com/kazemieh/admin_panel/manage_product/ManageProductScreen.kt)**:
    - Replace `ProductCategory` enum with dynamic categories.
    - Implement a BottomSheet for category selection.
    - Add UI for managing variants (Color/Size selection, Inventory management).

## Verification Plan

### Automated Tests
- No existing tests found for these modules. I will add unit tests for the new Use Cases.
- Command: `./gradlew :core:domain:test`

### Manual Verification
- Deploy to Android emulator.
- Navigate to Admin Panel.
- Test Product list, Search.
- Test "New Product" flow:
    - Title, Description, Price.
    - Category selection via BottomSheet.
    - Variant addition (Size, Color, SKU, Inventory).
    - Image upload.
- Test "Edit Product" flow:
    - Updating fields.
    - Deleting variants/images.
    - Hard delete product.
