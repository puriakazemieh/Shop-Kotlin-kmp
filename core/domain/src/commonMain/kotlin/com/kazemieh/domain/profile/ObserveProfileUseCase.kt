package com.kazemieh.domain.profile

import com.kazemieh.common.AppResult
import com.kazemieh.domain.profile.Profile
import com.kazemieh.domain.profile.ProfileRepository
import kotlinx.coroutines.flow.Flow

class ObserveProfileUseCase(
    private val repository: ProfileRepository
) {
    operator fun invoke(): Flow<AppResult<Profile>> {
        return repository.observeProfile()
    }
}
