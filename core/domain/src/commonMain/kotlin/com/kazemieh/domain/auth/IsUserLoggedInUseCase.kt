package com.kazemieh.domain.auth

import com.kazemieh.domain.auth.AuthRepository
import kotlinx.coroutines.flow.Flow

class IsUserLoggedInUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<Boolean> = authRepository.isLoggedIn()
}
