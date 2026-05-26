package com.kazemieh.network.admin.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class AdminInventorySetRequest(
    val onHand: Int,
    val version: Int? = null
)
