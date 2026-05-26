package com.kazemieh.domain.common

import android.util.Patterns

actual object EmailValidator {
    actual fun isValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
