import re

path = 'core/network/src/commonMain/kotlin/com/kazemieh/network/common/HttpClientFactory.kt'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

# Add EffectiveFeatureStore and FeatureGuardPlugin imports
content = content.replace('import io.ktor.client.HttpClient', 'import io.ktor.client.HttpClient\nimport io.ktor.client.plugins.api.createClientPlugin\nimport com.kazemieh.config.capabilities.EffectiveFeatureStore\nimport com.kazemieh.network.common.ApiException\nimport com.kazemieh.common.Res')

# Add FeatureGuardPlugin configuration and creation
plugin_code = """
class FeatureGuardConfig {
    var store: EffectiveFeatureStore? = null
}

val FeatureGuardPlugin = createClientPlugin("FeatureGuardPlugin", ::FeatureGuardConfig) {
    val store = pluginConfig.store ?: return@createClientPlugin
    onRequest { request, _ ->
        val path = request.url.encodedPath
        val featureId = when {
            path.contains("/api/admin") -> "admin.mobile"
            path.contains("/api/academy") || path.contains("/api/courses") -> "academy.core"
            path.contains("/api/clinic") || path.contains("/api/appointments") || path.contains("/api/therapist") -> "clinic.booking"
            path.contains("/api/psychtest") -> "psych.tests"
            path.contains("/api/cart") || path.contains("/api/order") || path.contains("/api/products") || path.contains("/api/bundle") || path.contains("/api/catalog") -> "commerce.core"
            path.contains("/api/blog") -> "content.blog"
            path.contains("/api/wallet") -> "wallet"
            else -> null
        }
        if (featureId != null && !store.features.value.isEnabled(featureId)) {
            throw ApiException(
                messageText = Res.string.unknownError, // Using unknownError as fallback string resource
                code = 403
            )
        }
    }
}
"""

if 'val FeatureGuardPlugin' not in content:
    content += plugin_code

# Update HttpClientFactory.create method signature and usage
content = content.replace('fun create(tokenProvider: TokenProvider): HttpClient {', 'fun create(tokenProvider: TokenProvider, featureStore: EffectiveFeatureStore? = null): HttpClient {')

install_guard = """
            if (featureStore != null) {
                install(FeatureGuardPlugin) {
                    store = featureStore
                }
            }
"""
content = content.replace('install(DefaultRequest) {', install_guard + '            install(DefaultRequest) {')

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)
print('Done HttpClientFactory!')
