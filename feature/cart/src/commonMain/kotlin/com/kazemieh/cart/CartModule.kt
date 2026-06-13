package com.kazemieh.cart

import com.kazemieh.cart.checkout.CheckoutViewModel
import com.kazemieh.cart.payment_completed.PaymentViewModel
import com.kazemieh.domain.cart.*
import com.kazemieh.domain.order.CreateOrderUseCase
import com.kazemieh.domain.payment.RequestPaymentUseCase
import com.kazemieh.domain.address.*
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val cartModule = module {
    // Cart UseCases
    factory { GetCartUseCase(get()) }
    factory { AddToCartUseCase(get()) }
    factory { RemoveFromCartUseCase(get()) }
    factory { UpdateCartItemUseCase(get()) }
    factory { ClearCartUseCase(get()) }
    factory { AdjustCartVariantQtyUseCase(get()) }
    factory { SetCartVariantQtyUseCase(get()) }
    factory { MoveToSaveForLaterUseCase(get()) }
    factory { MoveToCartUseCase(get()) }
    factory { ApplyDiscountUseCase(get()) }
    factory { RemoveDiscountUseCase(get()) }

    // Order UseCases (Related to Cart/Checkout)
    factory { CreateOrderUseCase(get()) }

    // Payment UseCases
    factory { RequestPaymentUseCase(get()) }

    // Address UseCases (Related to Checkout)
    factory { GetAddressesUseCase(get()) }
    factory { AddAddressUseCase(get()) }
    factory { UpdateAddressUseCase(get()) }
    factory { DeleteAddressUseCase(get()) }
    factory { SetDefaultAddressUseCase(get()) }

    viewModel {
        CartViewModel(
            getCartUseCase = get(),
            removeFromCartUseCase = get(),
            adjustCartVariantQtyUseCase = get(),
            moveToSaveForLaterUseCase = get(),
            moveToCartUseCase = get(),
            applyDiscountUseCase = get(),
            removeDiscountUseCase = get()
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
        PaymentViewModel(
            clearCartUseCase = get()
        )
    }
}
