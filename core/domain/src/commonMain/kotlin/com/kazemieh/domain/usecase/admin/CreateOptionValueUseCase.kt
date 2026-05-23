package com.kazemieh.domain.usecase.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.model.admin.AdminOptionValue
import com.kazemieh.domain.repository.AdminRepository

class CreateOptionValueUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(optionTypeId: Long, value: String): AppResult<AdminOptionValue> {
        return repository.createOptionValue(optionTypeId, value)
    }
}
