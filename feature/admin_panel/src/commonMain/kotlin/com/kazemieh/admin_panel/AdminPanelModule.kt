package com.kazemieh.admin_panel

import com.kazemieh.admin_panel.manage_product.ManageProductViewModel
import com.kazemieh.admin_panel.manage_product.PhotoPicker
import com.kazemieh.domain.usecase.admin.*
import com.kazemieh.domain.usecase.catalog.GetCategoriesUseCase
import com.kazemieh.domain.usecase.catalog.GetSizesUseCase
import com.kazemieh.domain.usecase.catalog.GetColorsUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val adminPanelModule = module {

    // ViewModels
    viewModel {
        AdminPanelViewModel(
            getAdminProductsUseCase = get()
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
            createSizeUseCase = get(),
            updateSizeUseCase = get(),
            deleteSizeUseCase = get(),
            createColorUseCase = get(),
            updateColorUseCase = get(),
            deleteColorUseCase = get(),
            addProductImageUseCase = get(),
            getCategoriesUseCase = get(),
            getAdminSizesUseCase = get(),
            getAdminColorsUseCase = get(),
            savedStateHandle = get()
        )
    }

    // UseCases
    factory { GetAdminProductsUseCase(get()) }
    factory { GetAdminProductDetailUseCase(get()) }
    factory { CreateAdminProductUseCase(get()) }
    factory { UpdateAdminProductUseCase(get()) }
    factory { DeleteAdminProductUseCase(get()) }
    factory { CreateProductVariantUseCase(get()) }
    factory { UpdateProductVariantUseCase(get()) }
    factory { DeleteProductVariantUseCase(get()) }
    factory { CreateAdminCategoryUseCase(get()) }
    factory { CreateSizeUseCase(get()) }
    factory { CreateColorUseCase(get()) }
    factory { GetAdminSizesUseCase(get()) }
    factory { UpdateSizeUseCase(get()) }
    factory { DeleteSizeUseCase(get()) }
    factory { GetAdminColorsUseCase(get()) }
    factory { UpdateColorUseCase(get()) }
    factory { DeleteColorUseCase(get()) }
    factory { AddProductImageUseCase(get()) }

    factory { GetCategoriesUseCase(get()) }
    factory { GetSizesUseCase(get()) }
    factory { GetColorsUseCase(get()) }

    single { PhotoPicker() }
}
