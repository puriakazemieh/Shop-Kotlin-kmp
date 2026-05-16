package com.kazemieh.domain.repository

data class AdminCategory(
    val id: Long,
    val name: String,
    val slug: String,
    val parentId: Long?
)
