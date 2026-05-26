package com.kazemieh.domain.cart

import com.kazemieh.common.AppResult
import com.kazemieh.domain.cart.Cart
import com.kazemieh.domain.cart.CartRepository

class UpdateCartItemUseCase(private val repository: CartRepository) {
    suspend operator fun invoke(itemId: Long, qty: Int): AppResult<Cart> =
        repository.updateQty(itemId, qty)
}
