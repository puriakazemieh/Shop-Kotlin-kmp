package com.kazemieh.navigation

import com.kazemieh.config.capabilities.ResolvedFeatures

sealed interface RouteGuardDecision {
    data object Allowed : RouteGuardDecision
    data class Blocked(val route: String, val featureId: String) : RouteGuardDecision
}

/** نگاشت مرکزی route به capability؛ ورودی مستقیم خاموش به home امن برمی‌گردد. */
class FeatureRouteGuard(private val store: com.kazemieh.config.capabilities.EffectiveFeatureStore) {
    @Deprecated("Use EffectiveFeatureStore", ReplaceWith("FeatureRouteGuard(com.kazemieh.config.capabilities.DefaultEffectiveFeatureStore(features))"))
    constructor(features: com.kazemieh.config.capabilities.ResolvedFeatures) : this(com.kazemieh.config.capabilities.DefaultEffectiveFeatureStore(features))

    fun checkRoute(route: String): RouteGuardDecision {
        val featureId = featureIdFor(route) ?: return RouteGuardDecision.Allowed
        return if (store.features.value.isEnabled(featureId)) RouteGuardDecision.Allowed
        else RouteGuardDecision.Blocked(route, featureId)
    }

    private fun featureIdFor(route: String): String? = when {
        route.contains("Admin") || route.contains("Manage") -> "admin.mobile"
        route.contains("Course") || route.contains("Certificate") || route.contains("Placement") || route.contains("PeerReview") -> "academy.core"
        route.contains("Therapist") || route.contains("Appointment") || route.contains("Mood") || route.contains("Messaging") || route.contains("Homework") || route.contains("Journal") || route.contains("SessionReceipt") -> "clinic.booking"
        route.contains("PsychTest") || route.contains("TakeTest") -> "psych.tests"
        route.contains("Wallet") -> "wallet"
        route.contains("Blog") -> "content.blog"
        route.contains("ProductDetail") || route.contains("Cart") || route.contains("Checkout") || route.contains("CategorySearch") || route.contains("Bundle") || route.contains("Comparison") || route.contains("ShoppingAssistant") -> "commerce.core"
        else -> null
    }
}
