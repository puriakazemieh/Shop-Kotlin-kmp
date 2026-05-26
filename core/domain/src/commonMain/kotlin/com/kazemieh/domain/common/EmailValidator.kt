package com.kazemieh.domain.common

expect object EmailValidator {
    fun isValid(email: String): Boolean
}
