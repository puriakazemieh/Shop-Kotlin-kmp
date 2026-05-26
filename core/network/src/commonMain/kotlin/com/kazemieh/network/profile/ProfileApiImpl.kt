package com.kazemieh.network.profile

import com.kazemieh.network.profile.dto.request.*
import com.kazemieh.network.profile.dto.response.*

import com.kazemieh.network.common.PageResponse

import com.kazemieh.network.common.safeApiCallRaw

import com.kazemieh.network.profile.dto.request.UpdateProfileRequest
import com.kazemieh.network.profile.dto.response.ProfileResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ProfileApiImpl(
    private val client: HttpClient
) : ProfileApi {

    override suspend fun getProfile(): ProfileResponse = safeApiCallRaw {
        client.get("/api/users/me")
    }

    override suspend fun updateProfile(request: UpdateProfileRequest): ProfileResponse =
        safeApiCallRaw {
            client.patch("/api/users/me") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
}
