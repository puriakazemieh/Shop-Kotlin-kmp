package com.kazemieh.navigation

import com.kazemieh.common.Screen
import com.kazemieh.config.capabilities.DefaultEffectiveFeatureStore
import com.kazemieh.config.capabilities.ManifestBootstrapSource
import com.kazemieh.config.capabilities.ManifestBootstrapState
import com.kazemieh.config.capabilities.ResolvedFeatures
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FeatureRouteGuardTest {

    @Test
    fun `disabled direct route is blocked while home and enabled route pass`() {
        val store = DefaultEffectiveFeatureStore(ResolvedFeatures.fromMap(mapOf("content.blog" to true, "commerce.core" to false)))
        val guard = FeatureRouteGuard(store)
        assertEquals(RouteGuardDecision.Allowed, guard.checkRoute("com.kazemieh.common.Screen.HomeGraph"))
        assertEquals(RouteGuardDecision.Allowed, guard.checkRoute("com.kazemieh.common.Screen.BlogList"))
        val blocked = guard.checkRoute("com.kazemieh.common.Screen.ProductDetail")
        assertTrue(blocked is RouteGuardDecision.Blocked)
        assertEquals("commerce.core", blocked.featureId)
    }

    @Test
    fun `checkRoute with Any screen objects validates type-safe routes`() {
        val features = mapOf(
            "commerce.core" to false,
            "academy.core" to true,
            "clinic.booking" to false,
            "psych.tests" to true,
            "wallet" to false,
            "admin.mobile" to false,
            "content.blog" to true
        )
        val store = DefaultEffectiveFeatureStore(ResolvedFeatures.fromMap(features))
        val guard = FeatureRouteGuard(store)

        // Allowed screens
        assertEquals(RouteGuardDecision.Allowed, guard.checkRoute(Screen.HomeGraph()))
        assertEquals(RouteGuardDecision.Allowed, guard.checkRoute(Screen.CourseCatalog))
        assertEquals(RouteGuardDecision.Allowed, guard.checkRoute(Screen.CourseDetail(slug = "sample-course")))
        assertEquals(RouteGuardDecision.Allowed, guard.checkRoute(Screen.PsychTestCatalog))
        assertEquals(RouteGuardDecision.Allowed, guard.checkRoute(Screen.TakeTest(userTestId = 12L)))
        assertEquals(RouteGuardDecision.Allowed, guard.checkRoute(Screen.BlogList))
        assertEquals(RouteGuardDecision.Allowed, guard.checkRoute(Screen.BlogDetail(slug = "my-post")))

        // Blocked screens
        val blockedCommerce = guard.checkRoute(Screen.ProductDetail(slug = "test-prod"))
        assertTrue(blockedCommerce is RouteGuardDecision.Blocked)
        assertEquals("commerce.core", blockedCommerce.featureId)

        val blockedCart = guard.checkRoute(Screen.Cart)
        assertTrue(blockedCart is RouteGuardDecision.Blocked)
        assertEquals("commerce.core", blockedCart.featureId)

        val blockedCheckout = guard.checkRoute(Screen.Checkout(totalAmount = 500.0))
        assertTrue(blockedCheckout is RouteGuardDecision.Blocked)
        assertEquals("commerce.core", blockedCheckout.featureId)

        val blockedClinic = guard.checkRoute(Screen.TherapistCatalog)
        assertTrue(blockedClinic is RouteGuardDecision.Blocked)
        assertEquals("clinic.booking", blockedClinic.featureId)

        val blockedAppointment = guard.checkRoute(Screen.MyAppointments)
        assertTrue(blockedAppointment is RouteGuardDecision.Blocked)
        assertEquals("clinic.booking", blockedAppointment.featureId)

        val blockedWallet = guard.checkRoute(Screen.Wallet)
        assertTrue(blockedWallet is RouteGuardDecision.Blocked)
        assertEquals("wallet", blockedWallet.featureId)

        val blockedAdmin = guard.checkRoute(Screen.AdminPanel)
        assertTrue(blockedAdmin is RouteGuardDecision.Blocked)
        assertEquals("admin.mobile", blockedAdmin.featureId)
    }

    @Test
    fun `guard dynamically reflects changes in EffectiveFeatureStore without rebuild`() {
        val initialFeatures = mapOf("commerce.core" to true, "academy.core" to true)
        val store = DefaultEffectiveFeatureStore(ResolvedFeatures.fromMap(initialFeatures))
        val guard = FeatureRouteGuard(store)

        // Initially commerce is allowed
        assertTrue(guard.isFeatureEnabled("commerce.core"))
        assertEquals(RouteGuardDecision.Allowed, guard.checkRoute(Screen.ProductDetail("widget")))

        // Dynamically update manifest state to toggle off commerce.core
        val stateRev2 = ManifestBootstrapState.Ready(
            features = ResolvedFeatures.fromMap(mapOf("commerce.core" to false, "academy.core" to true)),
            source = ManifestBootstrapSource.REMOTE,
            etag = "rev-2"
        )
        store.update(stateRev2)

        // Guard immediately reflects the new disabled state without rebuild
        assertFalse(guard.isFeatureEnabled("commerce.core"))
        val blocked = guard.checkRoute(Screen.ProductDetail("widget"))
        assertTrue(blocked is RouteGuardDecision.Blocked)
        assertEquals("commerce.core", blocked.featureId)

        // Toggling it back on immediately allows it again
        val stateRev3 = ManifestBootstrapState.Ready(
            features = ResolvedFeatures.fromMap(mapOf("commerce.core" to true, "academy.core" to true)),
            source = ManifestBootstrapSource.REMOTE,
            etag = "rev-3"
        )
        store.update(stateRev3)
        assertTrue(guard.isFeatureEnabled("commerce.core"))
        assertEquals(RouteGuardDecision.Allowed, guard.checkRoute(Screen.ProductDetail("widget")))
    }

    @Test
    fun `deep link path evaluation matches route guard decisions`() {
        val store = DefaultEffectiveFeatureStore(ResolvedFeatures.fromMap(mapOf(
            "commerce.core" to false,
            "academy.core" to false,
            "clinic.booking" to true
        )))
        val guard = FeatureRouteGuard(store)

        // Deep links matching blocked feature
        val productDeepLink = "com.kazemieh.common.Screen.ProductDetail/spring-shoe"
        val courseDeepLink = "com.kazemieh.common.Screen.CourseDetail/math-101"
        val clinicDeepLink = "com.kazemieh.common.Screen.TherapistDetail/dr-reza"

        val blockedProd = guard.checkRoute(productDeepLink)
        assertTrue(blockedProd is RouteGuardDecision.Blocked)
        assertEquals("commerce.core", blockedProd.featureId)

        val blockedCourse = guard.checkRoute(courseDeepLink)
        assertTrue(blockedCourse is RouteGuardDecision.Blocked)
        assertEquals("academy.core", blockedCourse.featureId)

        val allowedClinic = guard.checkRoute(clinicDeepLink)
        assertEquals(RouteGuardDecision.Allowed, allowedClinic)
    }
}
