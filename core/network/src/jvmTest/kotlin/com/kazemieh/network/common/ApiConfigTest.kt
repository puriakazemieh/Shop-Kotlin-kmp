package com.kazemieh.network.common

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ApiConfigTest {
    @Test
    fun `foreign host is not approved for bearer authentication`() {
        ApiConfig.baseUrlOverride = "https://api.example.test/"
        try {
            assertTrue(ApiConfig.isApprovedApiHost("api.example.test"))
            assertFalse(ApiConfig.isApprovedApiHost("attacker.example.test"))
        } finally {
            ApiConfig.baseUrlOverride = null
        }
    }
}
