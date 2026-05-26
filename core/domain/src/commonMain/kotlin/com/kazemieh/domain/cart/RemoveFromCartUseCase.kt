package com.kazemieh.domain.cart

import com.kazemieh.common.AppResult
import com.kazemieh.domain.cart.Cart
import com.kazemieh.domain.cart.CartRepository

class RemoveFromCartUseCase(private val repository: CartRepository) {
    suspend operator fun invoke(itemId: Long): AppResult<Cart> =
        repository.remove(itemId)
}
