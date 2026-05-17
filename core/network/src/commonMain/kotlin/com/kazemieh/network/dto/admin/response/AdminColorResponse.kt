package com.kazemieh.network.dto.admin.response

import kotlinx.serialization.Serializable

@Serializable
data class AdminColorResponse(
    val id: Long,
    val name: String,
    val hex: String?
)
