package com.kazemieh.data.auth.repository

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BrowserSessionLifecycleTest {
    @Test
    fun `only the fixed credential-free sign-out signal is accepted`() {
        assertTrue(isBrowserSignOutSignal(BrowserSignOutSignal))
        assertFalse(isBrowserSignOutSignal("access-token"))
        assertFalse(isBrowserSignOutSignal(null))
    }
}
