package com.kazemieh.network.dto.admin.request

import kotlinx.serialization.Serializable

@Serializable
data class AdminCreateColorRequest(
    val name: String,
    val hex: String? = null
)
