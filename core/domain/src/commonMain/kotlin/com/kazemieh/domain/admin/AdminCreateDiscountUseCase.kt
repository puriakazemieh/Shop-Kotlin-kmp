package com.kazemieh.domain.admin

import com.kazemieh.common.AppResult

class AdminCreateDiscountUseCase(private val repository: AdminRepository) {
    suspend operator fun invoke(param: CreateDiscountParam): AppResult<Discount> =
        repository.createDiscount(param)
}
