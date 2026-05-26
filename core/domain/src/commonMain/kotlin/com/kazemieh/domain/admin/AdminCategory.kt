package com.kazemieh.domain.admin

data class AdminCategory(
    val id: Long,
    val name: String,
    val slug: String,
    val parentId: Long?
)
