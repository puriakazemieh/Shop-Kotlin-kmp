package com.kazemieh.domain.cart

import com.kazemieh.common.AppResult

class RemoveDiscountUseCase(private val repository: CartRepository) {
    suspend operator fun invoke(): AppResult<Cart> =
        repository.removeDiscount()
}
