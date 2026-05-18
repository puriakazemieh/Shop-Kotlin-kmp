package com.kazemieh.domain.usecase.cart

import com.kazemieh.common.AppResult
import com.kazemieh.domain.model.Cart
import com.kazemieh.domain.repository.CartRepository

class AdjustCartVariantQtyUseCase(private val repository: CartRepository) {
    suspend operator fun invoke(variantId: Long, delta: Int): AppResult<Cart> =
        repository.adjustVariantQty(variantId, delta)
}
