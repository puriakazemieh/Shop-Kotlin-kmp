package com.kazemieh.domain.cart

import com.kazemieh.common.AppResult

class ApplyDiscountUseCase(private val repository: CartRepository) {
    suspend operator fun invoke(code: String): AppResult<Cart> =
        repository.applyDiscount(code)
}
