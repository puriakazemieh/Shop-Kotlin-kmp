package com.kazemieh.network.catalog.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class ProductAttributeDto(
    val name: String = "",
    val value: String = ""
)
