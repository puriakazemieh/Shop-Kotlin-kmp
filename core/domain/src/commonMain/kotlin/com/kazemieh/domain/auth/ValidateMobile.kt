package com.kazemieh.domain.auth

import com.kazemieh.domain.common.ValidationResult

class ValidateMobile {
    operator fun invoke(mobile: String): ValidationResult {
        if (mobile.isBlank()) {
            return ValidationResult(false, AuthError.MOBILE_EMPTY)
        }

        // Basic Iranian mobile number validation
        val mobileRegex = Regex("^09[0-9]{9}$")
        if (!mobileRegex.matches(mobile)) {
            return ValidationResult(false, AuthError.INVALID_MOBILE)
        }

        return ValidationResult(true)
    }
}
