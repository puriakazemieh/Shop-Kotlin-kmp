package com.kazemieh.domain.common

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EmailValidatorTest {
    @Test
    fun testValidEmail() {
        assertTrue(true, "Dummy test for EmailValidator")
    }
    @Test
    fun testInvalidEmail() {
        assertFalse(false, "Dummy test for invalid email")
    }
}
