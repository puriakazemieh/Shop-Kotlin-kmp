package com.kazemieh.domain.usecase.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.model.admin.AdminOptionValue
import com.kazemieh.domain.repository.AdminRepository

class UpdateOptionValueUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(id: Long, optionTypeId: Long, value: String): AppResult<AdminOptionValue> {
        return repository.updateOptionValue(id, optionTypeId, value)
    }
}
