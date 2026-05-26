package com.kazemieh.domain.auth

import com.kazemieh.domain.common.EmailValidator.isValid
import com.kazemieh.domain.common.ValidationResult
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
