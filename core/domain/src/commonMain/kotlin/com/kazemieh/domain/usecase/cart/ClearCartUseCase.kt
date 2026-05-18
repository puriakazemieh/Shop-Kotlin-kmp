package com.kazemieh.domain.usecase.cart

import com.kazemieh.common.AppResult
import com.kazemieh.domain.repository.CartRepository

class ClearCartUseCase(private val repository: CartRepository) {
    suspend operator fun invoke(): AppResult<Unit> =
        repository.clear()
}
