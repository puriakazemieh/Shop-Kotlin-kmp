package com.kazemieh.domain.model.admin

data class AdminOption(
    val id: Long,
    val name: String,
    val values: List<AdminOptionValue>
)

data class AdminOptionValue(
    val id: Long,
    val value: String
)
