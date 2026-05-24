package com.kazemieh.domain.usecase

import com.kazemieh.common.AppResult
import com.kazemieh.domain.repository.AuthRepository

class ResetPasswordUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(token: String, newPassword: String): AppResult<Unit> {
        return repository.resetPassword(token, newPassword)
    }
}