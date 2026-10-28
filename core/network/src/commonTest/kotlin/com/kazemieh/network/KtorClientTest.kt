package com.kazemieh.network

import com.kazemieh.network.common.ApiConfig
import io.ktor.http.Url
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KtorClientTest {
    @Test
    fun wordpressApiCallsStayUnderTheConfiguredInstallationPath() {
        ApiConfig.baseUrlOverride = "https://shop.example.test/site/wp-json/carmilla/v1/"
        try {
            val path = ApiConfig.resolveInternalApiPath("/api/products")
            assertEquals("/site/wp-json/carmilla/v1/api/products", path)
            assertTrue(ApiConfig.isApprovedApiUrl(Url("https://shop.example.test$path")))
            assertFalse(ApiConfig.isApprovedApiUrl(Url("https://other.example.test$path")))
            assertFalse(ApiConfig.isApprovedApiUrl(Url("https://shop.example.test/site/wp-json/carmilla/v10/api/products")))
        } finally {
            ApiConfig.baseUrlOverride = null
        }
    }

    @Test
    fun springApiCallsKeepTheRootPath() {
        ApiConfig.baseUrlOverride = "https://api.example.test/"
        try {
            assertEquals("/api/products", ApiConfig.resolveInternalApiPath("/api/products"))
            assertTrue(ApiConfig.isApprovedApiUrl(Url("https://api.example.test/api/products")))
            assertFalse(ApiConfig.isApprovedApiUrl(Url("http://api.example.test/api/products")))
        } finally {
            ApiConfig.baseUrlOverride = null
        }
    }
}
