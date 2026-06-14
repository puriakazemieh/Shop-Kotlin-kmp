package com.kazemieh.domain.admin

data class AdminInteraction(
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
