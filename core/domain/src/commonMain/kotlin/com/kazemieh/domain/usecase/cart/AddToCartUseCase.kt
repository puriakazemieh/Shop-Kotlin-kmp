package com.kazemieh.domain.usecase.cart

import com.kazemieh.common.AppResult
import com.kazemieh.domain.model.Cart
import com.kazemieh.domain.repository.CartRepository

class AddToCartUseCase(private val repository: CartRepository) {
    suspend operator fun invoke(variantId: Long, qty: Int): AppResult<Cart> =
        repository.addItem(variantId, qty)
}
