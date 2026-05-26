package com.kazemieh.domain.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.admin.AdminOption
import com.kazemieh.domain.admin.AdminRepository

class UpdateOptionTypeUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(id: Long, name: String): AppResult<AdminOption> {
        return repository.updateOptionType(id, name)
    }
}
