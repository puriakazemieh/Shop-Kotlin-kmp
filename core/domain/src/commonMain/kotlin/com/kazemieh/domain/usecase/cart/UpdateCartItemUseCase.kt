package com.kazemieh.domain.usecase.cart

import com.kazemieh.common.AppResult
import com.kazemieh.domain.model.Cart
import com.kazemieh.domain.repository.CartRepository

class UpdateCartItemUseCase(private val repository: CartRepository) {
    suspend operator fun invoke(itemId: Long, qty: Int): AppResult<Cart> =
        repository.updateQty(itemId, qty)
}
