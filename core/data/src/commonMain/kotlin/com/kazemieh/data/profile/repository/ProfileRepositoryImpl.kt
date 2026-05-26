package com.kazemieh.data.profile.repository

import com.kazemieh.network.profile.dto.request.*
import com.kazemieh.network.profile.dto.response.*
import com.kazemieh.network.address.dto.request.*
import com.kazemieh.network.address.dto.response.*
import com.kazemieh.domain.profile.*
import com.kazemieh.domain.address.*
import com.kazemieh.network.common.*
import com.kazemieh.common.*
import com.kazemieh.data.profile.source.ProfileDataSource
import com.kazemieh.data.local.ProfileLocalDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map




class ProfileRepositoryImpl(
    private val profileDataSource: ProfileDataSource,
    private val profileLocalDataSource: ProfileLocalDataSource,
) : ProfileRepository {

    override suspend fun getProfile(): AppResult<Profile> {
        return profileDataSource.getProfile().doOnSuccess {
            profileLocalDataSource.saveProfile(it)
        }
    }

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
