package com.kazemieh.catalog

import com.kazemieh.domain.catalog.GetCategoriesUseCase
import com.kazemieh.domain.catalog.GetProductDetailUseCase
import com.kazemieh.domain.catalog.GetProductsUseCase
import com.kazemieh.domain.favorite.ToggleFavoriteUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val catalogModule = module {
    // Catalog UseCases
    factory { GetCategoriesUseCase(get()) }
    factory { GetProductsUseCase(get()) }
    factory { GetProductDetailUseCase(get()) }
    factory { ToggleFavoriteUseCase(get()) }

    viewModel {
        ProductsOverviewViewModel(
            getProductsUseCase = get(),
            toggleFavoriteUseCase = get()
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
