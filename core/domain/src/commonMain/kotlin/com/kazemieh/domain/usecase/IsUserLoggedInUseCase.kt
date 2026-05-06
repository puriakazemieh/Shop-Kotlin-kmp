package com.kazemieh.domain.usecase

import com.kazemieh.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class IsUserLoggedInUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<Boolean> = authRepository.isLoggedIn()
}
