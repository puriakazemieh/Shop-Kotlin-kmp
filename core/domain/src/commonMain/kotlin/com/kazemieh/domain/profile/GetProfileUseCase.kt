package com.kazemieh.domain.profile

import com.kazemieh.common.AppResult
import com.kazemieh.domain.profile.Profile
import com.kazemieh.domain.profile.ProfileRepository

class GetProfileUseCase(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(): AppResult<Profile> {
        return repository.getProfile()
    }
}
