package com.kazemieh.catalog

import com.kazemieh.catalog.assistant.ShoppingAssistantViewModel
import com.kazemieh.catalog.bundle.BundleDetailViewModel
import com.kazemieh.catalog.bundle.BundleListViewModel
import com.kazemieh.domain.bundle.GetBundleDetailUseCase
import com.kazemieh.domain.bundle.GetBundlesUseCase
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
import com.kazemieh.domain.recentlyviewed.GetRecentlyViewedUseCase
import com.kazemieh.domain.settings.GetRecentSearchesUseCase
import com.kazemieh.domain.settings.AddRecentSearchUseCase
import com.kazemieh.domain.settings.ClearRecentSearchesUseCase
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

    // Recently viewed
    factory { GetRecentlyViewedUseCase(get()) }

    viewModel { ShoppingAssistantViewModel(getProductsUseCase = get()) }

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
            getBlogsUseCase = get(),
            getRecentlyViewedUseCase = get(),
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

    // Standalone search
    factory { GetRecentSearchesUseCase(get()) }
    factory { AddRecentSearchUseCase(get()) }
    factory { ClearRecentSearchesUseCase(get()) }

    viewModel {
        SearchViewModel(
            getProductsUseCase = get(),
            observeFavoriteIdsUseCase = get(),
            toggleFavoriteUseCase = get(),
            isUserLoggedInUseCase = get(),
            getRecentSearchesUseCase = get(),
            addRecentSearchUseCase = get(),
            clearRecentSearchesUseCase = get()
        )
    }

    // Bundles
    factory { GetBundlesUseCase(get()) }
    factory { GetBundleDetailUseCase(get()) }
    viewModel { BundleListViewModel(get()) }
    viewModel { BundleDetailViewModel(get()) }
}
