package com.kazemieh.network

import com.kazemieh.network.dto.request.UpdateProfileRequest
import com.kazemieh.network.dto.response.ProfileResponse
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