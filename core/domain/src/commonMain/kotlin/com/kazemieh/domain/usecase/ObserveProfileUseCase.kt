package com.kazemieh.domain.usecase

import com.kazemieh.common.AppResult
import com.kazemieh.domain.model.Profile
import com.kazemieh.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow

class ObserveProfileUseCase(
    private val repository: ProfileRepository
) {
    operator fun invoke(): Flow<AppResult<Profile>> {
        return repository.observeProfile()
    }
}
