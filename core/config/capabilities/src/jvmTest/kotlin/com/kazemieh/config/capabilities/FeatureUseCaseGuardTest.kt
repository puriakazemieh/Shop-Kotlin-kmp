package com.kazemieh.config.capabilities

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class FeatureUseCaseGuardTest {
    @Test
    fun `disabled feature never executes network block`() = runBlocking {
        var calls = 0
        val result = FeatureUseCaseGuard(ResolvedFeatures.fromMap(mapOf("commerce.core" to false)))
            .execute("commerce.core") { calls++; "network" }
        assertEquals(0, calls)
        assertEquals(FeatureCallResult.Disabled, result)
    }

    @Test
    fun `enabled feature executes exactly once`() = runBlocking {
        var calls = 0
        val result = FeatureUseCaseGuard(ResolvedFeatures.fromMap(mapOf("commerce.core" to true)))
            .execute("commerce.core") { calls++; "ok" }
        assertEquals(1, calls)
        assertEquals(FeatureCallResult.Executed("ok"), result)
    }
}
