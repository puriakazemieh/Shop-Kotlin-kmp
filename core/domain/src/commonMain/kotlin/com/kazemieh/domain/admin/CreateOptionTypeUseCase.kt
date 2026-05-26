package com.kazemieh.domain.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.admin.AdminOption
import com.kazemieh.domain.admin.AdminRepository

class CreateOptionTypeUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(name: String): AppResult<AdminOption> {
        return repository.createOptionType(name)
    }
}
