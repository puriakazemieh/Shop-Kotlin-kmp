package com.kazemieh.config.capabilities

import kotlinx.serialization.Serializable

/** تنها نوع‌های backend مجاز برای artifactهای محصول. */
@Serializable
enum class BackendKind {
    WORDPRESS,
    SPRING
}

/**
 * هویت backend که از branding و قابلیت‌های tenant جدا نگه داشته می‌شود.
 * endpointهای تغییرناپذیر در P03-ARCH-CODE-004 به این مدل افزوده می‌شوند.
 */
@Serializable
data class BackendProfile(
    val kind: BackendKind
)

/** داده‌های هویتی/نمایشی برند؛ بدون endpoint و بدون feature flag. */
@Serializable
data class BrandingConfig(
    val id: String,
    val displayName: String,
    val currency: String
) {
    init {
        require(id.isNotBlank()) { "Brand id must not be blank." }
        require(displayName.isNotBlank()) { "Brand display name must not be blank." }
        require(currency.isNotBlank()) { "Brand currency must not be blank." }
    }
}

/** هویت build که با tenant، branding و backend origin یکی نیست. */
@Serializable
data class BuildIdentity(
    val applicationId: String,
    val versionName: String,
    val versionCode: Int
) {
    init {
        require(applicationId.isNotBlank()) { "Application id must not be blank." }
        require(versionName.isNotBlank()) { "Version name must not be blank." }
        require(versionCode > 0) { "Version code must be positive." }
    }
}

/**
 * دادهٔ خام Manifest پیش از اعمال catalog، dependency و compiled ceiling.
 * این سه کنترل در P03-MANIFEST-CODE-007 و P03-MANIFEST-CODE-008 اعمال می‌شوند.
 */
@Serializable
data class FeatureManifest(
    val schemaVersion: Int,
    val manifestVersion: String,
    val backendKind: BackendKind,
    val tenantId: String,
    val features: Map<String, Boolean>
) {
    init {
        require(schemaVersion > 0) { "Schema version must be positive." }
        require(manifestVersion.isNotBlank()) { "Manifest version must not be blank." }
        require(tenantId.isNotBlank()) { "Tenant id must not be blank." }
    }
}
