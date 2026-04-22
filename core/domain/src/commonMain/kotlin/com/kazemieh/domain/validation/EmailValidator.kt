package com.kazemieh.domain.validation

expect object EmailValidator {
    fun isValid(email: String): Boolean
}