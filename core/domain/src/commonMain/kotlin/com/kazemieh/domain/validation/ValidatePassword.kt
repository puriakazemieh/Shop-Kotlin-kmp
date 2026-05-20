package com.kazemieh.domain.validation

import com.kazemieh.common.*
import com.kazemieh.common.Res

class ValidatePassword {
    operator fun invoke(password: String): ValidationResult {
        if (password.length < 6) {
            return ValidationResult(false, Res.string.password_too_short)
        }
        return ValidationResult(true)
    }
}
