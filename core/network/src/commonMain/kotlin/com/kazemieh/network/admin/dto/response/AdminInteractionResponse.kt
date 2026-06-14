package com.kazemieh.network.admin.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class AdminInteractionResponse(
    val id: Long,
    val productId: Long,
    val productTitle: String,
    val userId: Long,
    val userName: String,
    val content: String,
    val rating: Int? = null,
    val isNew: Boolean,
    val createdAt: String
)
