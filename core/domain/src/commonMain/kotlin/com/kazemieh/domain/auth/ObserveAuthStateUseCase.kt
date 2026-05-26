package com.kazemieh.domain.auth

import com.kazemieh.common.AuthState
import com.kazemieh.domain.auth.AuthRepository
import kotlinx.coroutines.flow.StateFlow

class ObserveAuthStateUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): StateFlow<AuthState> {
        return authRepository.authState
    }
}
