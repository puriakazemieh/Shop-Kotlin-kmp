package com.kazemieh.domain.cart

import com.kazemieh.common.AppResult
import com.kazemieh.domain.cart.Cart
import com.kazemieh.domain.cart.CartRepository
import kotlinx.coroutines.flow.Flow

class GetCartUseCase(private val repository: CartRepository) {
    operator fun invoke(): Flow<AppResult<Cart>> = repository.getCart()
}
