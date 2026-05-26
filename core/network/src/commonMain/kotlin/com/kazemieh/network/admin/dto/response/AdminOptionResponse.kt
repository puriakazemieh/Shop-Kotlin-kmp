package com.kazemieh.network.admin.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class AdminOptionResponse(
    val id: Long,
    val name: String,
    val values: List<AdminOptionValueResponse> = emptyList()
)

@Serializable
data class AdminOptionValueResponse(
    val id: Long,
    val value: String
)
