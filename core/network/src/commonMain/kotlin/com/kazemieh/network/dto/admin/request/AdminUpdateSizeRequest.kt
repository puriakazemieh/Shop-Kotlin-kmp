package com.kazemieh.network.dto.admin.request

import kotlinx.serialization.Serializable

@Serializable
data class AdminUpdateSizeRequest(
    val name: String? = null,
    val sortOrder: Int? = null
)
