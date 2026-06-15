package com.kazemieh.domain.auth

import com.kazemieh.domain.common.EmailValidator.isValid as isEmailValid
import com.kazemieh.domain.common.ValidationResult
import com.kazemieh.designsystem.Resources

class ValidateUsername {
    operator fun invoke(username: String): ValidationResult {
        if (username.isBlank()) {
            return ValidationResult(false, Resources.String.InvalidUsername)
        }

        val isEmail = isEmailValid(username)
        val mobileRegex = Regex("^09[0-9]{9}$")
        val isMobile = mobileRegex.matches(username)

        if (!isEmail && !isMobile) {
            return ValidationResult(false, Resources.String.InvalidUsername)
        }

        return ValidationResult(true)
    }
}
