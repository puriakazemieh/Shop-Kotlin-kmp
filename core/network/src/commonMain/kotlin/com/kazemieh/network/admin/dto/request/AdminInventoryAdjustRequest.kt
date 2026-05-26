package com.kazemieh.network.admin.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class AdminInventoryAdjustRequest(
    val delta: Int,
    val version: Int? = null
)
