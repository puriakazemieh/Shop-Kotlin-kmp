package com.kazemieh.details.di

import com.kazemieh.details.DetailsViewModel
import com.kazemieh.domain.auth.IsUserLoggedInUseCase
import com.kazemieh.domain.cart.AddToCartUseCase
import com.kazemieh.domain.catalog.DeleteQuestionUseCase
import com.kazemieh.domain.catalog.DeleteReviewUseCase
import com.kazemieh.domain.catalog.GetProductDetailUseCase
import com.kazemieh.domain.catalog.GetProductsUseCase
import com.kazemieh.domain.catalog.GetQuestionsUseCase
import com.kazemieh.domain.catalog.GetReviewsUseCase
import com.kazemieh.domain.catalog.PostQuestionUseCase
import com.kazemieh.domain.catalog.PostReviewUseCase
import com.kazemieh.domain.catalog.UpdateQuestionUseCase
import com.kazemieh.domain.catalog.UpdateReviewUseCase
import com.kazemieh.domain.catalog.ToggleReviewHelpfulUseCase
import com.kazemieh.domain.catalog.RequestBackInStockUseCase
import com.kazemieh.domain.catalog.SubscribeToPriceAlertUseCase
import com.kazemieh.domain.catalog.GetFrequentlyBoughtTogetherUseCase
import com.kazemieh.domain.order.CreateRecurringOrderUseCase
import com.kazemieh.domain.address.GetAddressesUseCase
import com.kazemieh.domain.favorite.ObserveFavoriteIdsUseCase
import com.kazemieh.domain.favorite.ToggleFavoriteUseCase
import com.kazemieh.domain.recentlyviewed.AddRecentlyViewedUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val detailsModule = module {
    viewModel {
        DetailsViewModel(
            getProductDetailUseCase = get(),
            getProductsUseCase = get(),
            addToCartUseCase = get(),
            isUserLoggedInUseCase = get(),
            getReviewsUseCase = get(),
            postReviewUseCase = get(),
            updateReviewUseCase = get(),
            deleteReviewUseCase = get(),
            getQuestionsUseCase = get(),
            postQuestionUseCase = get(),
            updateQuestionUseCase = get(),
            deleteQuestionUseCase = get(),
            toggleFavoriteUseCase = get(),
            observeFavoriteIdsUseCase = get(),
            addRecentlyViewedUseCase = get(),
            toggleReviewHelpfulUseCase = get(),
            requestBackInStockUseCase = get(),
            getAddressesUseCase = get(),
            subscribeToPriceAlertUseCase = get(),
            getFrequentlyBoughtTogetherUseCase = get(),
            createRecurringOrderUseCase = get()
        )
    }

    factory { ToggleFavoriteUseCase(get()) }
    factory { ObserveFavoriteIdsUseCase(get()) }
    factory { AddRecentlyViewedUseCase(get()) }
    factory { GetProductDetailUseCase(get()) }
    factory { GetProductsUseCase(get()) }
    factory { GetReviewsUseCase(get()) }
    factory { PostReviewUseCase(get()) }
    factory { UpdateReviewUseCase(get()) }
    factory { DeleteReviewUseCase(get()) }
    factory { GetQuestionsUseCase(get()) }
    factory { PostQuestionUseCase(get()) }
    factory { UpdateQuestionUseCase(get()) }
    factory { DeleteQuestionUseCase(get()) }
    factory { AddToCartUseCase(get()) }
    factory { IsUserLoggedInUseCase(get()) }
    factory { ToggleReviewHelpfulUseCase(get()) }
    factory { RequestBackInStockUseCase(get()) }
    factory { GetAddressesUseCase(get()) }
    factory { SubscribeToPriceAlertUseCase(get()) }
    factory { GetFrequentlyBoughtTogetherUseCase(get()) }
    factory { CreateRecurringOrderUseCase(get()) }
}
