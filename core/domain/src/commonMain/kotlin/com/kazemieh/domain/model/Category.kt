package com.kazemieh.domain.model

data class Category(
    val id: Long,
    val name: String,
    val slug: String,
    val parentId: Long? = null
)
