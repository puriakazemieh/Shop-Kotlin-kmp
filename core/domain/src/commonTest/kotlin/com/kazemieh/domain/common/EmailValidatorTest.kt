package com.kazemieh.domain.common

import com.kazemieh.domain.auth.ValidateEmail
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EmailValidatorTest {
    private val validateEmail = ValidateEmail()

    @Test
    fun validEmailPassesDomainValidation() {
        assertTrue(validateEmail("customer@example.test").successful)
    }

    @Test
    fun malformedEmailFailsDomainValidation() {
        assertFalse(validateEmail("customer.example.test").successful)
        assertFalse(validateEmail("customer@").successful)
    }

    @Test
    fun blankEmailFailsDomainValidation() {
        assertFalse(validateEmail("   ").successful)
    }
}
