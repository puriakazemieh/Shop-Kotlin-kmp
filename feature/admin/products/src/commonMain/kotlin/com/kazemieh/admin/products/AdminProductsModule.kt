package com.kazemieh.admin.products

import com.kazemieh.domain.admin.*
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val adminProductsModule = module {
    // Admin Product UseCases
    factory { GetAdminProductsUseCase(get()) }
    factory { GetAdminProductDetailUseCase(get()) }
    factory { CreateAdminProductUseCase(get()) }
    factory { UpdateAdminProductUseCase(get()) }
    factory { DeleteAdminProductUseCase(get()) }
    factory { CreateProductVariantUseCase(get()) }
    factory { UpdateProductVariantUseCase(get()) }
    factory { DeleteProductVariantUseCase(get()) }
    factory { CreateAdminCategoryUseCase(get()) }
    factory { DeleteAdminCategoryUseCase(get()) }
    factory { AddProductImageUseCase(get()) }
    factory { DeleteProductImageUseCase(get()) }
    factory { AdminCreateDiscountUseCase(get()) }
    factory { GetAdminDiscountsUseCase(get()) }
    factory { UpdateAdminDiscountUseCase(get()) }
    factory { DeleteAdminDiscountUseCase(get()) }

    factory { GetAdminOptionsUseCase(get()) }
    factory { CreateOptionTypeUseCase(get()) }
    factory { CreateOptionValueUseCase(get()) }
    factory { SetInventoryUseCase(get()) }

    viewModel {
        AdminPanelViewModel(
            getAdminProductsUseCase = get()
        )
    }

    viewModel {
        AdminDiscountsViewModel(
            getAdminDiscountsUseCase = get(),
            createDiscountUseCase = get(),
            updateAdminDiscountUseCase = get(),
            deleteAdminDiscountUseCase = get()
        )
    }

    viewModel {
        ManageProductViewModel(
            getAdminProductDetailUseCase = get(),
            createAdminProductUseCase = get(),
            updateAdminProductUseCase = get(),
            deleteAdminProductUseCase = get(),
            createProductVariantUseCase = get(),
            updateProductVariantUseCase = get(),
            deleteProductVariantUseCase = get(),
            createAdminCategoryUseCase = get(),
            deleteAdminCategoryUseCase = get(),
            addProductImageUseCase = get(),
            deleteProductImageUseCase = get(),
            getCategoriesUseCase = get(),
            getAdminOptionsUseCase = get(),
            createOptionTypeUseCase = get(),
            createOptionValueUseCase = get(),
            setInventoryUseCase = get(),
            savedStateHandle = get()
        )
    }
}
