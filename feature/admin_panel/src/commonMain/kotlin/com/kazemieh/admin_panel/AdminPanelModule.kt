package com.kazemieh.admin_panel

import com.kazemieh.admin_panel.manage_product.ManageProductViewModel
import com.kazemieh.admin_panel.manage_product.PhotoPicker
import com.kazemieh.domain.usecase.admin.*
import com.kazemieh.domain.usecase.catalog.GetCategoriesUseCase
import com.kazemieh.domain.usecase.catalog.GetColorsUseCase
import com.kazemieh.domain.usecase.catalog.GetSizesUseCase
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
            createAdminCategoryUseCase = get(),
            createSizeUseCase = get(),
            createColorUseCase = get(),
            uploadImageUseCase = get(),
            addProductImageUseCase = get(),
            getCategoriesUseCase = get(),
            getSizesUseCase = get(),
            getColorsUseCase = get(),
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
    factory { CreateAdminCategoryUseCase(get()) }
    factory { CreateSizeUseCase(get()) }
    factory { CreateColorUseCase(get()) }
    factory { UploadImageUseCase(get()) }
    factory { AddProductImageUseCase(get()) }

    factory { GetCategoriesUseCase(get()) }
    factory { GetSizesUseCase(get()) }
    factory { GetColorsUseCase(get()) }

    single { PhotoPicker() }
}
