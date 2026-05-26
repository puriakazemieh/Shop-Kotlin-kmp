package com.kazemieh.domain.cart

import com.kazemieh.common.AppResult
import com.kazemieh.domain.cart.Cart
import com.kazemieh.domain.cart.CartRepository

class SetCartVariantQtyUseCase(private val repository: CartRepository) {
    suspend operator fun invoke(variantId: Long, qty: Int): AppResult<Cart> =
        repository.setVariantQty(variantId, qty)
}
