package com.kazemieh.network.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import io.ktor.http.Url

class ApiConfigTest {
    @Test
    fun `bearer scope includes scheme port and path boundaries`() {
        ApiConfig.baseUrlOverride = "https://api.example.test:8443/wp-json/carmilla/v1/"
        try {
            assertTrue(ApiConfig.isApprovedApiUrl(Url("https://api.example.test:8443/wp-json/carmilla/v1/api/products")))
            assertTrue(ApiConfig.isApprovedApiUrl(Url("https://API.example.test:8443/wp-json/carmilla/v1")))
            assertFalse(ApiConfig.isApprovedApiUrl(Url("http://api.example.test:8443/wp-json/carmilla/v1/api/products")))
            assertFalse(ApiConfig.isApprovedApiUrl(Url("https://api.example.test/wp-json/carmilla/v1/api/products")))
            assertFalse(ApiConfig.isApprovedApiUrl(Url("https://other.example.test:8443/wp-json/carmilla/v1/api/products")))
            assertFalse(ApiConfig.isApprovedApiUrl(Url("https://api.example.test:8443/wp-json/carmilla/v10/api/products")))
            assertFalse(ApiConfig.isApprovedApiUrl(Url("https://api.example.test:8443/wp-json/carmilla/v1/%2e%2e/outside")))
            assertFalse(ApiConfig.isApprovedApiUrl(Url("https://api.example.test:8443/wp-json/carmilla/v1/%252e%252e/outside")))
        } finally {
            ApiConfig.baseUrlOverride = null
        }
    }

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
