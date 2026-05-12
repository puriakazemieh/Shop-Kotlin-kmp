package com.kazemieh.network

import com.kazemieh.common.ld
import com.kazemieh.network.dto.request.RefreshTokenRequest
import com.kazemieh.network.dto.response.RefreshTokenResponse
import io.ktor.client.HttpClient
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
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object HttpClientFactory {

    fun create(tokenProvider: TokenProvider): HttpClient {
        return HttpClient(httpClientEngine()) {

            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                })
            }

            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        message.ld("ktor ")
                    }
                }
                level = LogLevel.ALL
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 30_000
                connectTimeoutMillis = 30_000
                socketTimeoutMillis = 30_000
            }

            expectSuccess = false

            install(Auth) {
                bearer {
                    loadTokens {
                        val accessToken = tokenProvider.getAccessToken()
                        val refreshToken = tokenProvider.getRefreshToken()

                        if (accessToken != null && refreshToken != null) {
                            BearerTokens(
                                accessToken = accessToken,
                                refreshToken = refreshToken
                            )
                        } else null
                    }

                    refreshTokens {
                        val refreshToken = tokenProvider.getRefreshToken()

                        if (refreshToken == null) {
                            return@refreshTokens null
                        }

                        try {
                            // درخواست refresh token
                            val response: RefreshTokenResponse =
                                client.post("${PlatformConfig.baseUrl}api/auth/refresh") {
                                    markAsRefreshTokenRequest()
                                    setBody(RefreshTokenRequest(refreshToken))
                                }.body()

                            // ذخیره توکن‌های جدید
                            tokenProvider.saveTokens(
                                accessToken = response.accessToken,
                                refreshToken = response.refreshToken
                            )

                            BearerTokens(
                                accessToken = response.accessToken,
                                refreshToken = response.refreshToken
                            )
                        } catch (e: Exception) {
                            e.ld("token Exception")
                            tokenProvider.notifyTokenExpired()
                            tokenProvider.clearTokens()
                            null
                        }
                    }
                }
            }
            install(DefaultRequest) {
                url(PlatformConfig.baseUrl)
                contentType(ContentType.Application.Json)
                tokenProvider.getAccessToken()?.let { token ->
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            }

        }
    }
}