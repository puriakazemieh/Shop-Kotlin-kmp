package com.kazemieh.admin.products

import com.kazemieh.domain.admin.*
import com.kazemieh.domain.story.*
import com.kazemieh.domain.bundle.GetAdminBundlesUseCase
import com.kazemieh.domain.bundle.CreateBundleUseCase
import com.kazemieh.domain.bundle.UpdateBundleUseCase
import com.kazemieh.domain.bundle.DeleteBundleUseCase
import com.kazemieh.admin.story.AdminStoryViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val adminProductsModule = module {
    // Admin Product UseCases
    factory { GetAdminProductsUseCase(get()) }
    factory { GetAdminStatsUseCase(get()) }
    factory { GetAdminProductDetailUseCase(get()) }
    factory { CreateAdminProductUseCase(get()) }
    factory { UpdateAdminProductUseCase(get()) }
    factory { DeleteAdminProductUseCase(get()) }
    factory { AddProductVideoUseCase(get()) }
    factory { DeleteProductVideoUseCase(get()) }
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
    factory { GetAdminReviewsUseCase(get()) }
    factory { GetAdminQuestionsUseCase(get()) }

    factory { GetAdminOptionsUseCase(get()) }
    factory { CreateOptionTypeUseCase(get()) }
    factory { CreateOptionValueUseCase(get()) }
    factory { SetInventoryUseCase(get()) }

    // Bundles
    factory { GetAdminBundlesUseCase(get()) }
    factory { CreateBundleUseCase(get()) }
    factory { UpdateBundleUseCase(get()) }
    factory { DeleteBundleUseCase(get()) }

    // Admin Story UseCases
    factory { GetAdminStoriesUseCase(get()) }
    factory { CreateStoryUseCase(get()) }
    factory { UpdateStoryUseCase(get()) }
    factory { DeleteStoryUseCase(get()) }

    viewModel {
        AdminPanelViewModel(
            getAdminProductsUseCase = get(),
            getAdminStatsUseCase = get()
        )
    }

    viewModel {
        AdminStoryViewModel(
            getAdminStoriesUseCase = get(),
            createStoryUseCase = get(),
            updateStoryUseCase = get(),
            deleteStoryUseCase = get()
        )
    }

    viewModel {
        AdminInteractionsViewModel(
            getAdminReviewsUseCase = get(),
            getAdminQuestionsUseCase = get()
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
            addProductVideoUseCase = get(),
            deleteProductVideoUseCase = get(),
            getCategoriesUseCase = get(),
            getAdminOptionsUseCase = get(),
            createOptionTypeUseCase = get(),
            createOptionValueUseCase = get(),
            setInventoryUseCase = get(),
            savedStateHandle = get()
        )
    }

    viewModel {
        AdminBundlesViewModel(
            getAdminBundlesUseCase = get(),
            createBundleUseCase = get(),
            deleteBundleUseCase = get()
        )
    }
}
