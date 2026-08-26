package com.kazemieh.navigation

import com.kazemieh.config.capabilities.BackendKind
import com.kazemieh.config.capabilities.BootstrapProfiles
import com.kazemieh.config.capabilities.FeatureCatalog
import com.kazemieh.config.capabilities.FeatureManifest
import com.kazemieh.config.capabilities.FeatureUseCaseGuard
import com.kazemieh.config.capabilities.FeatureCallResult
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** F0..F4 contract matrix for both trusted backend profiles. */
class P03ManifestMatrixTest {
    private val profiles = listOf(BackendKind.WORDPRESS, BackendKind.SPRING)
    private val fixtures = listOf(
        setOf("content.blog"),
        setOf("content.blog", "commerce.core", "commerce.physical", "commerce.digital"),
        setOf("content.blog", "academy.core", "academy.quiz", "academy.certificate"),
        setOf("content.blog", "clinic.booking", "clinic.messaging", "psych.tests"),
        FeatureCatalog.v1Definitions.map { it.id }.toSet()
    )

    @Test
    fun `every fixture preserves route and network expectations for both profiles`() = runBlocking {
        profiles.forEach { backend ->
            val profile = BootstrapProfiles.forBackend(backend, "https://${backend.name.lowercase()}.example.test/")
            fixtures.forEachIndexed { index, enabled ->
                val resolved = FeatureCatalog().resolve(
                    FeatureManifest(1, "fixture-f$index", backend, "tenant-f$index", enabled.associateWith { true })
                )
                val guard = FeatureRouteGuard(resolved)
                val expectedRoute = when (index) {
                    0 -> "BlogList"
                    1 -> "ProductDetail"
                    2 -> "CourseDetail"
                    3 -> "TherapistDetail"
                    else -> "AdminDashboard"
                }
                assertEquals(RouteGuardDecision.Allowed, guard.checkRoute("Screen.$expectedRoute"))
                if (index < 4) {
                    assertTrue(guard.checkRoute("Screen.${listOf("ProductDetail", "CourseDetail", "TherapistDetail", "ProductDetail")[index]}") is RouteGuardDecision.Blocked)
                }
                assertEquals(backend, profile.kind)

                var calls = 0
                val useCase = FeatureUseCaseGuard(resolved)
                val feature = if (index == 0) "commerce.core" else "academy.core"
                val result = useCase.execute(feature) { calls++; "network" }
                if (resolved.isEnabled(feature)) {
                    assertTrue(result is FeatureCallResult.Executed)
                    assertEquals(1, calls)
                } else {
                    assertEquals(FeatureCallResult.Disabled, result)
                    assertEquals(0, calls)
                }
            }
        }
    }
}
