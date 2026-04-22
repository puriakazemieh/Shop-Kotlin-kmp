package com.kazemieh.domain.validation

class ValidatePassword {
    operator fun invoke(password: String): ValidationResult {
        if (password.length < 6) {
            return ValidationResult(false, "Password too short")
        }
        return ValidationResult(true)
    }
}