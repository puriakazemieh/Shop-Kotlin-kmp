package com.kazemieh.home

import com.kazemieh.domain.usecase.IsUserLoggedInUseCase
import com.kazemieh.domain.usecase.ObserveAuthStateUseCase
import com.kazemieh.domain.usecase.SignOutUseCase
import com.kazemieh.domain.usecase.address.*
import com.kazemieh.domain.usecase.cart.*
import com.kazemieh.domain.usecase.catalog.GetCategoriesUseCase
import com.kazemieh.domain.usecase.catalog.GetProductsUseCase
import com.kazemieh.domain.usecase.order.*
import com.kazemieh.domain.usecase.payment.RequestPaymentUseCase
import com.kazemieh.domain.usecase.settings.ObserveLanguageUseCase
import com.kazemieh.domain.usecase.settings.ObserveThemeModeUseCase
import com.kazemieh.domain.usecase.settings.UpdateLanguageUseCase
import com.kazemieh.domain.usecase.settings.UpdateThemeModeUseCase
import com.kazemieh.home.cart.CartViewModel
import com.kazemieh.home.cart.checkout.CheckoutViewModel
import com.kazemieh.home.cart.payment_completed.PaymentViewModel
import com.kazemieh.home.category.CategoriesViewModel
import com.kazemieh.home.category.CategorySearchViewModel
import com.kazemieh.home.productsOverview.ProductsOverviewViewModel
import com.kazemieh.home.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val homeModule = module {

    // ViewModel
    viewModel {
        HomeGraphViewModel(
            signOutUseCase = get(),
            observeAuthStateUseCase = get(),
            observeProfileUseCase = get(),
            getProfileUseCase = get(),
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
            getCartUseCase = get(),
            addAddressUseCase = get(),
            getAddressesUseCase = get(),
            requestPaymentUseCase = get()
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

    viewModel {
        SettingsViewModel(
            observeLanguageUseCase = get(),
            updateLanguageUseCase = get(),
            observeThemeModeUseCase = get(),
            updateThemeModeUseCase = get()
        )
    }

    // UseCases
    factory { IsUserLoggedInUseCase(get()) }
    factory { SignOutUseCase(get()) }
    factory { ObserveAuthStateUseCase(get()) }
    factory { GetProductsUseCase(get()) }
    factory { GetCategoriesUseCase(get()) }

    // Settings UseCases
    factory { ObserveLanguageUseCase(get()) }
    factory { UpdateLanguageUseCase(get()) }
    factory { ObserveThemeModeUseCase(get()) }
    factory { UpdateThemeModeUseCase(get()) }

    // Address UseCases
    factory { GetAddressesUseCase(get()) }
    factory { AddAddressUseCase(get()) }
    factory { UpdateAddressUseCase(get()) }
    factory { DeleteAddressUseCase(get()) }
    factory { SetDefaultAddressUseCase(get()) }

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
    factory { RequestPaymentUseCase(get()) }

}
