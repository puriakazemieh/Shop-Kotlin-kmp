package com.kazemieh.domain.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.admin.AdminOption
import com.kazemieh.domain.admin.AdminRepository

class GetAdminOptionsUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(): AppResult<List<AdminOption>> {
        return repository.listOptions()
    }
}
