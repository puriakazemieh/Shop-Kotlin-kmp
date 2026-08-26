package com.kazemieh.config.capabilities

/** URLهای endpoint را فقط بر پایهٔ پروفایل bootstrap قابل اعتماد می‌سازد. */
interface EndpointResolver {
    fun resolve(path: String): String
}

/** URLهای asset را برای mapperها، بدون وابستگی به config سراسری، می‌سازد. */
interface AssetUrlResolver {
    fun resolve(path: String): String
}

class ProfileEndpointResolver(private val profile: BackendProfile) : EndpointResolver {
    override fun resolve(path: String): String = profile.apiRoot.joinTrustedPath(path)
}

class ProfileAssetUrlResolver(private val profile: BackendProfile) : AssetUrlResolver {
    override fun resolve(path: String): String =
        if (path.isAbsoluteUrl()) path else profile.assetRoot.joinTrustedPath(path)
}

/** موقتاً برای profileهای legacy توسعه؛ artifactهای release باید profile صریح داشته باشند. */
object BootstrapProfiles {
    fun fromLegacyApiRoot(apiRoot: String): BackendProfile {
        val origin = apiRoot.origin()
        return BackendProfile(
            kind = BackendKind.SPRING,
            apiRoot = apiRoot.ensureTrailingSlash(),
            assetRoot = origin,
            allowedAuthHosts = setOf(origin.host()),
            contractVersion = 1,
            manifestPath = "/client-manifest"
        )
    }
}

private fun String.joinTrustedPath(path: String): String {
    require(!path.isAbsoluteUrl()) { "A remote path must not replace the trusted origin." }
    return "${removeSuffix("/")}/${path.removePrefix("/")}" 
}

private fun String.isAbsoluteUrl(): Boolean =
    startsWith("https://") || startsWith("http://")

private fun String.ensureTrailingSlash(): String = if (endsWith('/')) this else "$this/"

private fun String.origin(): String {
    val schemeEnd = indexOf("://") + 3
    require(schemeEnd > 2) { "A URL origin is required." }
    val pathStart = indexOf('/', schemeEnd)
    return if (pathStart == -1) ensureTrailingSlash() else substring(0, pathStart).ensureTrailingSlash()
}

private fun String.host(): String =
    removePrefix("https://").removePrefix("http://").substringBefore('/').substringBefore(':')
