package com.kazemieh.catalog

import com.kazemieh.domain.catalog.GetCategoriesUseCase
import com.kazemieh.domain.catalog.GetProductDetailUseCase
import com.kazemieh.domain.catalog.GetProductsUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val catalogModule = module {
    // Catalog UseCases
    factory { GetCategoriesUseCase(get()) }
    factory { GetProductsUseCase(get()) }
    factory { GetProductDetailUseCase(get()) }

    viewModel {
        ProductsOverviewViewModel(
            getProductsUseCase = get()
        )
    }

    viewModel {
        CategoriesViewModel(
            getCategoriesUseCase = get()
        )
    }

    viewModel {
        CategorySearchViewModel(
            getProductsUseCase = get()
        )
    }
}
