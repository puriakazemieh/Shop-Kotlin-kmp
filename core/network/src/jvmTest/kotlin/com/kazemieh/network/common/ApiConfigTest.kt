package com.kazemieh.network.common

import kotlin.test.Test
import kotlin.test.assertEquals
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

    @Test
    fun `internal API path retains WordPress installation path`() {
        ApiConfig.baseUrlOverride = "http://localhost/carmilla-p07-qa/wp-json/carmilla/v1/"
        try {
            assertEquals("/carmilla-p07-qa/wp-json/carmilla/v1/api/products", ApiConfig.resolveInternalApiPath("/api/products"))
            assertEquals("/wp-json/wp/v2/posts", ApiConfig.resolveInternalApiPath("/wp-json/wp/v2/posts"))
        } finally {
            ApiConfig.baseUrlOverride = null
        }
    }
}
