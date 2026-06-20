package com.kazemieh.network.profile.dto.response

import com.kazemieh.network.profile.dto.*

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: Long,
    val email: String?,
    val mobile: String? = null,
    val fullName: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val phone: String? = null,
    val city: String?,
    val postalCode: Int?,
    val role: String,
    @SerialName("active")
    val isActive: Boolean? = true,
)
