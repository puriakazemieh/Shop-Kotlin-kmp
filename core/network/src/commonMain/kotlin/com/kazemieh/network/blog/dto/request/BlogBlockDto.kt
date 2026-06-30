package com.kazemieh.network.blog.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class BlogBlockDto(
    val type: String,    // header, paragraph, image, button, list, quote, divider
    val content: String = "", // text, image url, or button label
    val level: Int? = null, // only for header (1, 2, 3)
    val url: String? = null, // for button
    val items: List<String>? = null // for list
)
