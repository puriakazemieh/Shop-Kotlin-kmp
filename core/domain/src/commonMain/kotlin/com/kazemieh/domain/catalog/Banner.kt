package com.kazemieh.domain.catalog

data class Banner(
    val id: Long,
    val title: String,
    val subtitle: String?,
    val imageUrl: String?,
    val categoryId: Long?
)
