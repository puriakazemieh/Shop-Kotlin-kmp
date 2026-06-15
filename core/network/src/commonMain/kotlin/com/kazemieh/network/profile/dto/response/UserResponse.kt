package com.kazemieh.network.profile.dto.response

import com.kazemieh.network.profile.dto.*

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: Long,
    val email: String?,
    val mobile: String?,
    val fullName: String?,
    val city: String?,
    val postalCode: Int?,
    val role: String,
    val isActive: Boolean? = true,
)
