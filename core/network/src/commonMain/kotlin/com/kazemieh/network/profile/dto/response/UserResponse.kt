package com.kazemieh.network.profile.dto.response

import com.kazemieh.network.profile.dto.*

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: Long,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val city: String?,
    val postalCode: Int?,
    val phone: String?,
    val role: String,
    val isActive: Boolean? = true,
)
