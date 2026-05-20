package com.kazemieh.domain.validation

data class ValidationResult(
    val successful: Boolean,
    val errorMessage: Any? = null
)
