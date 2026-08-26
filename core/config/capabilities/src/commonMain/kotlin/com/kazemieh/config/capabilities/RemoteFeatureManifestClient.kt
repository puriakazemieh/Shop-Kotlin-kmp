package com.kazemieh.config.capabilities

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.coroutines.withTimeout

/** درخواست transport را از policy manifest جدا می‌کند تا timeout و cache قابل تست بمانند. */
data class RemoteManifestRequest(
    val url: String,
    val timeoutMillis: Long,
    val ifNoneMatch: String?
)

data class RemoteManifestResponse(
    val statusCode: Int,
    val body: String,
    val etag: String?
)

fun interface RemoteManifestTransport {
    suspend fun execute(request: RemoteManifestRequest): RemoteManifestResponse
}

/** transport واقعی Ktor؛ origin فقط از BackendProfile trusted ساخته می‌شود. */
class KtorRemoteManifestTransport(private val client: HttpClient) : RemoteManifestTransport {
    override suspend fun execute(request: RemoteManifestRequest): RemoteManifestResponse {
        val response = withTimeout(request.timeoutMillis) {
            client.get {
                url(request.url)
                request.ifNoneMatch?.let { header(HttpHeaders.IfNoneMatch, it) }
            }
        }
        return RemoteManifestResponse(
            statusCode = response.status.value,
            body = if (response.status == HttpStatusCode.NotModified) "" else response.bodyAsText(),
            etag = response.headers[HttpHeaders.ETag]
        )
    }
}

@Serializable
private data class RemoteFeatureManifestPayload(
    val schemaVersion: Int,
    val manifestVersion: String,
    val backendProfile: String,
    val tenantId: String,
    val minimumAppVersion: String = "",
    val issuedAt: String = "",
    val expiresAt: String = "",
    val integrity: String? = null,
    val features: Map<String, Boolean>
) {
    fun toManifest(): FeatureManifest = FeatureManifest(
        schemaVersion = schemaVersion,
        manifestVersion = manifestVersion,
        backendKind = BackendKind.valueOf(backendProfile),
        tenantId = tenantId,
        features = features
    )
}

sealed interface RemoteManifestFetchResult {
    data class Success(
        val manifest: FeatureManifest,
        val features: ResolvedFeatures,
        val etag: String?
    ) : RemoteManifestFetchResult

    data class NotModified(val etag: String?) : RemoteManifestFetchResult

    data class Failure(val reason: String) : RemoteManifestFetchResult
}

/** دریافت و اعتبارسنجی fail-closed manifest tenant. */
class RemoteFeatureManifestClient(
    private val profile: BackendProfile,
    private val expectedTenantId: String,
    private val transport: RemoteManifestTransport,
    private val catalog: FeatureCatalog = FeatureCatalog(),
    private val ceiling: CompiledFeatureCeiling = CompiledFeatureCeiling.shopOnly,
    private val timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS,
    private val json: Json = strictJson
) {
    init {
        require(expectedTenantId.isNotBlank()) { "Tenant id must not be blank." }
        require(timeoutMillis > 0) { "Manifest timeout must be positive." }
    }

    suspend fun fetch(ifNoneMatch: String? = null): RemoteManifestFetchResult = runCatching {
        val response = transport.execute(
            RemoteManifestRequest(
                url = ProfileEndpointResolver(profile).resolve(profile.manifestPath),
                timeoutMillis = timeoutMillis,
                ifNoneMatch = ifNoneMatch
            )
        )
        when (response.statusCode) {
            HttpStatusCode.NotModified.value -> RemoteManifestFetchResult.NotModified(response.etag ?: ifNoneMatch)
            HttpStatusCode.OK.value -> decode(response.body, response.etag)
            else -> RemoteManifestFetchResult.Failure("Manifest HTTP ${response.statusCode}.")
        }
    }.getOrElse { error ->
        RemoteManifestFetchResult.Failure(error.message ?: "Manifest request failed.")
    }

    private fun decode(body: String, etag: String?): RemoteManifestFetchResult {
        val payload = json.decodeFromString<RemoteFeatureManifestPayload>(body)
        require(payload.schemaVersion == 1) { "Unsupported manifest schema." }
        require(payload.backendProfile == profile.kind.name) { "Manifest backend mismatch." }
        require(payload.tenantId == expectedTenantId) { "Manifest tenant mismatch." }
        val manifest = payload.toManifest()
        val resolved = ceiling.apply(catalog.resolve(manifest))
        return RemoteManifestFetchResult.Success(manifest, resolved, etag)
    }

    companion object {
        const val DEFAULT_TIMEOUT_MILLIS = 5_000L
        val strictJson: Json = Json {
            ignoreUnknownKeys = false
            isLenient = false
            explicitNulls = true
        }
    }
}
