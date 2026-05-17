package com.kazemieh.network.dto.admin.request

import kotlinx.serialization.Serializable

@Serializable
data class AdminUpdateColorRequest(
    val name: String? = null,
    val hex: String? = null
)
