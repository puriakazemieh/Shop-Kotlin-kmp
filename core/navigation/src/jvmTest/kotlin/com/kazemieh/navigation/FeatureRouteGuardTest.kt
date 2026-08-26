package com.kazemieh.navigation

import com.kazemieh.config.capabilities.ResolvedFeatures
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FeatureRouteGuardTest {
    @Test
    fun `disabled direct route is blocked while home and enabled route pass`() {
        val guard = FeatureRouteGuard(ResolvedFeatures.fromMap(mapOf("content.blog" to true, "commerce.core" to false)))
        assertEquals(RouteGuardDecision.Allowed, guard.checkRoute("com.kazemieh.common.Screen.HomeGraph"))
        assertEquals(RouteGuardDecision.Allowed, guard.checkRoute("com.kazemieh.common.Screen.BlogList"))
        val blocked = guard.checkRoute("com.kazemieh.common.Screen.ProductDetail")
        assertTrue(blocked is RouteGuardDecision.Blocked)
        assertEquals("commerce.core", (blocked as RouteGuardDecision.Blocked).featureId)
    }
}
