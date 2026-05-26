package com.kazemieh.domain.auth

import com.kazemieh.common.AppResult
import com.kazemieh.domain.auth.AuthRepository

class SignOutUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): AppResult<Unit> = authRepository.signOut()
}
