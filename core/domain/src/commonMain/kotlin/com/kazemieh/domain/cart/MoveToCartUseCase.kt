package com.kazemieh.domain.cart

import com.kazemieh.common.AppResult

class MoveToCartUseCase(private val repository: CartRepository) {
    suspend operator fun invoke(itemId: Long): AppResult<Cart> = repository.moveToCart(itemId)
}
