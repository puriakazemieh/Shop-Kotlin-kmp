package com.kazemieh.domain.usecase.cart

import com.kazemieh.common.AppResult
import com.kazemieh.domain.model.Cart
import com.kazemieh.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow

class GetCartUseCase(private val repository: CartRepository) {
    operator fun invoke(): Flow<AppResult<Cart>> = repository.getCart()
}
