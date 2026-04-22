package com.kazemieh.domain.validation

import android.util.Patterns

actual object EmailValidator {
    actual fun isValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}