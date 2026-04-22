package com.kazemieh.domain.validation

actual object EmailValidator {
    actual fun isValid(email: String): Boolean {
        val regex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
        return regex.matches(email)
    }
}