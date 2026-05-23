package com.kazemieh.network.dto.admin.request

import kotlinx.serialization.Serializable

@Serializable
data class AdminOptionValueRequest(
    val optionTypeId: Long,
    val value: String
)
