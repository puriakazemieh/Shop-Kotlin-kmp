package com.kazemieh.admin_panel

import com.kazemieh.admin_panel.manage_options.ManageOptionsViewModel
import com.kazemieh.admin_panel.manage_order.AdminOrderViewModel
import com.kazemieh.admin_panel.manage_product.ManageProductViewModel
import com.kazemieh.admin_panel.manage_product.PhotoPicker
import com.kazemieh.domain.usecase.admin.*
import com.kazemieh.domain.usecase.catalog.GetCategoriesUseCase
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
            deleteAdminCategoryUseCase = get(),
            addProductImageUseCase = get(),
            deleteProductImageUseCase = get(),
            getCategoriesUseCase = get(),
            getAdminOptionsUseCase = get(),
            savedStateHandle = get()
        )
    }

    viewModel {
        AdminOrderViewModel(
            listAdminOrdersUseCase = get(),
            getAdminOrderDetailUseCase = get(),
            updateAdminOrderStatusUseCase = get()
        )
    }

    viewModel {
        ManageOptionsViewModel(
            getAdminOptionsUseCase = get(),
            createOptionTypeUseCase = get(),
            updateOptionTypeUseCase = get(),
            deleteOptionTypeUseCase = get(),
            createOptionValueUseCase = get(),
            updateOptionValueUseCase = get(),
            deleteOptionValueUseCase = get()
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
    factory { DeleteAdminCategoryUseCase(get()) }
    factory { AddProductImageUseCase(get()) }
    factory { DeleteProductImageUseCase(get()) }

    factory { GetAdminOptionsUseCase(get()) }
    factory { CreateOptionTypeUseCase(get()) }
    factory { UpdateOptionTypeUseCase(get()) }
    factory { DeleteOptionTypeUseCase(get()) }
    factory { CreateOptionValueUseCase(get()) }
    factory { UpdateOptionValueUseCase(get()) }
    factory { DeleteOptionValueUseCase(get()) }

    factory { ListAdminOrdersUseCase(get()) }
    factory { GetAdminOrderDetailUseCase(get()) }
    factory { UpdateAdminOrderStatusUseCase(get()) }

    factory { GetCategoriesUseCase(get()) }

    single { PhotoPicker() }
}
