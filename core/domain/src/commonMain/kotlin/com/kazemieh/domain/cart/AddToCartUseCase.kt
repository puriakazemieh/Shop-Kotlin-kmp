package com.kazemieh.domain.cart

import com.kazemieh.common.AppResult
import com.kazemieh.domain.cart.Cart
import com.kazemieh.domain.cart.CartRepository

class AddToCartUseCase(private val repository: CartRepository) {
    suspend operator fun invoke(variantId: Long, qty: Int): AppResult<Cart> =
        repository.addItem(variantId, qty)
}
