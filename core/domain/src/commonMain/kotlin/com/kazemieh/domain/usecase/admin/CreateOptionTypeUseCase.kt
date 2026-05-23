package com.kazemieh.domain.usecase.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.model.admin.AdminOption
import com.kazemieh.domain.repository.AdminRepository

class CreateOptionTypeUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(name: String): AppResult<AdminOption> {
        return repository.createOptionType(name)
    }
}
