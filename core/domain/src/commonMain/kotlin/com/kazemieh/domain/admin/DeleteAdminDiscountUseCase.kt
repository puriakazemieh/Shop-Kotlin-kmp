package com.kazemieh.domain.admin

import com.kazemieh.common.AppResult

class DeleteAdminDiscountUseCase(private val repository: AdminRepository) {
    suspend operator fun invoke(id: Long): AppResult<Unit> =
        repository.deleteDiscount(id)
}
