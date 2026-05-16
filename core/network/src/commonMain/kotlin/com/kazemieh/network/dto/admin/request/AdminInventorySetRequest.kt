package com.kazemieh.network.dto.admin.request

import kotlinx.serialization.Serializable

@Serializable
data class AdminInventorySetRequest(
    val onHand: Int,
    val version: Int? = null
)
