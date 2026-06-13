package com.kazemieh.domain.admin

import com.kazemieh.common.AppResult

class UpdateAdminDiscountUseCase(private val repository: AdminRepository) {
    suspend operator fun invoke(id: Long, param: UpdateDiscountParam): AppResult<Discount> =
        repository.updateDiscount(id, param)
}
