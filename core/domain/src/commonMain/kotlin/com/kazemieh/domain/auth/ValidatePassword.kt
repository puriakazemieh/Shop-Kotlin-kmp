package com.kazemieh.domain.auth

import com.kazemieh.domain.common.ValidationResult
import com.kazemieh.common.*
import com.kazemieh.common.Res

class ValidatePassword {
    operator fun invoke(password: String): ValidationResult {
        if (password.length < 8) {
            return ValidationResult(false, Res.string.password_too_short)
        }
        return ValidationResult(true)
    }
}
