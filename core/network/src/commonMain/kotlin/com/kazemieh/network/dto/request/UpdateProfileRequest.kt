package com.kazemieh.network.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileRequest(
    val firstName: String?,
    val lastName: String?,
    val phone: String?,
    val city: String?,
    val postalCode: Int?
)