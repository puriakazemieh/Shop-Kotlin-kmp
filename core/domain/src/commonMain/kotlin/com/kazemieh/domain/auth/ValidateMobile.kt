package com.kazemieh.domain.auth

import com.kazemieh.domain.common.ValidationResult
import com.kazemieh.designsystem.Resources

class ValidateMobile {
    operator fun invoke(mobile: String): ValidationResult {
        if (mobile.isBlank()) {
            return ValidationResult(false, Resources.String.MobileEmpty)
        }

        // Basic Iranian mobile number validation
        val mobileRegex = Regex("^09[0-9]{9}$")
        if (!mobileRegex.matches(mobile)) {
            return ValidationResult(false, Resources.String.InvalidMobile)
        }

        return ValidationResult(true)
    }
}
