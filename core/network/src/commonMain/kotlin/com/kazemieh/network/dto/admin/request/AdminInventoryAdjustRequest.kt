package com.kazemieh.network.dto.admin.request

import kotlinx.serialization.Serializable

@Serializable
data class AdminInventoryAdjustRequest(
    val delta: Int,
    val version: Int? = null
)
