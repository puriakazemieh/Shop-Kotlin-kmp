package com.kazemieh.network.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileResponse(
    val id: Long,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val phone: String?,
    val city: String?,
    val postalCode: Int?,
    val role: String,
    @SerialName("active") val isActive: Boolean,
    val createdAt: String?,
    val updatedAt: String?
)

