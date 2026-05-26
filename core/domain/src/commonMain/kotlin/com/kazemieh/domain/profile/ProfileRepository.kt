package com.kazemieh.domain.profile

import com.kazemieh.common.AppResult
import com.kazemieh.domain.profile.Profile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    suspend fun getProfile(): AppResult<Profile>
    suspend fun updateProfile(request: Profile): AppResult<Profile>
    fun observeProfile(): Flow<AppResult<Profile>>
}
