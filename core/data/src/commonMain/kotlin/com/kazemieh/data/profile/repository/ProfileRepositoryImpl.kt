package com.kazemieh.data.profile.repository

import com.kazemieh.common.AppResult
import com.kazemieh.data.profile.source.ProfileDataSource
import com.kazemieh.data.local.ProfileLocalDataSource
import com.kazemieh.domain.model.Profile
import com.kazemieh.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProfileRepositoryImpl(
    private val profileDataSource: ProfileDataSource,
    private val profileLocalDataSource: ProfileLocalDataSource,

    ) : ProfileRepository {

    override suspend fun getProfile(): AppResult<Profile> = profileDataSource.getProfile()

    override suspend fun updateProfile(request: Profile): AppResult<Profile> =
        profileDataSource.updateProfile(request)

    override fun observeProfile(): Flow<AppResult<Profile>> {
        return profileLocalDataSource.observeProfile().map { profile ->
            if (profile != null) {
                AppResult.Success(profile)
            } else {
                AppResult.Error("Profile not found")
            }
        }
    }
}