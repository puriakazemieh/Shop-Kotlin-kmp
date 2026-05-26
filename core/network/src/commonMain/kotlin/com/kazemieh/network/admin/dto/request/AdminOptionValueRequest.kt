package com.kazemieh.network.admin.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class AdminOptionValueRequest(
    val optionTypeId: Long,
    val value: String
)
