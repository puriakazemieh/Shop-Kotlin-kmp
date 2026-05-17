package com.kazemieh.domain.usecase.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.repository.AdminRepository
import com.kazemieh.domain.repository.Color

class UpdateColorUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(id: Long, name: String?, hex: String?): AppResult<Color> {
        return repository.updateColor(id, name, hex)
    }
}
