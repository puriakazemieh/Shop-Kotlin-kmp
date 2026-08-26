package com.kazemieh.config.capabilities

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.serialization.json.Json

class ConfigurationModelsTest {
    private val json = Json { ignoreUnknownKeys = false }

    @Test
    fun serializesIndependentConfigurationModels() {
        val manifest = FeatureManifest(
            schemaVersion = 1,
            manifestVersion = "2026.08.1",
            backendKind = BackendKind.WORDPRESS,
            tenantId = "fixture-f1-shop",
            features = mapOf("content.blog" to true)
        )

        val decoded = json.decodeFromString<FeatureManifest>(json.encodeToString(manifest))

        assertEquals(manifest, decoded)
        assertEquals("shop-fa", BrandingConfig("shop-fa", "فروشگاه", "تومان").id)
        assertEquals("com.example.shop", BuildIdentity("com.example.shop", "1.0.0", 1).applicationId)
    }

    @Test
    fun rejectsInvalidIdentityAndManifestMetadata() {
        assertFailsWith<IllegalArgumentException> {
            BuildIdentity(applicationId = "", versionName = "1.0.0", versionCode = 1)
        }
        assertFailsWith<IllegalArgumentException> {
            FeatureManifest(1, "", BackendKind.SPRING, "tenant", emptyMap())
        }
    }
}
