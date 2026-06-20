package com.kazemieh.network.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import kotlinx.serialization.ExperimentalSerializationApi

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class PageResponse<T>(
    @JsonNames("content")
    val items: List<T>,
    val page: PageMetadata
)

@Serializable
data class PageMetadata(
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    @OptIn(ExperimentalSerializationApi::class)
    @JsonNames("number")
    val number: Int
)
