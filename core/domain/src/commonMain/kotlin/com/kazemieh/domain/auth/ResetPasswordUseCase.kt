package com.kazemieh.domain.auth

import com.kazemieh.common.AppResult
import com.kazemieh.domain.auth.AuthRepository

class ResetPasswordUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(token: String, newPassword: String): AppResult<Unit> {
        return repository.resetPassword(token, newPassword)
    }
}
