package com.kazemieh.domain.common

import platform.Foundation.NSPredicate
import platform.Foundation.NSString

actual object EmailValidator {
    actual fun isValid(email: String): Boolean {
        val regex = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}"
        val predicate = NSPredicate.predicateWithFormat("SELF MATCHES %@", regex as NSString)
        return predicate.evaluateWithObject(email)
    }
}
