package com.kazemieh.network.common

import com.kazemieh.network.auth.dto.response.RefreshTokenResponse

import com.kazemieh.common.ld
import com.kazemieh.common.isDebugLoggingEnabled
import com.kazemieh.common.redactedForLog
import com.kazemieh.network.auth.dto.request.RefreshTokenRequest
import io.ktor.client.HttpClient
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.api.SendingRequest
import com.kazemieh.config.capabilities.EffectiveFeatureStore

import io.ktor.client.call.body
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.encodedPath
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json

object HttpClientFactory {

    fun create(tokenProvider: TokenProvider, featureStore: EffectiveFeatureStore? = null): HttpClient {
        return createPlatformHttpClient {

            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = false
                    isLenient = true
                    encodeDefaults = true
                })
            }

            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        if (isDebugLoggingEnabled) message.redactedForLog().ld("ktor")
                    }
                }
                level = if (isDebugLoggingEnabled) LogLevel.ALL else clientHttpLogLevel
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 30_000
                connectTimeoutMillis = 30_000
                socketTimeoutMillis = 30_000
            }

            expectSuccess = false

            install(Auth) {
                reAuthorizeOnResponse { response ->
                    response.status == HttpStatusCode.Unauthorized && ApiConfig.isApprovedApiUrl(response.call.request.url)
                }
                bearer {
                    loadTokens {
                        val accessToken = tokenProvider.getAccessToken()
                        val refreshToken = tokenProvider.getRefreshToken()

                        if (accessToken != null) {
                            BearerTokens(
                                accessToken = accessToken,
                                refreshToken = refreshToken.orEmpty()
                            )
                        } else null
                    }

                    sendWithoutRequest { request ->
                        ApiConfig.isApprovedApiOrigin(request.url.build())
                    }

                    refreshTokens {
                        val refreshToken = tokenProvider.getRefreshToken()

                        val webCookie = PlatformConfig.usesWebSessionCookie
                        if (!webCookie && refreshToken.isNullOrBlank()) {
                            return@refreshTokens null
                        }

                        try {
                            val httpResponse = client.post("${ApiConfig.baseUrl}${if (webCookie) "api/auth/web/refresh" else "api/auth/refresh"}") {
                                markAsRefreshTokenRequest()
                                if (!webCookie) {
                                    setBody(RefreshTokenRequest(requireNotNull(refreshToken)))
                                }
                            }
                            check(httpResponse.status.isSuccess())
                            val response: RefreshTokenResponse = httpResponse.body()
                            check(response.accessToken.isNotBlank())
                            check(if (webCookie) response.refreshToken.isEmpty() else response.refreshToken.isNotBlank())

                            // ذخیره توکن‌های جدید
                            tokenProvider.saveTokens(
                                accessToken = response.accessToken,
                                refreshToken = response.refreshToken
                            )

                            BearerTokens(
                                accessToken = response.accessToken,
                                refreshToken = response.refreshToken
                            )
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            tokenProvider.notifyTokenExpired()
                            tokenProvider.clearTokens()
                            null
                        }
                    }
                }
            }
            
            if (featureStore != null) {
                install(FeatureGuardPlugin) {
                    store = featureStore
                }
            }
            install(DefaultRequest) {
                url(ApiConfig.baseUrl)
                contentType(ContentType.Application.Json)
//                tokenProvider.getAccessToken()?.let { token ->
//                    header(HttpHeaders.Authorization, "Bearer $token")
//                }
            }
            install(BackendPathPlugin)
            install(BearerScopePlugin)

        }
    }
}

val BackendPathPlugin = createClientPlugin("BackendPathPlugin") {
    onRequest { request, _ ->
        if (ApiConfig.isApprovedApiOrigin(request.url.build())) {
            val path = request.url.build().encodedPath
            val resolved = ApiConfig.resolveInternalApiPath(path)
            if (resolved != path) request.url.encodedPath = resolved
        }
    }
}

/** Runs on every Ktor send, including authentication retries and redirects. */
val BearerScopePlugin = createClientPlugin("BearerScopePlugin") {
    on(SendingRequest) { request, _ ->
        if (!ApiConfig.isApprovedApiUrl(request.url.build())) {
            request.headers.remove(HttpHeaders.Authorization)
        }
    }
}

class FeatureGuardConfig {
    var store: EffectiveFeatureStore? = null
}

val FeatureGuardPlugin = createClientPlugin("FeatureGuardPlugin", ::FeatureGuardConfig) {
    val store = pluginConfig.store ?: return@createClientPlugin
    onRequest { request, _ ->
        val path = request.url.build().encodedPath
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
            throw Exception("Feature " + featureId + " is disabled by FeatureGuardPlugin")
        }
    }
}
