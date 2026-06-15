package com.kazemieh.network.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.ExperimentalSerializationApi

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class PageResponse<T>(
    @JsonNames("content")
    val items: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)
