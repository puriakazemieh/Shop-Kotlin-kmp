package com.kazemieh.domain.cart

import com.kazemieh.common.AppResult
import com.kazemieh.domain.cart.Cart
import com.kazemieh.domain.cart.CartRepository

class AddToCartUseCase(private val repository: CartRepository) {
    suspend operator fun invoke(productId: Long? = null, variantId: Long? = null, qty: Int): AppResult<Cart> =
        repository.addItem(productId, variantId, qty)
}
