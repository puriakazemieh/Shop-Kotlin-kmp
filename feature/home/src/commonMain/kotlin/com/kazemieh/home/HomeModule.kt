package com.kazemieh.home

import com.kazemieh.domain.usecase.IsUserLoggedInUseCase
import com.kazemieh.domain.usecase.ObserveAuthStateUseCase
import com.kazemieh.domain.usecase.SignOutUseCase
import com.kazemieh.domain.usecase.catalog.GetProductsUseCase
import com.kazemieh.home.productsOverview.ProductsOverviewViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val homeModule = module {

    // ViewModel
    viewModel {
        HomeGraphViewModel(
            isUserLoggedInUseCase = get(),
            signOutUseCase = get(),
            observeAuthStateUseCase = get(),
            observeProfileUseCase = get()
        )
    }

    viewModel {
        ProductsOverviewViewModel(
            getProductsUseCase = get()
        )
    }

    // UseCases
    factory { IsUserLoggedInUseCase(get()) }
    factory { SignOutUseCase(get()) }
    factory { ObserveAuthStateUseCase(get()) }
    factory { GetProductsUseCase(get()) }

}
