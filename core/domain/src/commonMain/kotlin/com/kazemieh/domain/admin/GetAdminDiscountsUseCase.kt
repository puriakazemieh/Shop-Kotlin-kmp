package com.kazemieh.domain.admin

import com.kazemieh.common.AppResult

class GetAdminDiscountsUseCase(private val repository: AdminRepository) {
    suspend operator fun invoke(): AppResult<List<Discount>> =
        repository.listDiscounts()
}
