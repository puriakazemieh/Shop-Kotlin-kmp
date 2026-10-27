package com.kazemieh.network.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ApiOriginValidatorTest {

    private val allowedProductionHosts = setOf("api.carmilla.com", "cdn.carmilla.com")

    @Test
    fun `localhost and loopback are rejected in release`() {
        assertNull(
            ApiOriginValidator.validateOriginOverride(
                candidateUrl = "https://localhost:8443/wp-json/carmilla/v1/",
                allowedHosts = allowedProductionHosts,
                isRelease = true
            )
        )
        assertNull(
            ApiOriginValidator.validateOriginOverride(
                candidateUrl = "http://localhost:8080/wp-json/carmilla/v1/",
                allowedHosts = allowedProductionHosts,
                isRelease = true
            )
        )
        assertNull(
            ApiOriginValidator.validateOriginOverride(
                candidateUrl = "http://127.0.0.1:8080/wp-json/carmilla/v1/",
                allowedHosts = allowedProductionHosts,
                isRelease = true
            )
        )
        assertNull(
            ApiOriginValidator.validateOriginOverride(
                candidateUrl = "https://127.0.0.1:8443/wp-json/carmilla/v1/",
                allowedHosts = allowedProductionHosts,
                isRelease = true
            )
        )
        assertNull(
            ApiOriginValidator.validateOriginOverride(
                candidateUrl = "https://test.localhost/api",
                allowedHosts = allowedProductionHosts,
                isRelease = true
            )
        )
    }

    @Test
    fun `private IPv4 ranges and private domains are rejected in release`() {
        // 10.0.0.0/8
        assertNull(
            ApiOriginValidator.validateOriginOverride(
                candidateUrl = "https://10.0.1.25/api",
                allowedHosts = allowedProductionHosts,
                isRelease = true
            )
        )
        // 172.16.0.0/12
        assertNull(
            ApiOriginValidator.validateOriginOverride(
                candidateUrl = "https://172.20.5.10/api",
                allowedHosts = allowedProductionHosts,
                isRelease = true
            )
        )
        // 192.168.0.0/16
        assertNull(
            ApiOriginValidator.validateOriginOverride(
                candidateUrl = "https://192.168.1.100/wp-json/carmilla/v1/",
                allowedHosts = allowedProductionHosts,
                isRelease = true
            )
        )
        // 169.254.0.0/16
        assertNull(
            ApiOriginValidator.validateOriginOverride(
                candidateUrl = "https://169.254.169.254/latest/meta-data",
                allowedHosts = allowedProductionHosts,
                isRelease = true
            )
        )
        // Private domain suffixes
        assertNull(
            ApiOriginValidator.validateOriginOverride(
                candidateUrl = "https://myserver.local/api",
                allowedHosts = allowedProductionHosts,
                isRelease = true
            )
        )
        assertNull(
            ApiOriginValidator.validateOriginOverride(
                candidateUrl = "https://internal-service.lan/api",
                allowedHosts = allowedProductionHosts,
                isRelease = true
            )
        )
    }

    @Test
    fun `foreign host not in allowlist is rejected in release`() {
        assertNull(
            ApiOriginValidator.validateOriginOverride(
                candidateUrl = "https://evil-attacker.example.com/wp-json/carmilla/v1/",
                allowedHosts = allowedProductionHosts,
                isRelease = true
            )
        )
        assertNull(
            ApiOriginValidator.validateOriginOverride(
                candidateUrl = "https://untrusted-api.org/api",
                allowedHosts = allowedProductionHosts,
                isRelease = true
            )
        )
    }

    @Test
    fun `plain HTTP is rejected in release even if host is in allowlist`() {
        assertNull(
            ApiOriginValidator.validateOriginOverride(
                candidateUrl = "http://api.carmilla.com/wp-json/carmilla/v1/",
                allowedHosts = allowedProductionHosts,
                isRelease = true
            )
        )
    }

    @Test
    fun `trusted HTTPS host in allowlist is accepted in release`() {
        val validUrl = "https://api.carmilla.com/wp-json/carmilla/v1/"
        val result = ApiOriginValidator.validateOriginOverride(
            candidateUrl = validUrl,
            allowedHosts = allowedProductionHosts,
            isRelease = true
        )
        assertEquals(validUrl, result)
    }

    @Test
    fun `localhost and emulator are accepted in debug mode`() {
        val localUrl = "http://localhost:8080/wp-json/carmilla/v1/"
        val result = ApiOriginValidator.validateOriginOverride(
            candidateUrl = localUrl,
            allowedHosts = setOf("localhost"),
            isRelease = false
        )
        assertEquals(localUrl, result)

        val emuUrl = "http://10.0.2.2:8080/wp-json/carmilla/v1/"
        val emuResult = ApiOriginValidator.validateOriginOverride(
            candidateUrl = emuUrl,
            allowedHosts = setOf("localhost"),
            isRelease = false
        )
        assertEquals(emuUrl, emuResult)
    }

    @Test
    fun `foreign host not in allowlist is rejected in debug mode`() {
        assertNull(
            ApiOriginValidator.validateOriginOverride(
                candidateUrl = "https://evil-attacker.example.com/api",
                allowedHosts = setOf("localhost", "127.0.0.1"),
                isRelease = false
            )
        )
    }
}
