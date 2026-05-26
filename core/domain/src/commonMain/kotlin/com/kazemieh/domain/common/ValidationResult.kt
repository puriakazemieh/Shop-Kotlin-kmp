package com.kazemieh.domain.common

data class ValidationResult(
    val successful: Boolean,
    val errorMessage: Any? = null
)
