package com.kazemieh.network.profile.dto.request

import com.kazemieh.network.profile.dto.*

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileRequest(
    val firstName: String?,
    val lastName: String?,
    val phone: String?,
    val city: String?,
    val postalCode: Int?
)
