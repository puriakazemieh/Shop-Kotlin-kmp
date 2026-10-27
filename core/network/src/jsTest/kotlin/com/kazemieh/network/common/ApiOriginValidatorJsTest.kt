package com.kazemieh.network.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ApiOriginValidatorJsTest {

    private val allowedHosts = setOf("api.carmilla.com", "cdn.carmilla.com")

    @Test
    fun `js release rejects localhost and private IP and foreign origin`() {
        assertNull(
            ApiOriginValidator.validateOriginOverride(
                candidateUrl = "https://localhost:8443/api",
                allowedHosts = allowedHosts,
                isRelease = true
            )
        )
        assertNull(
            ApiOriginValidator.validateOriginOverride(
                candidateUrl = "https://192.168.1.5/api",
                allowedHosts = allowedHosts,
                isRelease = true
            )
        )
        assertNull(
            ApiOriginValidator.validateOriginOverride(
                candidateUrl = "https://evil.attacker.org/api",
                allowedHosts = allowedHosts,
                isRelease = true
            )
        )
    }

    @Test
    fun `js release accepts allowed https origin`() {
        val valid = "https://api.carmilla.com/wp-json/carmilla/v1/"
        val res = ApiOriginValidator.validateOriginOverride(
            candidateUrl = valid,
            allowedHosts = allowedHosts,
            isRelease = true
        )
        assertEquals(valid, res)
    }
}
