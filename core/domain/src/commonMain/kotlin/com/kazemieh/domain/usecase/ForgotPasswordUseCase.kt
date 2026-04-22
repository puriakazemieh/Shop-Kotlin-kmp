package com.kazemieh.domain.usecase

import com.kazemieh.domain.AuthRepository
import com.kazemieh.domain.common.AppResult

class ForgotPasswordUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String): AppResult<Unit> {
        return repository.forgotPassword(email)
    }
}