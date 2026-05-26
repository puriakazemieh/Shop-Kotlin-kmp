package com.kazemieh.domain.cart

import com.kazemieh.common.AppResult
import com.kazemieh.domain.cart.Cart
import com.kazemieh.domain.cart.CartRepository

class AdjustCartVariantQtyUseCase(private val repository: CartRepository) {
    suspend operator fun invoke(variantId: Long, delta: Int): AppResult<Cart> =
        repository.adjustVariantQty(variantId, delta)
}
