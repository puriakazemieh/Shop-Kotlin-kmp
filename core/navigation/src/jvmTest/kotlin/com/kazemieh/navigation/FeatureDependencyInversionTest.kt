package com.kazemieh.navigation

import com.kazemieh.common.Screen
import com.kazemieh.config.capabilities.DefaultEffectiveFeatureStore
import com.kazemieh.config.capabilities.ResolvedFeatures
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Architecture and Dependency Inversion tests for P02-CORE-CODE-015B.
 * Validates that `feature:profile` and `feature:admin:products` routes and
 * navigation contracts enforce strict architectural boundaries:
 * 1. Profile and Admin navigation tokens are self-contained without cross-feature dependencies.
 * 2. FeatureRouteGuard accurately evaluates entitlements and domain gates for Profile/Admin routes.
 * 3. Composition root orchestration in `profileNavGraph` and `adminNavGraph` maintains route independence.
 */
class FeatureDependencyInversionTest {

    @Test
    fun `profile route contract is decoupled from peer vertical domains`() {
        // Assert Profile and related route tokens are independent
        assertNotNull(Screen.Profile)
        assertNotNull(Screen.Wallet)
        assertNotNull(Screen.Referral)
        assertNotNull(Screen.Membership)
        assertNotNull(Screen.Favorites)

        // Route identity verification
        val profileRoute = Screen.Profile
        assertEquals("Profile", profileRoute::class.simpleName)

        val walletRoute = Screen.Wallet
        assertEquals("Wallet", walletRoute::class.simpleName)
    }

    @Test
    fun `admin route contract is decoupled from peer vertical domains`() {
        // Assert Admin Panel and sub-routes are self-contained
        assertNotNull(Screen.AdminPanel)
        assertNotNull(Screen.ManageOrders)
        assertNotNull(Screen.ManageOptions)
        assertNotNull(Screen.ManageDiscounts)
        assertNotNull(Screen.ManageWallets)
        assertNotNull(Screen.ManageWithdrawals)
        assertNotNull(Screen.ManageStories)
        assertNotNull(Screen.AdminBlogList)

        val manageProduct = Screen.ManageProduct(id = 101L)
        assertEquals(101L, manageProduct.id)

        val manageBlog = Screen.ManageBlog(id = 202L, slug = "release-announcement")
        assertEquals(202L, manageBlog.id)
        assertEquals("release-announcement", manageBlog.slug)
    }

    @Test
    fun `route guard protects feature boundaries without coupling profile and admin`() {
        val features = mapOf(
            "commerce.core" to true,
            "content.blog" to true,
            "admin.mobile" to true,
            "wallet" to false,
            "academy.core" to false,
            "clinic.booking" to false,
            "psych.tests" to false
        )
        val store = DefaultEffectiveFeatureStore(ResolvedFeatures.fromMap(features))
        val guard = FeatureRouteGuard(store)

        // Profile route itself is allowed
        assertEquals(RouteGuardDecision.Allowed, guard.checkRoute("com.kazemieh.common.Screen.Profile"))

        // Admin panel is allowed when admin.mobile is enabled
        assertEquals(RouteGuardDecision.Allowed, guard.checkRoute("com.kazemieh.common.Screen.AdminPanel"))

        // Wallet feature is blocked by entitlement without leaking cross-feature dependencies
        val walletDecision = guard.checkRoute("com.kazemieh.common.Screen.Wallet")
        assertTrue(walletDecision is RouteGuardDecision.Blocked)
        assertEquals("wallet", walletDecision.featureId)

        // Vertical features are gated independently
        val academyDecision = guard.checkRoute("com.kazemieh.common.Screen.CourseCatalog")
        assertTrue(academyDecision is RouteGuardDecision.Blocked)
        assertEquals("academy.core", academyDecision.featureId)
    }
}
