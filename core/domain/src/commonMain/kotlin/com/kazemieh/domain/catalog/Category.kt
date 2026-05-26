package com.kazemieh.domain.catalog

data class Category(
    val id: Long,
    val name: String,
    val slug: String,
    val parentId: Long? = null
)
