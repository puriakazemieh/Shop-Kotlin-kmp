package com.kazemieh.network

import com.kazemieh.network.dto.response.ProfileResponse
import com.kazemieh.network.dto.request.UpdateProfileRequest

interface ProfileApi {
    suspend fun getProfile(): ProfileResponse
    suspend fun updateProfile(request: UpdateProfileRequest): ProfileResponse
}