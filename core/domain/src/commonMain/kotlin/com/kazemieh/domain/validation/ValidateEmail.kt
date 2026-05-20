package com.kazemieh.domain.validation

import com.kazemieh.domain.validation.EmailValidator.isValid
import com.kazemieh.common.*
import com.kazemieh.common.Res

class ValidateEmail {
    operator fun invoke(email: String): ValidationResult {
        if (email.isBlank()) {
            return ValidationResult(false, Res.string.email_empty)
        }

        if (!isValid(email)) {
            return ValidationResult(false, Res.string.invalid_email)
        }

        return ValidationResult(true)
    }
}
