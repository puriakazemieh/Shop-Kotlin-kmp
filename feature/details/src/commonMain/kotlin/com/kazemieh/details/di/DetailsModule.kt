package com.kazemieh.details.di

import com.kazemieh.details.DetailsViewModel
import com.kazemieh.domain.auth.IsUserLoggedInUseCase
import com.kazemieh.domain.cart.AddToCartUseCase
import com.kazemieh.domain.catalog.GetProductDetailUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val detailsModule = module {
    viewModel {
        DetailsViewModel(
            getProductDetailUseCase = get(),
            addToCartUseCase = get(),
            isUserLoggedInUseCase = get()
        )
    }

    factory { GetProductDetailUseCase(get()) }
    factory { AddToCartUseCase(get()) }
    factory { IsUserLoggedInUseCase(get()) }
}
