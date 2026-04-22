package com.kazemieh.domain.validation

import platform.Foundation.NSPredicate

actual object EmailValidator {
    actual fun isValid(email: String): Boolean {
        val regex = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}"
        val predicate = NSPredicate.predicateWithFormat("SELF MATCHES %@", regex)
        return predicate.evaluateWithObject(email)
    }
}