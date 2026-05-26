package com.kazemieh.domain.cart

import com.kazemieh.common.AppResult
import com.kazemieh.domain.cart.CartRepository

class ClearCartUseCase(private val repository: CartRepository) {
    suspend operator fun invoke(): AppResult<Unit> =
        repository.clear()
}
