package com.kazemieh.network.blog.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class UploadMediaResponse(
    val url: String
)
