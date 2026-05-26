package com.kazemieh.domain.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.admin.AdminOptionValue
import com.kazemieh.domain.admin.AdminRepository

class CreateOptionValueUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(optionTypeId: Long, value: String): AppResult<AdminOptionValue> {
        return repository.createOptionValue(optionTypeId, value)
    }
}
