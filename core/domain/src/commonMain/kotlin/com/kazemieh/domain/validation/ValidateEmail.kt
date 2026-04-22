package com.kazemieh.domain.validation

import com.kazemieh.domain.validation.EmailValidator.isValid

class ValidateEmail {
    operator fun invoke(email: String): ValidationResult {
        if (email.isBlank()) {
            return ValidationResult(false, "Email can't be empty")
        }

        if (!isValid(email)) {
            return ValidationResult(false, "Invalid email")
        }

        return ValidationResult(true)
    }
}