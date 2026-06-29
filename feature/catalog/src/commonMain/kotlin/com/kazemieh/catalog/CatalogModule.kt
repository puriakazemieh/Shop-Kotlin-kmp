package com.kazemieh.catalog

import com.kazemieh.domain.catalog.GetActiveCampaignUseCase
import com.kazemieh.domain.catalog.GetBannersUseCase
import com.kazemieh.domain.catalog.GetCategoriesUseCase
import com.kazemieh.domain.catalog.GetProductDetailUseCase
import com.kazemieh.domain.catalog.GetProductsUseCase
import com.kazemieh.domain.auth.IsUserLoggedInUseCase
import com.kazemieh.domain.favorite.ObserveFavoriteIdsUseCase
import com.kazemieh.domain.favorite.ToggleFavoriteUseCase
import com.kazemieh.domain.story.GetStoriesUseCase
import com.kazemieh.domain.story.MarkStoryAsSeenUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val catalogModule = module {
    // Catalog UseCases
    factory { GetCategoriesUseCase(get()) }
    factory { GetProductsUseCase(get()) }
    factory { GetActiveCampaignUseCase(get()) }
    factory { GetBannersUseCase(get()) }
    factory { GetProductDetailUseCase(get()) }
    factory { ToggleFavoriteUseCase(get()) }

    // Story UseCases
    factory { GetStoriesUseCase(get()) }
    factory { MarkStoryAsSeenUseCase(get()) }

    viewModel {
        ProductsOverviewViewModel(
            getProductsUseCase = get(),
            getActiveCampaignUseCase = get(),
            getBannersUseCase = get(),
            getCategoriesUseCase = get(),
            toggleFavoriteUseCase = get(),
            observeFavoriteIdsUseCase = get(),
            getStoriesUseCase = get(),
            markStoryAsSeenUseCase = get(),
            isUserLoggedInUseCase = get()
        )
    }

    factory { ObserveFavoriteIdsUseCase(get()) }

    viewModel {
        CategoriesViewModel(
            getCategoriesUseCase = get()
        )
    }

    viewModel {
        CategorySearchViewModel(
            getProductsUseCase = get(),
            observeFavoriteIdsUseCase = get(),
            toggleFavoriteUseCase = get(),
            isUserLoggedInUseCase = get()
        )
    }
}
