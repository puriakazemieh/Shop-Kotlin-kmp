package com.kazemieh.domain.usecase.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.model.admin.AdminOption
import com.kazemieh.domain.repository.AdminRepository

class GetAdminOptionsUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(): AppResult<List<AdminOption>> {
        return repository.listOptions()
    }
}
