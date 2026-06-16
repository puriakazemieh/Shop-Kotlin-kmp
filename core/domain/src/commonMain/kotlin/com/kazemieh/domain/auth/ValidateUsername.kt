package com.kazemieh.domain.auth

import com.kazemieh.domain.common.EmailValidator.isValid as isEmailValid
import com.kazemieh.domain.common.ValidationResult

class ValidateUsername {
    operator fun invoke(username: String): ValidationResult {
        if (username.isBlank()) {
            return ValidationResult(false, AuthError.EMPTY_USERNAME)
        }

        val isEmail = isEmailValid(username)
        val mobileRegex = Regex("^09[0-9]{9}$")
        val isMobile = mobileRegex.matches(username)

        if (!isEmail && !isMobile) {
            return ValidationResult(false, AuthError.INVALID_USERNAME)
        }

        return ValidationResult(true)
    }
}
