package com.kazemieh.home

import com.kazemieh.domain.usecase.IsUserLoggedInUseCase
import com.kazemieh.domain.usecase.ObserveAuthStateUseCase
import com.kazemieh.domain.usecase.SignOutUseCase
import com.kazemieh.domain.usecase.cart.*
import com.kazemieh.domain.usecase.catalog.GetCategoriesUseCase
import com.kazemieh.domain.usecase.catalog.GetProductsUseCase
import com.kazemieh.domain.usecase.order.*
import com.kazemieh.home.cart.CartViewModel
import com.kazemieh.home.cart.checkout.CheckoutViewModel
import com.kazemieh.home.cart.payment_completed.PaymentViewModel
import com.kazemieh.home.category.CategoriesViewModel
import com.kazemieh.home.category.CategorySearchViewModel
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
            observeProfileUseCase = get(),
            getCartUseCase = get()
        )
    }

    viewModel {
        ProductsOverviewViewModel(
            getProductsUseCase = get()
        )
    }

    viewModel {
        CartViewModel(
            getCartUseCase = get(),
            updateCartItemUseCase = get(),
            removeFromCartUseCase = get(),
            adjustCartVariantQtyUseCase = get()
        )
    }

    viewModel {
        CheckoutViewModel(
            createOrderUseCase = get(),
            getProfileUseCase = get(),
            getCartUseCase = get()
        )
    }

    viewModel {
        CategoriesViewModel(
            getCategoriesUseCase = get()
        )
    }

    viewModel {
        PaymentViewModel(
            clearCartUseCase = get()
        )
    }

    viewModel {
        CategorySearchViewModel(
            getProductsUseCase = get()
        )
    }

    // UseCases
    factory { IsUserLoggedInUseCase(get()) }
    factory { SignOutUseCase(get()) }
    factory { ObserveAuthStateUseCase(get()) }
    factory { GetProductsUseCase(get()) }
    factory { GetCategoriesUseCase(get()) }

    // Cart UseCases
    factory { GetCartUseCase(get()) }
    factory { AddToCartUseCase(get()) }
    factory { UpdateCartItemUseCase(get()) }
    factory { RemoveFromCartUseCase(get()) }
    factory { ClearCartUseCase(get()) }
    factory { SetCartVariantQtyUseCase(get()) }
    factory { AdjustCartVariantQtyUseCase(get()) }

    // Order UseCases
    factory { GetMyOrdersUseCase(get()) }
    factory { GetOrderUseCase(get()) }
    factory { CreateOrderUseCase(get()) }
    factory { CancelOrderUseCase(get()) }

}
