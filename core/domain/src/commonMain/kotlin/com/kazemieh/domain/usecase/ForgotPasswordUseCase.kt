package com.kazemieh.domain.usecase

import com.kazemieh.domain.AuthRepository

class ForgotPasswordUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        return repository.forgotPassword(email)
    }
}