package com.kazemieh.domain.common

actual object EmailValidator {
    private val regex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")

    actual fun isValid(email: String): Boolean {
        return regex.matches(email)
    }
}
