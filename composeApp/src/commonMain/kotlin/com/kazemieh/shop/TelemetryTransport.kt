package com.kazemieh.shop

import com.kazemieh.common.TelemetryDispatch
import com.kazemieh.config.capabilities.EndpointResolver
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

/** Sends the privacy-minimised, consented event contract to the selected backend. */
class TelemetryTransport(
    private val client: HttpClient,
    private val endpoints: EndpointResolver
) {
    suspend fun send(event: TelemetryDispatch) {
        client.post {
            url(endpoints.resolve("telemetry/events"))
            contentType(ContentType.Application.Json)
            setBody(
                TelemetryRequest(
                    consent = true,
                    name = event.name,
                    client = event.client,
                    tenantId = event.tenantId
                )
            )
        }
    }
}

@Serializable
private data class TelemetryRequest(
    val consent: Boolean,
    val name: String,
    val client: String,
    val tenantId: String
)
