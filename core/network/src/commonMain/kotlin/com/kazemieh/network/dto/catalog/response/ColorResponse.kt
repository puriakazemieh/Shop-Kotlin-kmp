package com.kazemieh.network.dto.catalog.response

import kotlinx.serialization.Serializable

@Serializable
data class ColorResponse(
    val id: Long,
    val name: String,
    val hex: String? = null
)
