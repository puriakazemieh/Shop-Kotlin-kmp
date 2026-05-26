package com.kazemieh.domain.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.admin.AdminOptionValue
import com.kazemieh.domain.admin.AdminRepository

class UpdateOptionValueUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(id: Long, optionTypeId: Long, value: String): AppResult<AdminOptionValue> {
        return repository.updateOptionValue(id, optionTypeId, value)
    }
}
