package com.kazemieh.domain.usecase.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.model.admin.AdminOption
import com.kazemieh.domain.repository.AdminRepository

class UpdateOptionTypeUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(id: Long, name: String): AppResult<AdminOption> {
        return repository.updateOptionType(id, name)
    }
}
