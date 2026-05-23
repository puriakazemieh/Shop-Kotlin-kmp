package com.kazemieh.domain.usecase.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.repository.AdminRepository

class DeleteOptionValueUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(id: Long): AppResult<Unit> {
        return repository.deleteOptionValue(id)
    }
}
