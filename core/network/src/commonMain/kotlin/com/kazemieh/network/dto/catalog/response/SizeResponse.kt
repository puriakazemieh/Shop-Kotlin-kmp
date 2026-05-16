package com.kazemieh.network.dto.catalog.response

import kotlinx.serialization.Serializable

@Serializable
data class SizeResponse(
    val id: Long,
    val name: String,
    val sortOrder: Int
)
