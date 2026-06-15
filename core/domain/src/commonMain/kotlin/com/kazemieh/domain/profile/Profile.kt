package com.kazemieh.domain.profile

import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: Long,
    val email: String?,
    val firstName: String? = null,
    val lastName: String? = null,
    val phone: String? = null,
    val mobile: String? = null,
    val fullName: String? = null,
    val city: String?,
    val role: String,
    val postalCode: Int?,
)
